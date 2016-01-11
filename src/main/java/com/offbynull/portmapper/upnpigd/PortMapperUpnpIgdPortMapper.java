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

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortType;
import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.upnpigd.InternalUtils.HttpRequest;
import com.offbynull.portmapper.upnpigd.InternalUtils.ResponseCreator;
import static com.offbynull.portmapper.upnpigd.InternalUtils.performHttpRequests;
import com.offbynull.portmapper.upnpigd.externalmessages.AddAnyPortMappingUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.externalmessages.AddAnyPortMappingUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.externalmessages.AddPortMappingUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.externalmessages.AddPortMappingUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.externalmessages.DeletePortMappingUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.externalmessages.DeletePortMappingUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.externalmessages.GetExternalIpAddressUpnpIgdRequest;
import com.offbynull.portmapper.upnpigd.externalmessages.GetExternalIpAddressUpnpIgdResponse;
import com.offbynull.portmapper.upnpigd.externalmessages.Protocol;
import com.offbynull.portmapper.upnpigd.externalmessages.UpnpIgdHttpResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collections;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;

/**
 * Port mapper implementation that interfaces with a UPnP-IGD IPv4 port mapping service (both 1.0 and 2.0 variants).
 * <p>
 * Note that this port mapper doesn't care what the service type is. So long as the service type exposes GetExternalIPAddress,
 * GetSpecificPortMappingEntry, DeletePortMapping, and AddPortMapping/AddAnyPortMapping actions (and defines them as they're defined in
 * typical profiles that support port mapping -- such as WANIPConnection:1), this port mapper will be able to call those actions to expose
 * ports.
 * @author Kasra Faghihi
 */
public final class PortMapperUpnpIgdPortMapper extends UpnpIgdPortMapper {

    private final boolean hasAddAnyPortMappingMethod;
    
    /**
     * Constructs a {@link PortMapperUpnpIgdPortMapper} object
     * @param networkBus bus to network component
     * @param internalAddress local address accessing gateway device
     * @param controlUrl service control URL
     * @param serverName server name (can be {@code null}
     * @param serviceType service type
     * @param externalPortRange external port range
     * @param leaseDurationRange lease duration range
     * @param hasAddAnyPortMappingMethod {@code true} if AddAnyPortMapping action is available, otherwise {@code false}
     * @throws NullPointerException if any argument other than {@code severName} is {@code null}
     * @throws IllegalArgumentException if {@code 0 > leaseDurationRange > 0xFFFFFFFFL || 0 > externalPortRange > 0xFFFFL} (note that
     * a 0 lease duration means either default value or infinite, and a 0 external port means wildcard)
     */
    public PortMapperUpnpIgdPortMapper(Bus networkBus, InetAddress internalAddress, URL controlUrl, String serverName, String serviceType,
            Range<Long> externalPortRange, Range<Long> leaseDurationRange, boolean hasAddAnyPortMappingMethod) {
        super(networkBus, internalAddress, controlUrl, serverName, serviceType, externalPortRange, leaseDurationRange);
        
        this.hasAddAnyPortMappingMethod = hasAddAnyPortMappingMethod;
    }


    @Override
    public MappedPort mapPort(PortType portType, int internalPort, int externalPort, long lifetime) throws InterruptedException {
        Validate.notNull(portType);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);

        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        InetAddress internalAddress = getInternalAddress();
        
        
        
        //
        // GET EXTERNAL IP
        //
        HttpRequest externalIpHttpRequest = new HttpRequest();
        externalIpHttpRequest.location = controlUrl;
        externalIpHttpRequest.sourceAddress = internalAddress;
        externalIpHttpRequest.sendMsg = new GetExternalIpAddressUpnpIgdRequest(
                controlUrl.getAuthority(),
                controlUrl.getFile(),
                serviceType);
        externalIpHttpRequest.respCreator = new ResponseCreator() {
            @Override
            public UpnpIgdHttpResponse create(byte[] buffer) {
                return new GetExternalIpAddressUpnpIgdResponse(buffer);
            }
        };

        performHttpRequests(
                networkBus,
                Collections.singleton(externalIpHttpRequest),
                5000L, 5000L, 5000L);
        
        if (externalIpHttpRequest.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to getting external IP");
        }
        
        InetAddress externalAddress = ((GetExternalIpAddressUpnpIgdResponse) externalIpHttpRequest.respMsg).getIpAddress();



        //
        // PERFORM MAPPING
        //
        int reservedExternalPort;
        if (hasAddAnyPortMappingMethod) {
            reservedExternalPort = newMapPort(portType, internalPort, externalPort, lifetime);
        } else {
            reservedExternalPort = oldMapPort(portType, internalPort, externalPort, lifetime);
        }
        
        
        
        
        return new PortMapperMappedPort(internalPort, reservedExternalPort, externalAddress, portType, lifetime);
    }
    
    private int newMapPort(PortType portType, int internalPort, int externalPort, long lifetime) throws InterruptedException {
        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        InetAddress internalAddress = getInternalAddress();
        
        Protocol protocol;
        switch (portType) {
            case TCP:
                protocol = Protocol.TCP;
                break;
            case UDP:
                protocol = Protocol.UDP;
                break;
            default:
                throw new IllegalStateException(); // shuold never happend
        }
        Range<Long> externalPortRange = getExternalPortRange();
        Range<Long> leaseDurationRange = getLeaseDurationRange();
        long leaseDuration;
        if (leaseDurationRange.isBefore(lifetime)) {
            leaseDuration = leaseDurationRange.getMaximum();
        } else if (leaseDurationRange.isAfter(lifetime)) {
            leaseDuration = leaseDurationRange.getMinimum();
        } else {
            leaseDuration = lifetime;
        }
        
        Validate.validState(externalPortRange.contains((long) externalPort),
                "Router reports external port mappings as %s", externalPortRange);
        
        HttpRequest mapHttpRequest = new HttpRequest();
        mapHttpRequest.location = controlUrl;
        mapHttpRequest.sourceAddress = internalAddress;
        mapHttpRequest.sendMsg = new AddAnyPortMappingUpnpIgdRequest(
                controlUrl.getAuthority(),
                controlUrl.getFile(),
                serviceType,
                null,
                externalPort,
                protocol,
                internalPort,
                internalAddress,
                true,
                "",
                leaseDuration);
        mapHttpRequest.respCreator = new ResponseCreator() {
            @Override
            public UpnpIgdHttpResponse create(byte[] buffer) {
                return new AddAnyPortMappingUpnpIgdResponse(buffer);
            }
        };

        performHttpRequests(
                networkBus,
                Collections.singleton(mapHttpRequest),
                5000L, 5000L, 5000L);
        
        if (mapHttpRequest.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to mapping");
        }
        
        
        
        return ((AddAnyPortMappingUpnpIgdResponse) mapHttpRequest.respMsg).getReservedPort();
    }
    
    private int oldMapPort(PortType portType, int internalPort, int externalPort, long lifetime) throws InterruptedException {
        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        InetAddress internalAddress = getInternalAddress();
        
        // attempt to map 5 times -- first attempt should be 3 tries to map the externalPort passed in... anything after that is 1 attempt
        // to map a randomized externalPort
        long[] retryDurations = new long[] { 5000L, 5000L, 5000L };
        for (int i = 0; i < 5; i++) {
            Protocol protocol;
            switch (portType) {
                case TCP:
                    protocol = Protocol.TCP;
                    break;
                case UDP:
                    protocol = Protocol.UDP;
                    break;
                default:
                    throw new IllegalStateException(); // shuold never happend
            }
            Range<Long> externalPortRange = getExternalPortRange();
            Range<Long> leaseDurationRange = getLeaseDurationRange();
            long leaseDuration;
            if (leaseDurationRange.isBefore(lifetime)) {
                leaseDuration = leaseDurationRange.getMaximum();
            } else if (leaseDurationRange.isAfter(lifetime)) {
                leaseDuration = leaseDurationRange.getMinimum();
            } else {
                leaseDuration = lifetime;
            }

            Validate.validState(externalPortRange.contains((long) externalPort),
                    "Router reports external port mappings as %s", externalPortRange);

            HttpRequest mapHttpRequest = new HttpRequest();
            mapHttpRequest.location = controlUrl;
            mapHttpRequest.sourceAddress = internalAddress;
            mapHttpRequest.sendMsg = new AddPortMappingUpnpIgdRequest(
                    controlUrl.getAuthority(),
                    controlUrl.getFile(),
                    serviceType,
                    null,
                    externalPort,
                    protocol,
                    internalPort,
                    internalAddress,
                    true,
                    "",
                    leaseDuration);
            mapHttpRequest.respCreator = new ResponseCreator() {
                @Override
                public UpnpIgdHttpResponse create(byte[] buffer) {
                    return new AddPortMappingUpnpIgdResponse(buffer);
                }
            };

            performHttpRequests(
                    networkBus,
                    Collections.singleton(mapHttpRequest),
                    retryDurations);

            if (mapHttpRequest.respMsg != null) {
                // server responded, so we're good to go
                return externalPort;
            }
            
            // choose another external port for next try -- next try only make 1 attempt
            retryDurations = new long[] { 5000L };
            externalPort = RandomUtils.nextInt(
                    externalPortRange.getMinimum().intValue(), // should never be < 1
                    externalPortRange.getMaximum().intValue() + 1); // should never be > 65535
        }
        
        
        
        throw new IllegalStateException();
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof PortMapperMappedPort);

        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        Protocol protocol;
        switch (mappedPort.getPortType()) {
            case TCP:
                protocol = Protocol.TCP;
                break;
            case UDP:
                protocol = Protocol.UDP;
                break;
            default:
                throw new IllegalStateException(); // shuold never happend
        }
        int externalPort = mappedPort.getExternalPort();
        InetAddress internalAddress = getInternalAddress();
        
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.location = controlUrl;
        httpRequest.sourceAddress = internalAddress;
        httpRequest.sendMsg = new DeletePortMappingUpnpIgdRequest(
                controlUrl.getAuthority(),
                controlUrl.getFile(),
                serviceType,
                null,
                externalPort,
                protocol);
        httpRequest.respCreator = new ResponseCreator() {
            @Override
            public UpnpIgdHttpResponse create(byte[] buffer) {
                return new DeletePortMappingUpnpIgdResponse(buffer);
            }
        };
        
        performHttpRequests(
                networkBus,
                Collections.singleton(httpRequest),
                5000L, 5000L, 5000L);
        
        if (httpRequest.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to unmapping");
        }
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof PortMapperMappedPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
        return mapPort(mappedPort.getPortType(), mappedPort.getInternalPort(), mappedPort.getExternalPort(), lifetime);
    }

    @Override
    public String toString() {
        return "PortMapperUpnpIgdPortMapper{super=" + super.toString() + ", hasAddAnyPortMappingMethod=" + hasAddAnyPortMappingMethod + '}';
    }

}