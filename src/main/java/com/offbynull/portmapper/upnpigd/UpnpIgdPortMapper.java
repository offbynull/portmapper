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
import com.offbynull.portmapper.upnpigd.messages.UpnpIgdHttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MultiValuedMap;
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
        Map<InetAddress, ServiceDiscoveryUpnpIgdResponse> probeResponses = discoverDevices(networkBus);
        
        
        // Get root XMLs
        Collection<HttpRequest> rootRequests = new ArrayList<>(probeResponses.size());
        for (Entry<InetAddress, ServiceDiscoveryUpnpIgdResponse> entry : probeResponses.entrySet()) {
            HttpRequest req = new HttpRequest();
            
            ProbeResult other = new ProbeResult();
            other.source = entry.getKey();
            other.location = entry.getValue().getLocation();
            other.serverName = entry.getValue().getServer();
            other.serviceType = entry.getValue().getServiceType();
            
            req.other = other;
            req.sourceAddress = other.source;
            req.location = other.location;
            req.sendValue = new RootUpnpIgdRequest(other.location.getAuthority(), other.location.getFile());
            req.sendData = ((RootUpnpIgdRequest) req.sendValue).dump();
            rootRequests.add(req);
        }
        queryHttp(networkBus, rootRequests);
        
        
        // Extract service locations from root XMLs + get service descriptions
        Collection<HttpRequest> serviceDescRequests = new ArrayList<>(rootRequests.size());
        for (HttpRequest rootRequest : rootRequests) {
            RootUpnpIgdResponse rootResp;
            try {
                rootResp = new RootUpnpIgdResponse(rootRequest.location, rootRequest.respData);
                rootRequest.respValue = rootResp;
            } catch (IllegalArgumentException iae) {
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
                req.sendValue = new ServiceDescriptionUpnpIgdRequest(scpdUrl.getAuthority(), scpdUrl.getFile()); 
                req.sendData = ((ServiceDescriptionUpnpIgdRequest) req.sendValue).dump();
                serviceDescRequests.add(req);
            }
        }
        queryHttp(networkBus, serviceDescRequests);
        
        
        // Get service descriptions
        Set<UpnpIgdPortMapper> ret = new HashSet<>();
        for (HttpRequest serviceDescRequest : serviceDescRequests) {
            ServiceDescriptionUpnpIgdResponse serviceDescResp;
            try {
                serviceDescResp = new ServiceDescriptionUpnpIgdResponse(serviceDescRequest.respData);
                serviceDescRequest.respValue = serviceDescResp;
            } catch (IllegalArgumentException iae) {
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
            }
        }
        
        return ret;
    }

    private static Map<InetAddress, ServiceDiscoveryUpnpIgdResponse> discoverDevices(Bus networkBus) throws InterruptedException {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Get local IP addresses
        networkBus.send(new GetLocalIpAddressesRequest(selfBus));
        GetLocalIpAddressesResponse localIpsResp = (GetLocalIpAddressesResponse) queue.take();

        // Create UDP sockets and send discovery messages
        Map<Integer, InetAddress> udpSocketIds = new HashMap<>(); // id to src address
        Map<InetAddress, ServiceDiscoveryUpnpIgdResponse> probeResponses = new HashMap<>(); // source address to response
        for (InetAddress sourceAddress : localIpsResp.getLocalAddresses()) {
            System.out.println("sending from " + sourceAddress);
            try {
                networkBus.send(new CreateUdpSocketNetworkRequest(selfBus, sourceAddress));
                Object createResp = queue.take();

                int id = ((CreateUdpSocketNetworkResponse) createResp).getId();

                udpSocketIds.put(id, sourceAddress);

                UpnpIgdHttpRequest req;
                if (sourceAddress instanceof Inet4Address) {
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV4, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpNetworkRequest(
                            id,
                            ProbeDeviceType.IPV4.getMulticastSocketAddress(),
                            req.dump()));
                } else if (sourceAddress instanceof Inet6Address) {
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_LINK_LOCAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpNetworkRequest(
                            id,
                            ProbeDeviceType.IPV6_LINK_LOCAL.getMulticastSocketAddress(),
                            req.dump()));
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_SITE_LOCAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpNetworkRequest(
                            id,
                            ProbeDeviceType.IPV6_SITE_LOCAL.getMulticastSocketAddress(),
                            req.dump()));
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_ORGANIZATION_LOCAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpNetworkRequest(
                            id,
                            ProbeDeviceType.IPV6_ORGANIZATION_LOCAL.getMulticastSocketAddress(),
                            req.dump()));
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_GLOBAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpNetworkRequest(
                            id,
                            ProbeDeviceType.IPV6_GLOBAL.getMulticastSocketAddress(),
                            req.dump()));
                } else {
                    throw new IllegalStateException();
                }
            } catch (RuntimeException re) {
                // do nothing -- just skip
            }
        }

        // Read responses to discovery message
        long remainingTime = System.currentTimeMillis() + 10000L;
        while (true) {
            long sleepTime = remainingTime - System.currentTimeMillis();
            if (sleepTime < 0L) {
                break;
            }

            Object netResp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
            if (netResp == null) {
                break;
            } else if (!(netResp instanceof ReadUdpNetworkNotification)) {
                // We only care about responses -- message could be successful write or error
                continue;
            }

            ReadUdpNetworkNotification readNetResp = (ReadUdpNetworkNotification) netResp;
            int id = readNetResp.getId();

            try {
                ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(readNetResp.getData());
                InetAddress sourceAddress = udpSocketIds.get(id);
                probeResponses.put(sourceAddress, resp);
            } catch (IllegalArgumentException iae) {
                // if invalid, do nothing -- just skip over
            }
        }

        // Destroy UDP sockets
        for (int id : udpSocketIds.keySet()) {
            networkBus.send(new DestroySocketNetworkRequest(id));
        }
        // don't worry about checking responses -- we abandon the queue we created in this method

        return probeResponses;
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
        private Object sendValue;
        private byte[] respData;
        private Object respValue;
    }
    
    private static void queryHttp(Bus networkBus, Collection<HttpRequest> reqs) throws InterruptedException, UnknownHostException,
            IOException {

        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        long remainingTime = System.currentTimeMillis() + 10000L;
        
        
        // Create sockets
        Map<Integer, HttpRequest> sockets = new HashMap<>();
        for (HttpRequest req : reqs) {
            long sleepTime = remainingTime - System.currentTimeMillis();
            if (sleepTime < 0L) {
                break;
            }
            
            if (!"http".equalsIgnoreCase(req.location.getProtocol())) {
                // not http -- is it https? we don't support that yet
                // TODO LOG SOMETHING HERE
                continue;
            }

            InetAddress destinationAddress = InetAddress.getByName(req.location.getHost());
            int destinationPort = req.location.getPort();

            networkBus.send(new CreateTcpSocketNetworkRequest(selfBus, req.sourceAddress, destinationAddress, destinationPort));
            Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);

            if (resp instanceof CreateTcpSocketNetworkResponse) {
                CreateTcpSocketNetworkResponse createResp = (CreateTcpSocketNetworkResponse) resp;
                int id = createResp.getId();

                sockets.put(id, req);
            } else if (resp instanceof ErrorNetworkResponse) {
                // there was an error creating the socket -- it could be anything that caused this (e.g. an interface not enabled for
                // writing), so just ignore this socket and move on
            } else {
                throw new IllegalStateException("" + resp); // should never happen
            }
        }

        
        // Handle connections
        Map<Integer, ByteArrayOutputStream> readBuffers = new HashMap<>();
        while (true) {
            long sleepTime = remainingTime - System.currentTimeMillis();
            if (sleepTime < 0L) {
                break;
            }

            Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);

            if (resp instanceof ConnectedTcpNetworkResponse) {
                // On connect, create nessecary buffers and push out request
                ConnectedTcpNetworkResponse connectResp = (ConnectedTcpNetworkResponse) resp;
                int id = connectResp.getId();

                Validate.validState(!readBuffers.containsKey(id)); // sanity check -- should never happen
                readBuffers.put(id, new ByteArrayOutputStream());
                HttpRequest req = sockets.get(id);
                Validate.validState(req != null); // sanity check -- should never happen
                
                networkBus.send(new WriteTcpNetworkRequest(id, req.sendData));
            } else if (resp instanceof ReadTcpNetworkNotification) {
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
        MultiValuedMap<Entry<InetAddress, URL>, byte[]> responses = new ArrayListValuedHashMap<>();
        for (Entry<Integer, ByteArrayOutputStream> entry : readBuffers.entrySet()) {
            int id = entry.getKey();
            HttpRequest req = sockets.get(id);
            
            Validate.validState(req != null); // sanity check

            byte[] respData = entry.getValue().toByteArray();
            req.respData = respData;
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
