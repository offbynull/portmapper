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
package com.offbynull.portmapper.mappers.upnpigd;

import com.offbynull.portmapper.gateway.BasicBus;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.helpers.NetworkUtils;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateTcpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateTcpNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.CloseNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.IdentifiableErrorNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.IdentifiableNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadClosedTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteTcpNetworkRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.UpnpIgdHttpRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.UpnpIgdHttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InternalUtils {
    private static final Logger LOG = LoggerFactory.getLogger(InternalUtils.class);
    
    private InternalUtils() {
        
    }
    
    // avoids flooding a single server with a bunch of requests -- does requests to each server in batches of no more than 3
    static void performBatchedHttpRequests(Bus networkBus, Collection<HttpRequest> reqs, long ... attemptDurations)
            throws InterruptedException {
        ArrayListValuedHashMap<String, HttpRequest> ret = new ArrayListValuedHashMap<>();
        for (HttpRequest req : reqs) {
            String authority = req.getLocation().getAuthority();
            ret.put(authority, req);
        }
        
        List<List<HttpRequest>> batches = new LinkedList<>();
        int counter = 0;
        while (true) {
            List<HttpRequest> batch = new LinkedList<>();
            int start = counter * 3;
            int end = (counter + 1) * 3;
            
            for (String serverAddress : ret.keySet()) {
                List<HttpRequest> serverRequests = ret.get(serverAddress);
                if (start >= serverRequests.size()) {
                    continue;
                }
                
                int size = serverRequests.size();
                int actualEnd = Math.min(end, size);
                
                batch.addAll(serverRequests.subList(start, actualEnd));
            }
            
            if (batch.isEmpty()) {
                break;
            }
            
            batches.add(batch);
            counter++;
        }
        
        for (List<HttpRequest> batch : batches) {
            performHttpRequests(networkBus, batch, attemptDurations);
        }
    }
    
    static void performHttpRequests(Bus networkBus, Collection<HttpRequest> reqs, long ... attemptDurations) throws InterruptedException {
        LOG.debug("Performing http requests {} with durations ", reqs, attemptDurations);
        
        Queue<Long> remainingAttemptDurations = new LinkedList<>();
        for (long attemptDuration : attemptDurations) {
            remainingAttemptDurations.add(attemptDuration);
        }
        Set<HttpRequest> remainingReqs = new HashSet<>(reqs);
        while (!remainingAttemptDurations.isEmpty()) {
            LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
            Bus selfBus = new BasicBus(queue);

            // Create sockets
            Map<Integer, HttpRequest> sockets = new HashMap<>();
            Map<Integer, ByteArrayOutputStream> readBuffers = new HashMap<>();
            long endCreateTime = System.currentTimeMillis() + 1000L; // 1 second to create all sockets
            next:
            for (HttpRequest req : remainingReqs) {
                if (!"http".equalsIgnoreCase(req.location.getProtocol())) {
                    // not http -- is it https? we don't support that yet
                    // TODO LOG SOMETHING HERE
                    continue;
                }

                long sleepTime;
                
                
                // Get id
                networkBus.send(new GetNextIdNetworkRequest(selfBus));

                    // read
                int id;
                while (true) {
                    sleepTime = endCreateTime - System.currentTimeMillis();
                    Validate.validState(sleepTime > 0, "Failed to create all UDP sockets in time");

                    Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                    if (resp instanceof GetNextIdNetworkResponse) {
                        id = ((GetNextIdNetworkResponse) resp).getId();
                        break;
                    }
                }
                
                // Create socket
                InetAddress destinationAddress = NetworkUtils.toAddress(req.getLocation().getHost());
                int destinationPort = req.getLocation().getPort();
                networkBus.send(new CreateTcpNetworkRequest(id, selfBus, req.getSourceAddress(), destinationAddress, destinationPort));

                   // read until success or failure
                while (true) {
                    sleepTime = endCreateTime - System.currentTimeMillis();
                    if (sleepTime <= 0L) {
                        break next;
                    }

                    Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                    if (resp instanceof IdentifiableErrorNetworkResponse
                            && ((IdentifiableNetworkResponse) resp).getId() == id) {
                        // create socket failed, so skip this request
                        continue next;
                    } else if (resp instanceof CreateTcpNetworkResponse
                            && ((CreateTcpNetworkResponse) resp).getId() == id) {
                        // create socket success
                        break;
                    }
                }

                // Track socket
                sockets.put(id, req);
                readBuffers.put(id, new ByteArrayOutputStream());
            }

            
            // Send data to sockets
            for (Entry<Integer, HttpRequest> entry : sockets.entrySet()) {
                int id = entry.getKey();
                HttpRequest req = entry.getValue();
                networkBus.send(new WriteTcpNetworkRequest(id, req.getSendMsg().dump()));
            }


            // Read data from sockets
            long timeout = remainingAttemptDurations.poll();
            long endTime = System.currentTimeMillis() + timeout;
            Set<Integer> activeSocketIds = new HashSet<>(sockets.keySet());
            while (!activeSocketIds.isEmpty()) {
                long sleepTime = endTime - System.currentTimeMillis();
                if (sleepTime <= 0L) {
                    break;
                }

                Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);

                if (resp instanceof ReadTcpNetworkNotification) {
                    // On read, put in to readBuffer
                    ReadTcpNetworkNotification readResp = (ReadTcpNetworkNotification) resp;
                    int id = readResp.getId();

                    ByteArrayOutputStream baos = readBuffers.get(id);
                    Validate.validState(baos != null); // sanity check -- should never happen
                    try {
                        baos.write(readResp.getData());
                    } catch (IOException ioe) {
                        throw new IllegalStateException(); // should never happen
                    }
                } else if (resp instanceof IdentifiableErrorNetworkResponse) {
                    // On error, remove socket from active set
                    IdentifiableErrorNetworkResponse errorResp = (IdentifiableErrorNetworkResponse) resp;
                    int id = errorResp.getId();

                    activeSocketIds.remove(id);
                } else if (resp instanceof ReadClosedTcpNetworkNotification) {
                    // On no more read, remove socket from active set
                    ReadClosedTcpNetworkNotification closedResp = (ReadClosedTcpNetworkNotification) resp;
                    int id = closedResp.getId();

                    activeSocketIds.remove(id);
                }
            }


            // Issue socket closes
            for (int id : sockets.keySet()) {
                networkBus.send(new CloseNetworkRequest(id));
            }


            // Process responses
            for (Entry<Integer, ByteArrayOutputStream> entry : readBuffers.entrySet()) {
                int id = entry.getKey();
                HttpRequest req = sockets.get(id);

                byte[] respData = entry.getValue().toByteArray();
                try {
                    req.setRespMsg(req.getRespCreator().create(respData));
                    remainingReqs.remove(req);
                } catch (RuntimeException e) {
                    // do nothing
                }
            }
        }
        
        LOG.debug("Completed http requests {}", reqs);
    }
    
    
    static final class HttpRequest {
        private Object other;
        private InetAddress sourceAddress;
        private URL location;
        private UpnpIgdHttpRequest sendMsg;
        private UpnpIgdHttpResponse respMsg;
        private ResponseCreator respCreator;

        Object getOther() {
            return other;
        }

        void setOther(Object other) {
            this.other = other;
        }

        InetAddress getSourceAddress() {
            return sourceAddress;
        }

        void setSourceAddress(InetAddress sourceAddress) {
            this.sourceAddress = sourceAddress;
        }

        URL getLocation() {
            return location;
        }

        void setLocation(URL location) {
            this.location = location;
        }

        UpnpIgdHttpRequest getSendMsg() {
            return sendMsg;
        }

        void setSendMsg(UpnpIgdHttpRequest sendMsg) {
            this.sendMsg = sendMsg;
        }

        UpnpIgdHttpResponse getRespMsg() {
            return respMsg;
        }

        void setRespMsg(UpnpIgdHttpResponse respMsg) {
            this.respMsg = respMsg;
        }

        ResponseCreator getRespCreator() {
            return respCreator;
        }

        void setRespCreator(ResponseCreator respCreator) {
            this.respCreator = respCreator;
        }

        @Override
        public String toString() {
            return "HttpRequest{" + "other=" + other + ", sourceAddress=" + sourceAddress + ", location=" + location + ", sendMsg="
                    + sendMsg + ", respMsg=" + respMsg + ", respCreator=" + respCreator + '}';
        }
        
    }
    
    interface ResponseCreator {
        UpnpIgdHttpResponse create(byte[] buffer);
    }
}
