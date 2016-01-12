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
import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.upnpigd.InternalUtils.HttpRequest;
import com.offbynull.portmapper.upnpigd.InternalUtils.ResponseCreator;
import com.offbynull.portmapper.upnpigd.InternalUtils.UdpRequest;
import static com.offbynull.portmapper.upnpigd.InternalUtils.getLocalIpAddresses;
import static com.offbynull.portmapper.upnpigd.InternalUtils.performBatchedHttpRequests;
import com.offbynull.portmapper.upnpigd.externalmessages.RootUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.externalmessages.RootUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.externalmessages.RootUpnpIgdResponse.ServiceReference;
import com.offbynull.portmapper.upnpigd.externalmessages.ServiceDescriptionUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.IdentifiedService;
import com.offbynull.portmapper.upnpigd.externalmessages.ServiceDiscoveryUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.externalmessages.ServiceDiscoveryUpnpIgdRequest.ProbeDeviceType;
import com.offbynull.portmapper.upnpigd.externalmessages.ServiceDiscoveryUpnpIgdResponse;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import static com.offbynull.portmapper.upnpigd.InternalUtils.performUdpRequests;
import com.offbynull.portmapper.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType;
import static com.offbynull.portmapper.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType.FIREWALL;
import static com.offbynull.portmapper.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType.OLD_PORT_MAPPER;
import static com.offbynull.portmapper.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType.NEW_PORT_MAPPER;
import com.offbynull.portmapper.upnpigd.externalmessages.UpnpIgdHttpResponse;

/**
 * UPNP-IGD {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdPortMapper implements PortMapper {

    private final Bus networkBus;
    private final InetAddress internalAddress;
    private final URL controlUrl;
    private final String serverName;
    private final String serviceType;
    private final Range<Long> externalPortRange;
    private final Range<Long> leaseDurationRange;

    /**
     * Constructs a {@link UpnpIgdPortMapper} object.
     * @param networkBus network bus
     * @param internalAddress source address (address to communicate with gateway from)
     * @param controlUrl control URL
     * @param serverName sever name (may be {@code null})
     * @param serviceType service type
     * @param externalPortRange external port range
     * @param leaseDurationRange lease duration range
     * @throws NullPointerException if any argument other than {@code serverName} is {@code null}
     * @throws IllegalArgumentException if {@code 0L > externalPortRange > 65535L || 0L > leaseDurationRange > 0xFFFFFFFFL}, or if
     * {@code controlUrl} scheme is not {@code "http"}
     * 
     */
    protected UpnpIgdPortMapper(Bus networkBus, InetAddress internalAddress, URL controlUrl, String serverName, String serviceType,
            Range<Long> externalPortRange, Range<Long> leaseDurationRange) {
        Validate.notNull(networkBus);
        Validate.notNull(internalAddress);
        Validate.notNull(controlUrl);
//        Validate.notNull(serverName); // can be null
        Validate.notNull(serviceType);
        Validate.notNull(externalPortRange);
        Validate.notNull(leaseDurationRange);
        Validate.isTrue(leaseDurationRange.getMinimum() >= 0L);
        Validate.isTrue(leaseDurationRange.getMaximum() <= 0xFFFFFFFFL);
        Validate.isTrue(externalPortRange.getMinimum() >= 0L);
        Validate.isTrue(externalPortRange.getMaximum() <= 0xFFFFL);
        Validate.isTrue("http".equalsIgnoreCase(controlUrl.getProtocol()));
        this.networkBus = networkBus;
        this.internalAddress = internalAddress;
        this.controlUrl = controlUrl;
        this.serverName = serverName;
        this.serviceType = serviceType;
        this.externalPortRange = externalPortRange;
        this.leaseDurationRange = leaseDurationRange;
    }

    /**
     * Get network bus.
     * @return network bus
     */
    protected final Bus getNetworkBus() {
        return networkBus;
    }

    /**
     * Get local address used to interface with the UPnP-IGD device.
     * @return local address
     */
    protected final InetAddress getInternalAddress() {
        return internalAddress;
    }

    /**
     * Get control URL.
     * @return control URL
     */
    protected final URL getControlUrl() {
        return controlUrl;
    }

    /**
     * Get server name.
     * @return server name (may be {@code null})
     */
    protected final String getServerName() {
        return serverName;
    }

    /**
     * Get service type.
     * @return service type
     */
    protected final String getServiceType() {
        return serviceType;
    }

    /**
     * Get external port mapping range.
     * @return external port mapping range
     */
    protected final Range<Long> getExternalPortRange() {
        return externalPortRange;
    }

    /**
     * Get lease duration range.
     * @return lease duration range
     */
    protected final Range<Long> getLeaseDurationRange() {
        return leaseDurationRange;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public String toString() {
        return "UpnpIgdPortMapper{" + "internalAddress=" + internalAddress + ", controlUrl=" + controlUrl + ", serverName=" + serverName
                + ", serviceType=" + serviceType + ", externalPortRange=" + externalPortRange + ", leaseDurationRange=" + leaseDurationRange
                + '}';
    }
    // CHECKSTYLE:ON:DesignForExtension

    /**
     * Identify UPnP-IGD devices on all interfaces.
     * @param networkBus network bus
     * @return set of found UPnP-IGD devices
     * @throws NullPointerException if any argument is {@code null}
     * @throws InterruptedException if interrupted
     */
    public static Set<UpnpIgdPortMapper> identify(Bus networkBus) throws InterruptedException {
        Validate.notNull(networkBus);

        // Probe for devices -- for each device found, query the device
        Set<InetAddress> sourceAddresses = getLocalIpAddresses(networkBus);
        Collection<UdpRequest> discoveryRequests = new LinkedList<>();
        for (InetAddress sourceAddress : sourceAddresses) {
            if (sourceAddress instanceof Inet4Address) {
                UdpRequest req = new UdpRequest();
                req.sourceAddress = sourceAddress;
                req.destinationSocketAddress = ProbeDeviceType.IPV4.getMulticastSocketAddress();
                req.respCreator = new ServiceDiscoveryUpnpIgdResponseCreator();
                req.sendMsg = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV4, null, 3, "ssdp:all");
                discoveryRequests.add(req);
            } else if (sourceAddress instanceof Inet6Address) {
                UdpRequest v6LocalReq = new UdpRequest();
                v6LocalReq.sourceAddress = sourceAddress;
                v6LocalReq.destinationSocketAddress = ProbeDeviceType.IPV6_LINK_LOCAL.getMulticastSocketAddress();
                v6LocalReq.respCreator = new ServiceDiscoveryUpnpIgdResponseCreator();
                v6LocalReq.sendMsg = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_LINK_LOCAL, null, 3, "ssdp:all");
                discoveryRequests.add(v6LocalReq);

                UdpRequest v6SiteReq = new UdpRequest();
                v6SiteReq.sourceAddress = sourceAddress;
                v6SiteReq.destinationSocketAddress = ProbeDeviceType.IPV6_SITE_LOCAL.getMulticastSocketAddress();
                v6SiteReq.respCreator = new ServiceDiscoveryUpnpIgdResponseCreator();
                v6SiteReq.sendMsg = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_SITE_LOCAL, null, 3, "ssdp:all");
                discoveryRequests.add(v6SiteReq);

                UdpRequest v6OrgReq = new UdpRequest();
                v6OrgReq.sourceAddress = sourceAddress;
                v6OrgReq.destinationSocketAddress = ProbeDeviceType.IPV6_ORGANIZATION_LOCAL.getMulticastSocketAddress();
                v6OrgReq.respCreator = new ServiceDiscoveryUpnpIgdResponseCreator();
                v6OrgReq.sendMsg = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_ORGANIZATION_LOCAL, null, 3, "ssdp:all");
                discoveryRequests.add(v6OrgReq);

                UdpRequest v6GlobalReq = new UdpRequest();
                v6GlobalReq.sourceAddress = sourceAddress;
                v6GlobalReq.destinationSocketAddress = ProbeDeviceType.IPV6_GLOBAL.getMulticastSocketAddress();
                v6GlobalReq.respCreator = new ServiceDiscoveryUpnpIgdResponseCreator();
                v6GlobalReq.sendMsg = new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_GLOBAL, null, 3, "ssdp:all");
                discoveryRequests.add(v6GlobalReq);
            } else {
                throw new IllegalStateException();
            }
        }
        performUdpRequests(networkBus, discoveryRequests, 1000L, 1000L, 1000L, 1000L, 1000L);

        // Get root XMLs
        Collection<HttpRequest> rootRequests = new ArrayList<>(discoveryRequests.size());
        for (UdpRequest discoveryReq : discoveryRequests) {
            for (UpnpIgdHttpResponse respMsg : discoveryReq.respMsges) {
                try {
                    ServiceDiscoveryUpnpIgdResponse discoveryResp = (ServiceDiscoveryUpnpIgdResponse) respMsg;

                    HttpRequest req = new HttpRequest();

                    ProbeResult other = new ProbeResult();
                    other.source = discoveryReq.sourceAddress;
                    other.location = discoveryResp.getLocation();
                    other.serverName = discoveryResp.getServer();

                    req.other = other;
                    req.sourceAddress = other.source;
                    req.location = other.location;
                    req.respCreator = new RootUpnpIgdResponseCreator(other.location);
                    req.sendMsg = new RootUpnpIgdRequest(other.location.getAuthority(), other.location.getFile());
                    rootRequests.add(req);
                } catch (RuntimeException iae) {
                    // failed to parse, so skip to next
                }
            }
        }
        performBatchedHttpRequests(networkBus, rootRequests, 5000L, 5000L, 5000L);

        // Extract service locations from root XMLs + get service descriptions
        Collection<HttpRequest> serviceDescRequests = new ArrayList<>(rootRequests.size());
        for (HttpRequest rootRequest : rootRequests) {
            try {
                RootUpnpIgdResponse rootResp = (RootUpnpIgdResponse) rootRequest.respMsg;

                for (ServiceReference serviceReference : rootResp.getServices()) {
                    URL scpdUrl = serviceReference.getScpdUrl();

                    RootRequestResult other = new RootRequestResult();
                    other.probeResult = (ProbeResult) rootRequest.other;
                    other.serviceReference = serviceReference;

                    HttpRequest req = new HttpRequest();
                    req.other = other;
                    req.sourceAddress = rootRequest.sourceAddress;
                    req.location = scpdUrl;
                    req.respCreator = new ServiceDescriptionUpnpIgdResponseCreator();
                    req.sendMsg = new ServiceDescriptionUpnpIgdRequest(scpdUrl.getAuthority(), scpdUrl.getFile());
                    serviceDescRequests.add(req);
                }
            } catch (RuntimeException iae) {
                // failed to parse, so skip to next
            }
        }
        performBatchedHttpRequests(networkBus, serviceDescRequests, 5000L, 5000L, 5000L);

        // Get service descriptions
        Set<UpnpIgdPortMapper> ret = new HashSet<>();
        for (HttpRequest serviceDescRequest : serviceDescRequests) {
            try {
                ServiceDescriptionUpnpIgdResponse serviceDescResp = (ServiceDescriptionUpnpIgdResponse) serviceDescRequest.respMsg;

                RootRequestResult rootReqRes = (RootRequestResult) serviceDescRequest.other;
                for (Entry<ServiceType, IdentifiedService> e : serviceDescResp.getIdentifiedServices().entrySet()) {
                    ServiceType serviceType = e.getKey();
                    IdentifiedService identifiedService = e.getValue();

                    UpnpIgdPortMapper upnpIgdPortMapper;
                    switch (serviceType) {
                        case OLD_PORT_MAPPER:
                            upnpIgdPortMapper = new PortMapperUpnpIgdPortMapper(
                                    networkBus,
                                    serviceDescRequest.sourceAddress,
                                    rootReqRes.serviceReference.getControlUrl(),
                                    rootReqRes.probeResult.serverName,
                                    rootReqRes.serviceReference.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange(),
                                    false);
                            break;
                        case NEW_PORT_MAPPER:
                            upnpIgdPortMapper = new PortMapperUpnpIgdPortMapper(
                                    networkBus,
                                    serviceDescRequest.sourceAddress,
                                    rootReqRes.serviceReference.getControlUrl(),
                                    rootReqRes.probeResult.serverName,
                                    rootReqRes.serviceReference.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange(),
                                    true);
                            break;
                        case FIREWALL:
                            upnpIgdPortMapper = new FirewallUpnpIgdPortMapper(
                                    networkBus,
                                    serviceDescRequest.sourceAddress,
                                    rootReqRes.serviceReference.getControlUrl(),
                                    rootReqRes.probeResult.serverName,
                                    rootReqRes.serviceReference.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange());
                            break;
                        default:
                            throw new IllegalStateException(); // should never happen
                    }

                    ret.add(upnpIgdPortMapper);
                }
            } catch (RuntimeException iae) {
                // failed to parse, so skip to next
            }
        }

        return ret;
    }


    @Override
    public final InetAddress getSourceAddress() {
        return internalAddress;
    }

    private static final class ProbeResult {

        private InetAddress source;
        private URL location;
        private String serverName;
    }

    private static final class RootRequestResult {

        private ProbeResult probeResult;
        private ServiceReference serviceReference;
    }

    private static final class ServiceDiscoveryUpnpIgdResponseCreator implements ResponseCreator {

        @Override
        public UpnpIgdHttpResponse create(byte[] buffer) {
            return new ServiceDiscoveryUpnpIgdResponse(buffer);
        }
    }

    private static final class RootUpnpIgdResponseCreator implements ResponseCreator {

        private URL baseUrl;

        private RootUpnpIgdResponseCreator(URL baseUrl) {
            this.baseUrl = baseUrl;
        }

        @Override
        public UpnpIgdHttpResponse create(byte[] buffer) {
            return new RootUpnpIgdResponse(baseUrl, buffer);
        }
    }

    private static final class ServiceDescriptionUpnpIgdResponseCreator implements ResponseCreator {

        @Override
        public ServiceDescriptionUpnpIgdResponse create(byte[] buffer) {
            return new ServiceDescriptionUpnpIgdResponse(buffer);
        }
    }
}
