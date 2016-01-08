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
package com.offbynull.portmapper.upnpigd;

import com.offbynull.portmapper.common.BasicBus;
import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkRequest;
import com.offbynull.portmapper.io.messages.ErrorNetworkResponse;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesRequest;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesResponse;
import com.offbynull.portmapper.io.messages.IdentifiableErrorNetworkResponse;
import com.offbynull.portmapper.io.messages.ReadTcpNetworkNotification;
import com.offbynull.portmapper.io.messages.ReadUdpNetworkNotification;
import com.offbynull.portmapper.io.messages.WriteTcpNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteUdpNetworkRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.Validate;

final class InternalUtils {
    private InternalUtils() {
        
    }

    static Set<InetAddress> getLocalIpAddresses(Bus networkBus) throws InterruptedException {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Get local IP addresses
        networkBus.send(new GetLocalIpAddressesRequest(selfBus));
        GetLocalIpAddressesResponse localIpsResp = (GetLocalIpAddressesResponse) queue.poll(1000L, TimeUnit.MILLISECONDS);
        
        return localIpsResp.getLocalAddresses();
    }

    static void performHttpRequests(Bus networkBus, Collection<HttpRequest> reqs, long timeout) throws InterruptedException,
            UnknownHostException, IOException {

        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);
        

        // Create sockets
        Map<Integer, HttpRequest> sockets = new HashMap<>();
        Map<Integer, ByteArrayOutputStream> readBuffers = new HashMap<>();
        long endCreateTime = System.currentTimeMillis() + 1000L; // 1 second to create all sockets
        next:
        for (HttpRequest req : reqs) {
            if (!"http".equalsIgnoreCase(req.location.getProtocol())) {
                // not http -- is it https? we don't support that yet
                // TODO LOG SOMETHING HERE
                continue;
            }

            InetAddress destinationAddress = InetAddress.getByName(req.location.getHost());
            int destinationPort = req.location.getPort();

            networkBus.send(new CreateTcpSocketNetworkRequest(selfBus, req.sourceAddress, destinationAddress, destinationPort));
            
            int id;
            while (true) {
                long sleepTime = endCreateTime - System.currentTimeMillis();
                if (sleepTime <= 0L) {
                    break next;
                }

                Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                if (resp instanceof ErrorNetworkResponse) {
                    // create socket failed, so skip this request
                    continue next;
                } else if (resp instanceof CreateTcpSocketNetworkResponse) {
                    // create socket success
                    id = ((CreateTcpSocketNetworkResponse) resp).getId();
                    break;
                } else if (resp instanceof IdentifiableErrorNetworkResponse) {
                    // likely one of the previously created sockets failed to connect -- remove the previously added socket and move on
                    int removeId = ((IdentifiableErrorNetworkResponse) resp).getId();
                    sockets.remove(removeId);
                    readBuffers.remove(removeId);
                }

                // unrecognized response/notification, keep reading from queue until we have something we recognize
            }

            // Even though the TCP socket hasn't connected yet, add outgoing data (it'll be sent on connect
            sockets.put(id, req);
            readBuffers.put(id, new ByteArrayOutputStream());
            networkBus.send(new WriteTcpNetworkRequest(id, req.sendData));
        }


        // Read data from sockets
        long remainingTime = System.currentTimeMillis() + timeout;
        Set<Integer> activeSocketIds = new HashSet<>(sockets.keySet());
        while (!activeSocketIds.isEmpty()) {
            long sleepTime = remainingTime - System.currentTimeMillis();
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
                baos.write(readResp.getData());
            } else if (resp instanceof IdentifiableErrorNetworkResponse) {
                // On error, remove socket from active set (server likely closed the socket)
                IdentifiableErrorNetworkResponse errorResp = (IdentifiableErrorNetworkResponse) resp;
                int id = errorResp.getId();
                
                activeSocketIds.remove(id);
            }
        }


        // Issue socket closes
        for (int id : sockets.keySet()) {
            networkBus.send(new DestroySocketNetworkRequest(id));
        }


        // Process responses
        for (Map.Entry<Integer, ByteArrayOutputStream> entry : readBuffers.entrySet()) {
            int id = entry.getKey();
            HttpRequest req = sockets.get(id);

            byte[] respData = entry.getValue().toByteArray();
            if (respData.length > 0) {
                req.respData = respData;
            }
        }
    }

    static void performUdpQueries(Bus networkBus, Collection<UdpRequest> reqs, Queue<Long> retryDurations)
            throws InterruptedException {
        
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        
        // Get unique source addresses and create socket for each one
        BidiMap<InetAddress, Integer> addressToId = new DualHashBidiMap<>(); // source address to socket id
        BidiMap<Integer, UdpRequest> idToRequest = new DualHashBidiMap<>(); // source address to socket id

        long endCreateTime = System.currentTimeMillis() + 1000L; // 1 second to create all sockets
        next:
        for (UdpRequest req : reqs) {
            if (addressToId.containsKey(req.sourceAddress)) {
                continue;
            }

            networkBus.send(new CreateUdpSocketNetworkRequest(selfBus, req.sourceAddress));

            Object createResp;
            while (true) {
                long sleepTime = endCreateTime - System.currentTimeMillis();
                Validate.validState(sleepTime > 0, "Failed to create all UDP sockets in time");

                createResp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                if (createResp instanceof ErrorNetworkResponse) {
                    // create socket failed, so skip this request
                    continue next;
                } else if (createResp instanceof CreateUdpSocketNetworkResponse) {
                    // create socket success, store the udp socket info
                    break;
                }

                // unrecognized response/notification, keep reading from queue until we have something we recognize
            }

            int id = ((CreateUdpSocketNetworkResponse) createResp).getId();
            addressToId.put(req.sourceAddress, id);
            idToRequest.put(id, req);
        }

        
        // Send requests
        Queue<Long> remainingRetryDurations = new LinkedList<>(retryDurations);
        while (!idToRequest.isEmpty() && !remainingRetryDurations.isEmpty()) {
            // Send requests to whoever hasn't responded yet
            for (UdpRequest req : idToRequest.values()) {
                int id = addressToId.get(req.sourceAddress);

                try {
                    networkBus.send(new WriteUdpNetworkRequest(id, req.destinationSocketAddress, req.sendData));
                } catch (RuntimeException re) {
                    // do nothing -- just skip
                }
            }

            // Wait for responses
            long retryTime = remainingRetryDurations.poll();
            long remainingTime = System.currentTimeMillis() + retryTime;
            while (true) {
                long sleepTime = remainingTime - System.currentTimeMillis();
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

                UdpRequest req = idToRequest.get(id);
                if (req == null) {
                    // a secondary response came in after the first one was already processed, so skip
                    continue;
                }

                // leave this out for now
//                if (!req.destinationSocketAddress.equals(readNetResp.getSocketAddress())) {
//                    // skip if we're recving from someone other than who we sent to
//                    continue;
//                }
                req.respData = readNetResp.getData();
                idToRequest.remove(id);
            }
        }

        
        // Destroy UDP sockets
        for (int id : addressToId.values()) {
            networkBus.send(new DestroySocketNetworkRequest(id));
        }
    }
    
    
    static final class HttpRequest {

        Object other;
        InetAddress sourceAddress;
        URL location;
        byte[] sendData;
        byte[] respData;
    }

    static final class UdpRequest {

        Object other;
        InetAddress sourceAddress;
        InetSocketAddress destinationSocketAddress;
        byte[] sendData;
        byte[] respData;
    }
}
