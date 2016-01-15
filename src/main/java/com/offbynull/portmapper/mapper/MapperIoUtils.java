/*
 * Copyright (c) 2013-2016, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.portmapper.mapper;

import com.offbynull.portmapper.gateway.BasicBus;
import com.offbynull.portmapper.gateway.Bus;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV4;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV6;
import com.offbynull.portmapper.gateways.network.internalmessages.CloseNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateUdpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetLocalIpAddressesNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetLocalIpAddressesNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadUdpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteUdpNetworkRequest;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that simplifies IO for mappers.
 * @author Kasra Faghihi
 */
public final class MapperIoUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MapperIoUtils.class);
    
    private MapperIoUtils() {
        
    }
    
    /**
     * Default IPv4 router addresses. IPs grabbed from http://www.techspot.com/guides/287-default-router-ip-addresses/ + comments @
     * 1/10/2016.
     */
    public static final UnmodifiableSet<InetAddress> PRESET_IPV4_GATEWAY_ADDRESSES;
    static {
        Set<InetAddress> gatewayAddresses = convertToAddressSet(Arrays.asList(
                // 2Wire
                "192.168.1.1",
                "192.168.0.1",
                "192.168.1.254",
                "10.0.0.138",
                // 3Com
                "192.168.1.1",
                "192.168.1.10.1",
                // Actiontec
                "192.168.1.1",
                "192.168.0.1",
                "192.168.2.1",
                "192.168.254.254",
                // Airlink
                "192.168.1.1",
                "192.168.2.1",
                // Airlive
                "192.168.2.1",
                // Airties
                "192.168.2.1",
                // Apple
                "10.0.1.1",
                // Amped Wireless
                "192.168.3.1",
                // Asus
                "192.168.1.1",
                "192.168.2.1",
                "10.10.1.1",
                // Aztech
                "192.168.1.1",
                "192.168.2.1",
                "192.168.1.254",
                "192.168.254.254",
                // Belkin
                "192.168.1.1",
                "192.168.2.1",
                "10.0.0.2",
                "10.1.1.1",
                // Billion
                "192.168.1.254",
                "10.0.0.2",
                // Buffalo
                "192.168.1.1",
                "192.168.11.1",
                // Dell
                "192.168.1.1",
                // Cisco
                "192.168.1.1",
                "192.168.0.30",
                "192.168.0.50",
                "10.0.0.1",
                "10.0.0.2",
                // D-Link
                "192.168.1.1",
                "192.168.0.1",
                "192.168.0.10",
                "192.168.0.101",
                "192.168.0.30",
                "192.168.0.50",
                "192.168.1.254",
                "192.168.15.1",
                "192.168.254.254",
                "10.0.0.1",
                "10.0.0.2",
                "10.1.1.1",
                "10.90.90.90",
                // Edimax
                "192.168.2.1",
                // Eminent
                "192.168.1.1",
                "192.168.0.1",
                "192.168.8.1",
                // Gigabyte
                "192.168.1.254",
                // Hawking
                "192.168.1.200",
                "192.168.1.254",
                // Huawei
                "192.168.1.1",
                "192.168.0.1",
                "192.168.3.1",
                "192.168.8.1",
                "192.168.100.1",
                "10.0.0.138",
                // LevelOne
                "192.168.0.1",
                "192.168.123.254",
                // Linksys
                "192.168.1.1",
                "192.168.0.1",
                "192.168.1.10",
                "192.168.1.210",
                "192.168.1.254",
                "192.168.1.99",
                "192.168.15.1",
                "192.168.16.1",
                "192.168.2.1",
                // Microsoft
                "192.168.2.1",
                // Motorola
                "192.168.0.1",
                "192.168.10.1",
                "192.168.15.1",
                "192.168.20.1",
                "192.168.30.1",
                "192.168.62.1",
                "192.168.100.1",
                "192.168.102.1",
                "192.168.1.254",
                // MSI
                "192.168.1.254",
                // Netgear
                "192.168.0.1",
                "192.168.0.227",
                // NetComm
                "192.168.1.1",
                "192.168.10.50",
                "192.168.20.1",
                "10.0.0.138",
                // Netopia
                "192.168.0.1",
                "192.168.1.254",
                // Planet
                "192.168.1.1",
                "192.168.0.1",
                "192.168.1.254",
                // Repotec
                "192.168.1.1",
                "192.168.10.1",
                "192.168.16.1",
                "192.168.123.254",
                // Senao
                "192.168.0.1",
                // Siemens
                "192.168.1.1",
                "192.168.0.1",
                "192.168.1.254",
                "192.168.2.1",
                "192.168.254.254",
                "10.0.0.138",
                "10.0.0.2",
                // Sitecom
                "192.168.0.1",
                "192.168.1.254",
                "192.168.123.254",
                "10.0.0.1",
                // SMC Networks
                "192.168.1.1",
                "192.168.0.1",
                "192.168.2.1",
                "10.0.0.1",
                "10.1.10.1",
                // Sonicwall
                "192.168.0.3",
                "192.168.168.168",
                // SpeedTouch
                "10.0.0.138",
                "192.168.1.254",
                // Sweex
                "192.168.15.1",
                "192.168.50.1",
                "192.168.55.1",
                "192.168.251.1",
                // Tenda
                "192.168.1.1",
                "192.168.0.1",
                // Thomson
                "192.168.0.1",
                "192.168.1.254",
                "192.168.100.1",
                // TP-Link
                "192.168.1.1",
                "192.168.0.1",
                "192.168.0.254",
                // Trendnet
                "192.168.1.1",
                "192.168.0.1",
                "192.168.0.30",
                "192.168.0.100",
                "192.168.1.100",
                "192.168.1.254",
                "192.168.10.1",
                "192.168.10.10",
                "192.168.10.100",
                "192.168.2.1",
                "192.168.223.100",
                // "200.200.200.5", -- this seems invalid, it isn't a local IP
                // U.S. Robotics
                "192.168.1.1",
                "192.168.2.1",
                "192.168.123.254",
                // Zoom
                "192.168.1.1",
                "192.168.2.1",
                "192.168.4.1",
                "192.168.10.1",
                "192.168.1.254",
                "10.0.0.2",
                "10.0.0.138",
                // ZTE
                "192.168.1.1",
                "192.168.0.1",
                "192.168.100.100",
                "192.168.1.254",
                "192.168.2.1",
                "192.168.2.254",
                // Zyxel
                "192.168.1.1",
                "192.168.0.1",
                "192.168.2.1",
                "192.168.4.1",
                "192.168.10.1",
                "192.168.1.254",
                "192.168.254.254",
                "10.0.0.2",
                "10.0.0.138"
        ));
        
        PRESET_IPV4_GATEWAY_ADDRESSES = (UnmodifiableSet<InetAddress>) UnmodifiableSet.unmodifiableSet(gatewayAddresses);
    }
    
//    static Set<String> runCommandline(Bus processBus, RunProcessRequest ... reqs) throws InterruptedException {
//
//        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
//        Bus selfBus = new BasicBus(queue);
//        long timeout = 10000L;
//        long endTime = System.currentTimeMillis() + timeout;
//
//        // Create processes
//        Map<Integer, ByteArrayOutputStream> readBuffers = new HashMap<>();
//        next:
//        for (RunProcessRequest req : reqs) {
//            LOG.debug("Starting process {}", req);
//
//            int id;
//
//            processBus.send(new GetNextIdProcessRequest(selfBus));
//            while (true) {
//                long sleepTime = endTime - System.currentTimeMillis();
//                Validate.validState(sleepTime > 0, "Failed to create all processes in time");
//
//                Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
//                if (resp instanceof GetNextIdProcessResponse) {
//                    id = ((GetNextIdProcessResponse) resp).getId();
//                    break;
//                }
//            }
//            
//            processBus.send(new CreateProcessRequest(id, selfBus, req.getExecutable(), req.getParameters()));
//        }
//
//        // Read data from sockets
//        int runningProcs = reqs.length;
//        while (runningProcs > 0) {
//            long sleepTime = endTime - System.currentTimeMillis();
//            if (sleepTime <= 0L) {
//                break;
//            }
//
//            Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
//
//            if (resp instanceof ReadProcessNotification) {
//                // On read, put in to readBuffer
//                ReadProcessNotification readResp = (ReadProcessNotification) resp;
//                if (readResp.getReadType() == ReadType.STDOUT) {
//                    Integer id = readResp.getId();
//
//                    ByteArrayOutputStream baos = readBuffers.get(id);
//                    if (baos == null) {
//                        baos = new ByteArrayOutputStream();
//                        readBuffers.put(id, baos);
//                    }
//
//                    try {
//                        baos.write(readResp.getData());
//                    } catch (IOException ioe) {
//                        throw new IllegalStateException(); // should never happen
//                    }
//                }
//            } else if (resp instanceof ExitProcessNotification) {
//                runningProcs--;
//            } else if (resp instanceof IdentifiableErrorProcessResponse) {
//                runningProcs--;
//            } else if (resp instanceof ErrorProcessResponse) {
//                runningProcs--;
//            }
//        }
//
//        // Issue closes
//        for (int id : readBuffers.keySet()) {
//            processBus.send(new CloseProcessRequest(id));
//        }
//
//        // Process responses
//        Set<String> ret = new HashSet<>();
//        for (Entry<Integer, ByteArrayOutputStream> entry : readBuffers.entrySet()) {
//            String resp = new String(entry.getValue().toByteArray(), Charset.forName("US-ASCII"));
//            LOG.debug("Process respose {}", resp);
//            ret.add(resp);
//        }
//        
//        return ret;
//    }
//
//    public static final class RunProcessRequest {
//        private final String executable;
//        private final String[] parameters;
//        private String output;
//        
//        RunProcessRequest(String executable, String ... parameters) {
//            Validate.notNull(executable);
//            Validate.notNull(parameters);
//            Validate.noNullElements(parameters);
//
//            this.executable = executable;
//            this.parameters = Arrays.copyOf(parameters, parameters.length);
//        }
//
//        public String[] getParameters() {
//            return Arrays.copyOf(parameters, parameters.length);
//        }
//
//        public String getExecutable() {
//            return executable;
//        }
//
//        public String getOutput() {
//            return output;
//        }
//
//        void setOutput(String output) {
//            this.output = output;
//        }
//
//        @Override
//        public String toString() {
//            return "RunProcessRequest{" + "executable=" + executable + ", parameters=" + Arrays.toString(parameters) + ", output=" +output
//                    + '}';
//        }
//
//        
//    }
    
    /**
     * Convert a list of addresses as strings to {@link InetAddress}es.
     * @param addresses string addresses to convert
     * @return converted addresses
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     */
    public static Set<InetAddress> convertToAddressSet(List<String> addresses) {
        Validate.notNull(addresses);
        Validate.noNullElements(addresses);
        
        Set<InetAddress> ret = new HashSet<>();
        for (String address : addresses) {
            try {
                ret.add(InetAddress.getByName(address));
            } catch (UnknownHostException uhe) {
                // do nothing
            }
        }
        
        return ret;
    }
    
    /**
     * Get local IPs.
     * @param networkBus network bus
     * @return local ips
     * @throws NullPointerException if any argument is {@code null}
     * @throws InterruptedException if interrupted
     */
    public static Set<InetAddress> getLocalIpAddresses(Bus networkBus) throws InterruptedException {
        Validate.notNull(networkBus);
        
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Get local IP addresses
        LOG.debug("Getting local IP addresses");
        
        networkBus.send(new GetLocalIpAddressesNetworkRequest(selfBus));
        GetLocalIpAddressesNetworkResponse localIpsResp = (GetLocalIpAddressesNetworkResponse) queue.poll(1000L, TimeUnit.MILLISECONDS);
        
        Validate.validState(localIpsResp != null);
        
        LOG.debug("Got local IP addresses {}", localIpsResp);
        
        return localIpsResp.getLocalAddresses();
    }

    /**
     * Calculate attempt durations as per NAT-PMP/PCP specifications. Starts off at 250ms and doubles for each attempt (e.g. 250, 500, 1000,
     * ...). The RFCs say that this shouldn't extend past 9 attempts, but practically this should be much lower (otherwise the user would
     * end up waiting a long time for a mapping)
     * @param attempts number of attempts
     * @throws IllegalArgumentException if any numeric argument is negative
     * @return attempt durations
     */
    public static long[] calculateExponentialBackoffTimes(int attempts) {
        Validate.isTrue(attempts >= 0);
        
        long[] ret = new long[attempts];
        
        long nextTime = 250L;
        for (int i = 0; i < attempts; i++) {
            ret[i] = nextTime;
            nextTime *= 2;
        }
        
        return ret;
    }
    
    /**
     * Perform a group of UDP requests.
     * @param networkBus network bus
     * @param reqs requests to perform
     * @param broadcastBehaviour {@code true} if multiple responses may come in from any IP to each request, {@code false} if only a single
     * response is expected from the IP that the request was sent to (NOTE: if this is {@code true} and there are multiple UDP requests
     * set to go out from the same source IP, it's impossible to identify which of those requests the response is for when it comes in...
     * as such, the response will be added to a random request that has the same source IP)
     * @param attemptDurations amount of time to wait before resending a request
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws InterruptedException if interrupted
     */
    public static void performUdpRequests(Bus networkBus, Collection<UdpRequest> reqs, boolean broadcastBehaviour,
            long ... attemptDurations) throws InterruptedException {
        
        Validate.notNull(networkBus);
        Validate.notNull(reqs);
        Validate.noNullElements(reqs);
        Validate.notNull(attemptDurations);
        
        LOG.debug("Performing udp requests {} with durations {}", reqs, attemptDurations);
        
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        BidiMap<InetAddress, Integer> addressToSocketId = new DualHashBidiMap<>(); // source address to socket id
        MultiValuedMap<Integer, UdpRequest> socketIdToRequests = new ArrayListValuedHashMap<>(); // source address to requests

        
        // Assign IDs for new sockets
        long endCreateTime = System.currentTimeMillis() + 1000L;
        for (UdpRequest req : reqs) {
            long sleepTime = endCreateTime - System.currentTimeMillis();
            Validate.validState(sleepTime > 0, "Failed to perform all UDP operations in desired time");

            InetAddress source = req.getSourceAddress();
            if (addressToSocketId.containsKey(source)) {
                continue;
            }
            
            if (req.getDestinationSocketAddress().getAddress().equals(ZERO_IPV4)
                    || req.getDestinationSocketAddress().getAddress().equals(ZERO_IPV6)) {
                // skip if 0.0.0.0 or :: -- we don't want to bind to 'any' address
                continue;
            }
            
            LOG.debug("Creating socket ID for {}", source);

            networkBus.send(new GetNextIdNetworkRequest(selfBus));
            Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
            int id = ((GetNextIdNetworkResponse) resp).getId();
            
            addressToSocketId.put(source, id);
            
            LOG.debug("Socket ID for {} is {}", source, id);
        }

        
        // Create new sockets
        next:
        for (Entry<InetAddress, Integer> entry : addressToSocketId.entrySet()) {
            int id = entry.getValue();
            InetAddress source = entry.getKey();
            
            networkBus.send(new CreateUdpNetworkRequest(id, selfBus, source));
            // Don't worry if it was created or not -- just assume that it was
        }
        
        
        // Queue up requests to send out
        for (UdpRequest req : reqs) {
            int id = addressToSocketId.get(req.getSourceAddress());
            socketIdToRequests.put(id, req);
        }


        // Send requests
        Queue<Long> remainingAttemptDurations = new LinkedList<>();
        for (long attemptDuration : attemptDurations) {
            remainingAttemptDurations.add(attemptDuration);
        }
        while (!socketIdToRequests.isEmpty() && !remainingAttemptDurations.isEmpty()) {
            // Send requests to whoever hasn't responded yet
            for (UdpRequest req : socketIdToRequests.values()) {
                int id = addressToSocketId.get(req.getSourceAddress());
                
                Object request = req.getRequest();
                byte[] reqBytes = req.getRequestToBytesTransformer().create(request); // should never throw an exc -- we created the request
                
                InetSocketAddress dst = req.getDestinationSocketAddress();
                
                networkBus.send(new WriteUdpNetworkRequest(id, dst, reqBytes));
            }

            // Wait for responses
            long timeout = remainingAttemptDurations.poll();
            long endTime = System.currentTimeMillis() + timeout;
            while (true) {
                long sleepTime = endTime - System.currentTimeMillis();
                if (sleepTime <= 0L) {
                    break;
                }

                Object netResp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                if (netResp == null) {
                    LOG.debug("Timed out waiting for response");
                    continue;
                } else if (!(netResp instanceof ReadUdpNetworkNotification)) {
                    LOG.debug("Expected a read but encountered {} -- skipping", netResp);
                    continue;
                }

                ReadUdpNetworkNotification readNetResp = (ReadUdpNetworkNotification) netResp;
                int id = readNetResp.getId();

                InetSocketAddress remoteSocketAddress = readNetResp.getRemoteAddress();
                Iterator<UdpRequest> it = socketIdToRequests.get(id).iterator();
                while (it.hasNext()) {
                    UdpRequest pendingReq = it.next();
                    if (broadcastBehaviour || pendingReq.getDestinationSocketAddress().equals(remoteSocketAddress)) {
                        byte[] respData = readNetResp.getData();
                        try {
                            Object response = pendingReq.getBytesToResponseTransformer().create(respData);
                            LOG.debug("Parsed the following response to {} from {}", response, respData);
                            pendingReq.addResponse(response);
                            
                            if (!broadcastBehaviour) {
                                LOG.debug("Removed request from send queue");
                                it.remove();
                            }
                        } catch (RuntimeException e) {
                            LOG.error("Encountered error while parsing response from {}", respData, e);
                        }
                        
                        break;
                    }
                }
            }
        }

        
        // Destroy UDP sockets
        for (int id : addressToSocketId.values()) {
            networkBus.send(new CloseNetworkRequest(id));
        }
        
        LOG.debug("Completed udp requests {}", reqs);
    }
    
    /**
     * UDP request object.
     */
    public static final class UdpRequest {
        private final InetAddress sourceAddress;
        private final InetSocketAddress destinationSocketAddress;
        private final Object request;
        private final List<Object> responses;
        private final RequestToBytesTransformer requestToBytesTransformer;
        private final BytesToResponseTransformer bytesToResponseTransformer;
        private Object other;

        /**
         * Construct a {@link UdpRequest} object.
         * @param sourceAddress source address
         * @param destinationSocketAddress destination socket address
         * @param request request object
         * @param requestToBytesTransformer request to byte buffer transformer
         * @param bytesToResponseTransformer bytes to response transformer
         * @throws NullPointerException if any argument is {@code null}
         */
        public UdpRequest(InetAddress sourceAddress, InetSocketAddress destinationSocketAddress, Object request,
                RequestToBytesTransformer requestToBytesTransformer, BytesToResponseTransformer bytesToResponseTransformer) {
            Validate.notNull(sourceAddress);
            Validate.notNull(destinationSocketAddress);
            Validate.notNull(request);
            Validate.notNull(requestToBytesTransformer);
            Validate.notNull(bytesToResponseTransformer);
            this.sourceAddress = sourceAddress;
            this.destinationSocketAddress = destinationSocketAddress;
            this.request = request;
            this.responses = new LinkedList<>();
            this.requestToBytesTransformer = requestToBytesTransformer;
            this.bytesToResponseTransformer = bytesToResponseTransformer;
        }

        /**
         * Get source address.
         * @return source address
         */
        public InetAddress getSourceAddress() {
            return sourceAddress;
        }

        /**
         * Get destination socket address.
         * @return destination socket address
         */
        public InetSocketAddress getDestinationSocketAddress() {
            return destinationSocketAddress;
        }

        /**
         * Get request to bytes transformer.
         * @return request to bytes transformer
         */
        public RequestToBytesTransformer getRequestToBytesTransformer() {
            return requestToBytesTransformer;
        }

        /**
         * Gets bytes to response transformer.
         * @return bytes to response transformer
         */
        public BytesToResponseTransformer getBytesToResponseTransformer() {
            return bytesToResponseTransformer;
        }

        /**
         * Get request object.
         * @return request
         */
        public Object getRequest() {
            return request;
        }

        /**
         * Get first response object.
         * @return response (or {@code null} if no parse-able response was received)
         */
        public Object getResponse() {
            return responses.isEmpty() ? null : responses.get(0);
        }

        /**
         * Get all response objects.
         * @return all response objects
         */
        public List<Object> getResponses() {
            return new ArrayList<>(responses);
        }

        void addResponse(Object response) {
            Validate.notNull(response);
            this.responses.add(response);
        }

        /**
         * Get extra field.
         * @return extra
         */
        public Object getOther() {
            return other;
        }

        /**
         * Set extra field.
         * @param other extra
         */
        public void setOther(Object other) {
            this.other = other;
        }

        @Override
        public String toString() {
            return "UdpRequest{" + "sourceAddress=" + sourceAddress + ", destinationSocketAddress=" + destinationSocketAddress
                    + ", request=" + request + ", responses=" + responses + ", requestToBytesTransformer=" + requestToBytesTransformer
                    + ", bytesToResponseTransformer=" + bytesToResponseTransformer + ", other=" + other + '}';
        }
    }

    /**
     * Transforms a request object to bytes.
     */    
    public interface RequestToBytesTransformer {
        /**
         * Construct a byte array from a request object.
         * @param request request object
         * @return converted byte array
         * @throws IllegalArgumentException on failure to convert
         * @throws NullPointerException if any argument is {@code null}
         */
        byte[] create(Object request);
    }
    
    /**
     * Transforms bytes to response object.
     */
    public interface BytesToResponseTransformer {
        /**
         * Parses a response object from a byte array.
         * @param buffer byte array to convert
         * @return response object
         * @throws IllegalArgumentException on failure to parse buffer
         * @throws NullPointerException if any argument is {@code null}
         */
        Object create(byte[] buffer);
    }
}
