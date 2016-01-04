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
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkRequest;
import com.offbynull.portmapper.io.messages.ErrorNetworkResponse;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesRequest;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesResponse;
import com.offbynull.portmapper.io.messages.NetworkResponse;
import com.offbynull.portmapper.io.messages.ReadTcpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.ReadUdpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteUdpBlockNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteUdpBlockNetworkResponse;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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

    public static Set<UpnpIgdPortMapper> identify(Bus networkBus) throws InterruptedException {
        Validate.notNull(networkBus);

        Set<UpnpIgdPortMapper> ret = new HashSet<>();

        // Probe for devices -- for each device found, query the device
        Map<InetAddress, ServiceDiscoveryUpnpIgdResponse> probeResponses = probe(networkBus);
        for (Entry<InetAddress, ServiceDiscoveryUpnpIgdResponse> probeEntry : probeResponses.entrySet()) {
            InetAddress sourceAddress = probeEntry.getKey();
            ServiceDiscoveryUpnpIgdResponse discoveryResp = probeEntry.getValue();
            RootUpnpIgdResponse rootResp;
            try {
                rootResp = queryDevice(networkBus, sourceAddress, discoveryResp.getLocation());
            } catch (RuntimeException | IOException ioe) {
                continue;
            }

            // For each service found in the device -- query the service description
            for (ServiceReference serviceRef : rootResp.getServices()) {
                ServiceDescriptionUpnpIgdResponse descResp;
                try {
                    descResp = queryService(networkBus, sourceAddress, serviceRef.getScpdUrl());
                } catch (RuntimeException | IOException ioe) {
                    continue;
                }

                // Create a UPnP-IGD port mapper based on the type of service found (should almost always be either IPv4 portmapper or IPv6
                // firewall, not both)
                for (Entry<ServiceType, IdentifiedService> serviceEntry : descResp.getIdentifiedServices().entrySet()) {
                    IdentifiedService identifiedService = serviceEntry.getValue();
                    UpnpIgdPortMapper portMapper;
                    switch (serviceEntry.getKey()) {
                        case FIREWALL:
                            portMapper = new FirewallUpnpIgdPortMapper(
                                    sourceAddress,
                                    serviceRef.getControlUrl(),
                                    discoveryResp.getServer(),
                                    serviceRef.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange());
                            break;
                        case PORT_MAPPER:
                            portMapper = new PortMapperUpnpIgdPortMapper(
                                    sourceAddress,
                                    serviceRef.getControlUrl(),
                                    discoveryResp.getServer(),
                                    serviceRef.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange());
                            break;
                        default:
                            throw new IllegalStateException(); // should never happen
                    }
                    ret.add(portMapper);
                }
            }
        }

        return ret;
    }

    private static Map<InetAddress, ServiceDiscoveryUpnpIgdResponse> probe(Bus networkBus) throws InterruptedException {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Get local IP addresses
        networkBus.send(new GetLocalIpAddressesRequest(selfBus));
        GetLocalIpAddressesResponse localIpsResp = (GetLocalIpAddressesResponse) queue.take();

        // Create UDP sockets and send discovery messages
        Map<Integer, InetAddress> udpSocketIds = new HashMap<>(); // id to src address
        Map<InetAddress, ServiceDiscoveryUpnpIgdResponse> probeResponses = new HashMap<>(); // source address to response
        for (InetAddress sourceAddress : localIpsResp.getLocalAddresses()) {
            System.out.println("sending to " + sourceAddress);
            try {
                networkBus.send(new CreateUdpSocketNetworkRequest(selfBus, sourceAddress));
                NetworkResponse createResp = (NetworkResponse) queue.take();

                int id = ((CreateUdpSocketNetworkResponse) createResp).getId();

                udpSocketIds.put(id, sourceAddress);

                UpnpIgdHttpRequest req;
                if (sourceAddress instanceof Inet4Address) {
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV4, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpBlockNetworkRequest(
                            id,
                            ProbeDeviceType.IPV4.getMulticastSocketAddress(),
                            req.dump()));
                } else if (sourceAddress instanceof Inet6Address) {
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_LINK_LOCAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpBlockNetworkRequest(
                            id,
                            ProbeDeviceType.IPV6_LINK_LOCAL.getMulticastSocketAddress(),
                            req.dump()));
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_SITE_LOCAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpBlockNetworkRequest(
                            id,
                            ProbeDeviceType.IPV6_SITE_LOCAL.getMulticastSocketAddress(),
                            req.dump()));
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_ORGANIZATION_LOCAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpBlockNetworkRequest(
                            id,
                            ProbeDeviceType.IPV6_ORGANIZATION_LOCAL.getMulticastSocketAddress(),
                            req.dump()));
                    req = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_GLOBAL, null, 3, "ssdp:all");
                    networkBus.send(new WriteUdpBlockNetworkRequest(
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

            NetworkResponse netResp = (NetworkResponse) queue.poll(sleepTime, TimeUnit.MILLISECONDS);
            if (netResp == null) {
                break;
            } else if (!(netResp instanceof ReadUdpBlockNetworkResponse)) {
                // We only care about responses -- message could be successful write or error
                continue;
            }
            
            ReadUdpBlockNetworkResponse readNetResp = (ReadUdpBlockNetworkResponse) netResp;
            int id = readNetResp.getId();

            InetAddress sourceAddress = udpSocketIds.get(id);
            try {
                ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(readNetResp.getData());
                probeResponses.put(sourceAddress, resp);
            } catch (IllegalArgumentException iae) {
                // if invalid, do nothing -- just skip over
            }
        }

        // Destroy UDP sockets
        for (int id : udpSocketIds.keySet()) {
            networkBus.send(new DestroySocketNetworkRequest(id));
        }

        return probeResponses;
    }

    private static RootUpnpIgdResponse queryDevice(Bus networkBus, InetAddress sourceAddress, URL location)
            throws UnknownHostException, IOException, InterruptedException {
        InetAddress destinationAddress = InetAddress.getByName(location.getHost());
        int destinationPort = location.getPort();

        RootUpnpIgdRequest req = new RootUpnpIgdRequest(location.getAuthority(), location.getFile());
        byte[] reqData = req.dump();

        byte[] respData = tcpRequest(networkBus, sourceAddress, destinationAddress, destinationPort, reqData);

        return new RootUpnpIgdResponse(location, respData);
    }

    private static ServiceDescriptionUpnpIgdResponse queryService(Bus networkBus, InetAddress sourceAddress, URL scpdUrl)
            throws UnknownHostException, IOException, InterruptedException {
        InetAddress destinationAddress = InetAddress.getByName(scpdUrl.getHost());
        int destinationPort = scpdUrl.getPort();

        ServiceDescriptionUpnpIgdRequest req = new ServiceDescriptionUpnpIgdRequest(scpdUrl.getAuthority(), scpdUrl.getFile());
        byte[] reqData = req.dump();

        byte[] respData = tcpRequest(networkBus, sourceAddress, destinationAddress, destinationPort, reqData);

        return new ServiceDescriptionUpnpIgdResponse(respData);
    }

    protected static byte[] tcpRequest(Bus networkBus, InetAddress sourceAddress, InetAddress destinationAddress, int destinationPort,
            byte[] data) throws IOException, InterruptedException {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);

        // Create TCP socket
        networkBus.send(new CreateTcpSocketNetworkRequest(selfBus, sourceAddress, destinationAddress, destinationPort));
        CreateTcpSocketNetworkResponse createResp = (CreateTcpSocketNetworkResponse) queue.poll(10000L, TimeUnit.MILLISECONDS);
        int id = createResp.getId();

        // Write request, and save any data that comes in
        ByteArrayOutputStream queuedRespBytes = new ByteArrayOutputStream();
        try {
            int remainingDataLen = data.length;

            long remainingTime = System.currentTimeMillis() + 10000L;
            while (true) {
                long sleepTime = remainingTime - System.currentTimeMillis();
                if (sleepTime < 0L) {
                    break;
                }

                if (remainingDataLen > 0) {
                    networkBus.send(new WriteTcpBlockNetworkRequest(id, data));
                }

                NetworkResponse resp = (NetworkResponse) queue.poll(10000L, TimeUnit.MILLISECONDS);

                if (resp instanceof WriteTcpBlockNetworkResponse) {
                    WriteTcpBlockNetworkResponse writeResp = (WriteTcpBlockNetworkResponse) resp;
                    remainingDataLen -= writeResp.getAmountWritten();
                } else if (resp instanceof ReadTcpBlockNetworkResponse) {
                    ReadTcpBlockNetworkResponse readResp = (ReadTcpBlockNetworkResponse) resp;
                    queuedRespBytes.write(readResp.getData());
                } else if (resp instanceof ErrorNetworkResponse) {
                    break;
                }
            }
        } finally {
            networkBus.send(new DestroySocketNetworkRequest(id));
        }

        // don't bother reading the destroysocket response, the queue being responded to is being thrown away
        return queuedRespBytes.toByteArray();
    }

    protected UpnpIgdPortMapper(InetAddress selfAddress, URL controlUrl, String serverName, String serviceType,
            Range<Long> externalPortRange, Range<Long> leaseDurationRange) {
        Validate.notNull(selfAddress);
        Validate.notNull(controlUrl);
//        Validate.notNull(serverName); // can be null
        Validate.notNull(serviceType);
        Validate.notNull(externalPortRange);
        Validate.notNull(leaseDurationRange);
        this.internalAddress = selfAddress;
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
