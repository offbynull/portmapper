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

import com.offbynull.portmapper.PortMapper;
import com.offbynull.portmapper.common.BasicBus;
import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.io.messages.ConnectedTcpNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkRequest;
import com.offbynull.portmapper.io.messages.ErrorNetworkResponse;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesRequest;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesResponse;
import com.offbynull.portmapper.io.messages.ReadTcpNetworkNotification;
import com.offbynull.portmapper.io.messages.ReadUdpNetworkNotification;
import com.offbynull.portmapper.io.messages.WriteTcpNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteUdpNetworkRequest;
import com.offbynull.portmapper.upnpigd.messages.RootUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.messages.RootUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.messages.RootUpnpIgdResponse.ServiceReference;
import com.offbynull.portmapper.upnpigd.messages.ServiceDescriptionUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.messages.ServiceDescriptionUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.messages.ServiceDescriptionUpnpIgdResponse.IdentifiedService;
import com.offbynull.portmapper.upnpigd.messages.ServiceDescriptionUpnpIgdResponse.ServiceType;
import com.offbynull.portmapper.upnpigd.messages.ServiceDiscoveryUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.messages.ServiceDiscoveryUpnpIgdRequest.ProbeDeviceType;
import com.offbynull.portmapper.upnpigd.messages.ServiceDiscoveryUpnpIgdResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;

/**
 * UPNP-IGD {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
abstract class UpnpIgdPortMapper implements PortMapper {

    private final InetAddress internalAddress;
    private final URL controlUrl;
    private final String serverName;
    private final String serviceType;
    private final Range<Long> externalPortRange;
    private final Range<Long> leaseDurationRange;

    public static Set<UpnpIgdPortMapper> identify(Bus networkBus) throws InterruptedException, IOException {
        Validate.notNull(networkBus);

        // Probe for devices -- for each device found, query the device
        Set<InetAddress> sourceAddresses = getLocalIpAddresses(networkBus);
        Collection<UdpRequest> discoveryRequests = new LinkedList<>();
        for (InetAddress sourceAddress : sourceAddresses) {
            if (sourceAddress instanceof Inet4Address) {
                UdpRequest req = new UdpRequest();
                req.sourceAddress = sourceAddress;
                req.destinationSocketAddress = ProbeDeviceType.IPV4.getMulticastSocketAddress();
                req.sendData = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV4, null, 3, "ssdp:all").dump();
                discoveryRequests.add(req);
            } else if (sourceAddress instanceof Inet6Address) {
                UdpRequest v6LocalReq = new UdpRequest();
                v6LocalReq.sourceAddress = sourceAddress;
                v6LocalReq.destinationSocketAddress = ProbeDeviceType.IPV6_LINK_LOCAL.getMulticastSocketAddress();
                v6LocalReq.sendData = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_LINK_LOCAL, null, 3, "ssdp:all").dump();
                discoveryRequests.add(v6LocalReq);

                UdpRequest v6SiteReq = new UdpRequest();
                v6SiteReq.sourceAddress = sourceAddress;
                v6SiteReq.destinationSocketAddress = ProbeDeviceType.IPV6_SITE_LOCAL.getMulticastSocketAddress();
                v6SiteReq.sendData = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_SITE_LOCAL, null, 3, "ssdp:all").dump();
                discoveryRequests.add(v6SiteReq);

                UdpRequest v6OrgReq = new UdpRequest();
                v6OrgReq.sourceAddress = sourceAddress;
                v6OrgReq.destinationSocketAddress = ProbeDeviceType.IPV6_ORGANIZATION_LOCAL.getMulticastSocketAddress();
                v6OrgReq.sendData = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_ORGANIZATION_LOCAL, null, 3, "ssdp:all").dump();
                discoveryRequests.add(v6OrgReq);

                UdpRequest v6GlobalReq = new UdpRequest();
                v6GlobalReq.sourceAddress = sourceAddress;
                v6GlobalReq.destinationSocketAddress = ProbeDeviceType.IPV6_GLOBAL.getMulticastSocketAddress();
                v6GlobalReq.sendData = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_GLOBAL, null, 3, "ssdp:all").dump();
                discoveryRequests.add(v6GlobalReq);
            } else {
                throw new IllegalStateException();
            }
        }
        performUdpQueries(networkBus, discoveryRequests, new LinkedList<>(Arrays.asList(1000L, 1000L, 1000L, 1000L, 1000L)));


        // Get root XMLs
        Collection<HttpRequest> rootRequests = new ArrayList<>(discoveryRequests.size());
        for (UdpRequest discoveryReq : discoveryRequests) {
            ServiceDiscoveryUpnpIgdResponse discoveryResp;
            try {
                discoveryResp = new ServiceDiscoveryUpnpIgdResponse(discoveryReq.respData);
            } catch (RuntimeException iae) {
                // failed to parse, so skip to next
                continue;
            }
            HttpRequest req = new HttpRequest();

            ProbeResult other = new ProbeResult();
            other.source = discoveryReq.sourceAddress;
            other.location = discoveryResp.getLocation();
            other.serverName = discoveryResp.getServer();
            other.serviceType = discoveryResp.getServiceType();

            req.other = other;
            req.sourceAddress = other.source;
            req.location = other.location;
            req.sendData = new RootUpnpIgdRequest(other.location.getAuthority(), other.location.getFile()).dump();
            rootRequests.add(req);
        }
        performHttpRequests(networkBus, rootRequests, 5000L);

        // Extract service locations from root XMLs + get service descriptions
        Collection<HttpRequest> serviceDescRequests = new ArrayList<>(rootRequests.size());
        for (HttpRequest rootRequest : rootRequests) {
            RootUpnpIgdResponse rootResp;
            try {
                rootResp = new RootUpnpIgdResponse(rootRequest.location, rootRequest.respData);
            } catch (RuntimeException iae) {
                // failed to parse, so skip to next
                continue;
            }

            for (ServiceReference serviceReference : rootResp.getServices()) {
                URL scpdUrl = serviceReference.getScpdUrl();

                RootRequestResult other = new RootRequestResult();
                other.probeResult = (ProbeResult) rootRequest.other;
                other.serviceReference = serviceReference;

                HttpRequest req = new HttpRequest();
                req.other = other;
                req.sourceAddress = rootRequest.sourceAddress;
                req.location = scpdUrl;
                req.sendData = new ServiceDescriptionUpnpIgdRequest(scpdUrl.getAuthority(), scpdUrl.getFile()).dump();
                serviceDescRequests.add(req);
            }
        }
        performHttpRequests(networkBus, serviceDescRequests, 10000L);

        // Get service descriptions
        Set<UpnpIgdPortMapper> ret = new HashSet<>();
        for (HttpRequest serviceDescRequest : serviceDescRequests) {
            ServiceDescriptionUpnpIgdResponse serviceDescResp;
            try {
                serviceDescResp = new ServiceDescriptionUpnpIgdResponse(serviceDescRequest.respData);
            } catch (RuntimeException iae) {
                // failed to parse, so skip to next
                continue;
            }

            RootRequestResult rootReqRes = (RootRequestResult) serviceDescRequest.other;
            for (Entry<ServiceType, IdentifiedService> e : serviceDescResp.getIdentifiedServices().entrySet()) {
                ServiceType serviceType = e.getKey();
                IdentifiedService identifiedService = e.getValue();

                UpnpIgdPortMapper upnpIgdPortMapper;
                switch (serviceType) {
                    case PORT_MAPPER:
                        upnpIgdPortMapper = new PortMapperUpnpIgdPortMapper(
                                serviceDescRequest.sourceAddress,
                                rootReqRes.serviceReference.getControlUrl(),
                                rootReqRes.probeResult.serverName,
                                rootReqRes.probeResult.serviceType,
                                identifiedService.getExternalPortRange(),
                                identifiedService.getLeaseDurationRange());
                        break;
                    case FIREWALL:
                        upnpIgdPortMapper = new FirewallUpnpIgdPortMapper(
                                serviceDescRequest.sourceAddress,
                                rootReqRes.serviceReference.getControlUrl(),
                                rootReqRes.probeResult.serverName,
                                rootReqRes.probeResult.serviceType,
                                identifiedService.getExternalPortRange(),
                                identifiedService.getLeaseDurationRange());
                        break;
                    default:
                        throw new IllegalStateException(); // should never happen
                }

                ret.add(upnpIgdPortMapper);
            }
        }

        return ret;
    }


    private static final class ProbeResult {

        private InetAddress source;
        private URL location;
        private String serverName;
        private String serviceType;
    }

    private static final class RootRequestResult {

        private ProbeResult probeResult;
        private ServiceReference serviceReference;
    }

    private static final class HttpRequest {

        private Object other;
        private InetAddress sourceAddress;
        private URL location;
        private byte[] sendData;
        private byte[] respData;
    }

    private static final class UdpRequest {

        private Object other;
        private InetAddress sourceAddress;
        private InetSocketAddress destinationSocketAddress;
        private byte[] sendData;
        private byte[] respData;
    }

    private static Set<InetAddress> getLocalIpAddresses(Bus networkBus) throws InterruptedException {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Get local IP addresses
        networkBus.send(new GetLocalIpAddressesRequest(selfBus));
        GetLocalIpAddressesResponse localIpsResp = (GetLocalIpAddressesResponse) queue.poll(1000L, TimeUnit.MILLISECONDS);
        
        return localIpsResp.getLocalAddresses();
    }

    private static void performHttpRequests(Bus networkBus, Collection<HttpRequest> reqs, long timeout) throws InterruptedException,
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
                Validate.validState(sleepTime > 0, "Failed to create all TCP sockets in time");

                Object createResp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                if (createResp instanceof ErrorNetworkResponse) {
                    // create socket failed, so skip this request
                    continue next;
                } else if (createResp instanceof CreateTcpSocketNetworkResponse) {
                    // create socket success
                    id = ((CreateTcpSocketNetworkResponse) createResp).getId();
                    break;
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
        
        while (true) {
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
            }
        }


        // Issue socket closes
        for (int id : sockets.keySet()) {
            networkBus.send(new DestroySocketNetworkRequest(id));
        }


        // Process responses
        for (Entry<Integer, ByteArrayOutputStream> entry : readBuffers.entrySet()) {
            int id = entry.getKey();
            HttpRequest req = sockets.get(id);

            byte[] respData = entry.getValue().toByteArray();
            req.respData = respData;
        }
    }

    private static void performUdpQueries(Bus networkBus, Collection<UdpRequest> reqs, Queue<Long> retryDurations)
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

    protected UpnpIgdPortMapper(InetAddress internalAddress, URL controlUrl, String serverName, String serviceType,
            Range<Long> externalPortRange, Range<Long> leaseDurationRange) {
        Validate.notNull(internalAddress);
        Validate.notNull(controlUrl);
//        Validate.notNull(serverName); // can be null
        Validate.notNull(serviceType);
        Validate.notNull(externalPortRange);
        Validate.notNull(leaseDurationRange);
        this.internalAddress = internalAddress;
        this.controlUrl = controlUrl;
        this.serverName = serverName;
        this.serviceType = serviceType;
        this.externalPortRange = externalPortRange;
        this.leaseDurationRange = leaseDurationRange;
    }

    protected final InetAddress getInternalAddress() {
        return internalAddress;
    }

    protected final URL getControlUrl() {
        return controlUrl;
    }

    protected final String getServerName() {
        return serverName;
    }

    protected final String getServiceType() {
        return serviceType;
    }

    protected final Range<Long> getExternalPortRange() {
        return externalPortRange;
    }

    protected final Range<Long> getLeaseDurationRange() {
        return leaseDurationRange;
    }
}
