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
package com.offbynull.portmapper.natpmp;

import com.offbynull.portmapper.BasicBus;
import com.offbynull.portmapper.Bus;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV4;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV6;
import com.offbynull.portmapper.io.network.internalmessages.CloseNetworkRequest;
import com.offbynull.portmapper.io.network.internalmessages.CreateUdpNetworkRequest;
import com.offbynull.portmapper.io.network.internalmessages.CreateUdpNetworkResponse;
import com.offbynull.portmapper.io.network.internalmessages.ErrorNetworkResponse;
import com.offbynull.portmapper.io.network.internalmessages.GetLocalIpAddressesNetworkRequest;
import com.offbynull.portmapper.io.network.internalmessages.GetLocalIpAddressesNetworkResponse;
import com.offbynull.portmapper.io.network.internalmessages.ReadUdpNetworkNotification;
import com.offbynull.portmapper.io.network.internalmessages.WriteUdpNetworkRequest;
import com.offbynull.portmapper.io.process.internalmessages.CloseProcessRequest;
import com.offbynull.portmapper.io.process.internalmessages.CreateProcessRequest;
import com.offbynull.portmapper.io.process.internalmessages.ErrorProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.ExitProcessNotification;
import com.offbynull.portmapper.io.process.internalmessages.IdentifiableErrorProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.ReadProcessNotification;
import com.offbynull.portmapper.io.process.internalmessages.ReadType;
import com.offbynull.portmapper.natpmp.externalmessages.NatPmpRequest;
import com.offbynull.portmapper.natpmp.externalmessages.NatPmpResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

final class InternalUtils {
    private InternalUtils() {
        
    }
    
    static final UnmodifiableSet<InetAddress> PRESET_IPV4_GATEWAY_ADDRESSES;
    static {
        //
        // IPs grabbed from http://www.techspot.com/guides/287-default-router-ip-addresses/ + comments @ 1/10/2016.
        //
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
    
    static Set<InetAddress> convertToAddressSet(List<String> addresses) {
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
    
    static Set<InetAddress> getLocalIpAddresses(Bus networkBus) throws InterruptedException {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Get local IP addresses
        networkBus.send(new GetLocalIpAddressesNetworkRequest(selfBus));
        GetLocalIpAddressesNetworkResponse localIpsResp = (GetLocalIpAddressesNetworkResponse) queue.poll(1000L, TimeUnit.MILLISECONDS);
        
        return localIpsResp.getLocalAddresses();
    }
    
    static Set<String> runCommandline(Bus processBus, RunProcessRequest ... reqs) throws InterruptedException {

        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Create processes
        Map<Integer, ByteArrayOutputStream> readBuffers = new HashMap<>();
        next:
        for (RunProcessRequest req : reqs) {
            processBus.send(
                    new CreateProcessRequest(
                            selfBus,
                            req.executable,
                            req.parameters));
        }

        // Read data from sockets
        long timeout = 10000L;
        long endTime = System.currentTimeMillis() + timeout;
        int runningProcs = reqs.length;
        while (runningProcs > 0) {
            long sleepTime = endTime - System.currentTimeMillis();
            if (sleepTime <= 0L) {
                break;
            }

            Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);

            if (resp instanceof ReadProcessNotification) {
                // On read, put in to readBuffer
                ReadProcessNotification readResp = (ReadProcessNotification) resp;
                if (readResp.getReadType() == ReadType.STDOUT) {
                    Integer id = readResp.getId();

                    ByteArrayOutputStream baos = readBuffers.get(id);
                    if (baos == null) {
                        baos = new ByteArrayOutputStream();
                        readBuffers.put(id, baos);
                    }

                    try {
                        baos.write(readResp.getData());
                    } catch (IOException ioe) {
                        throw new IllegalStateException(); // should never happen
                    }
                }
            } else if (resp instanceof ExitProcessNotification) {
                runningProcs--;
            } else if (resp instanceof IdentifiableErrorProcessResponse) {
                runningProcs--;
            } else if (resp instanceof ErrorProcessResponse) {
                runningProcs--;
            }
        }

        // Issue closes
        for (int id : readBuffers.keySet()) {
            processBus.send(new CloseProcessRequest(id));
        }

        // Process responses
        Set<String> ret = new HashSet<>();
        for (Entry<Integer, ByteArrayOutputStream> entry : readBuffers.entrySet()) {
             ret.add(new String(entry.getValue().toByteArray(), Charset.forName("US-ASCII")));
        }
        
        return ret;
    }
    

    static void performUdpRequests(Bus networkBus, Collection<UdpRequest> reqs, long ... attemptDurations)
            throws InterruptedException {
        
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        
        // Get unique source addresses and create socket for each one
        BidiMap<InetAddress, Integer> addressToSocketId = new DualHashBidiMap<>(); // source address to socket id
        MultiValuedMap<Integer, UdpRequest> socketIdToRequests = new ArrayListValuedHashMap<>(); // source address to requests

        long endCreateTime = System.currentTimeMillis() + 1000L; // 1 second to create all sockets
        next:
        for (UdpRequest req : reqs) {
            if (addressToSocketId.containsKey(req.sourceAddress)) {
                continue;
            }

            networkBus.send(new CreateUdpNetworkRequest(selfBus, req.sourceAddress));

            Object createResp;
            while (true) {
                long sleepTime = endCreateTime - System.currentTimeMillis();
                Validate.validState(sleepTime > 0, "Failed to create all UDP sockets in time");

                createResp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                if (createResp instanceof ErrorNetworkResponse) {
                    // create socket failed, so skip this request
                    continue next;
                } else if (createResp instanceof CreateUdpNetworkResponse) {
                    // create socket success, store the udp socket info
                    break;
                }

                // unrecognized response/notification, keep reading from queue until we have something we recognize
            }

            int id = ((CreateUdpNetworkResponse) createResp).getId();
            addressToSocketId.put(req.sourceAddress, id);
        }
        
        
        
        // Queue up requests to send out
        for (UdpRequest req : reqs) {
            int id = addressToSocketId.get(req.sourceAddress);
            if (req.destinationSocketAddress.getAddress().equals(ZERO_IPV4)
                    || req.destinationSocketAddress.getAddress().equals(ZERO_IPV6)) {
                // skip if 0.0.0.0 or :: -- seems to cause a socketexception if you try to send to these addresses, which in turn causes the
                // networkgateway to close the socket
                continue;
            }
            
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
                int id = addressToSocketId.get(req.sourceAddress);
                networkBus.send(new WriteUdpNetworkRequest(id, req.destinationSocketAddress, req.sendMsg.dump()));
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
                    // timed out
                    continue;
                } else if (!(netResp instanceof ReadUdpNetworkNotification)) {
                    // got a response but it wasn't a read
                    continue;
                }

                ReadUdpNetworkNotification readNetResp = (ReadUdpNetworkNotification) netResp;
                int id = readNetResp.getId();

                InetSocketAddress remoteSocketAddress = readNetResp.getRemoteAddress();
                Iterator<UdpRequest> it = socketIdToRequests.get(id).iterator();
                while (it.hasNext()) {
                    UdpRequest pendingReq = it.next();
                    if (pendingReq.destinationSocketAddress.equals(remoteSocketAddress)) {
                        byte[] respData = readNetResp.getData();
                        try {
                            pendingReq.respMsg = pendingReq.respCreator.create(respData);
                            it.remove();
                        } catch (RuntimeException e) {
                            // do nothing
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
    }
    
    static long[] calculateRetryTimes(int attempts) {
        long[] ret = new long[attempts];
        
        long nextTime = 250L;
        for (int i = 0; i < attempts; i++) {
            ret[i] = nextTime;
            nextTime *= 2;
        }
        
        return ret;
    }

    static final class RunProcessRequest {
        String executable;
        String[] parameters;
        
        RunProcessRequest(String executable, String ... parameters) {
            this.executable = executable;
            this.parameters = parameters;
        }
    }

    static final class UdpRequest {
        Object other;
        InetAddress sourceAddress;
        InetSocketAddress destinationSocketAddress;
        NatPmpRequest sendMsg;
        NatPmpResponse respMsg;
        ResponseCreator respCreator;
    }
    
    interface ResponseCreator {
        NatPmpResponse create(byte[] buffer);
    }
}
