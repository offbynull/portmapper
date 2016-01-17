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

import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortType;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.mapper.MapperIoUtils.BytesToResponseTransformer;
import com.offbynull.portmapper.mapper.MapperIoUtils.TcpRequest;
import static com.offbynull.portmapper.mapper.MapperIoUtils.performTcpRequests;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.AddPinholeUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.AddPinholeUpnpIgdResponse;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.DeletePinholeUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.DeletePinholeUpnpIgdResponse;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.UpdatePinholeUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.UpdatePinholeUpnpIgdResponse;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Collections;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port mapper implementation that interfaces with a UPnP-IGD IPv6 firewall service.
 * <p>
 * Note that this port mapper doesn't care what the service type is. So long as the service type exposes AddPinhole, DeletePinhole, and
 * RefreshPinhole actions (and defines them as they're defined in WANIPv6FirewallControl:1), this port mapper will be able to call those
 * actions to expose ports.
 * @author Kasra Faghihi
 */
public final class FirewallUpnpIgdPortMapper extends UpnpIgdPortMapper {
    private static final Logger LOG = LoggerFactory.getLogger(FirewallUpnpIgdPortMapper.class);

    private final InetSocketAddress controlUrlAddress;
    
    /**
     * Constructs a {@link FirewallUpnpIgdPortMapper} object.
     * @param networkBus bus to network component
     * @param internalAddress local address accessing gateway device
     * @param controlUrl service control URL
     * @param serverName server name (can be {@code null}
     * @param serviceType service type
     * @param externalPortRange external port range
     * @param leaseDurationRange lease duration range
     * @throws NullPointerException if any argument other than {@code severName} is {@code null}
     * @throws IllegalArgumentException if {@code 0 > leaseDurationRange > 0xFFFFFFFFL || 0 > externalPortRange > 0xFFFFL} (note that
     * a 0 lease duration means either default value or infinite, and a 0 external port means wildcard), or if {@code controlUrl}'s protocol
     * was not {@code http}
     */
    public FirewallUpnpIgdPortMapper(Bus networkBus, InetAddress internalAddress, URL controlUrl, String serverName, String serviceType,
            Range<Long> externalPortRange, Range<Long> leaseDurationRange) {
        super(networkBus, internalAddress, controlUrl, serverName, serviceType, externalPortRange, leaseDurationRange);
        controlUrlAddress = getAddressFromUrl(controlUrl);
    }


    @Override
    public MappedPort mapPort(PortType portType, int internalPort, int externalPort, long lifetime) throws InterruptedException {
        LOG.info("Attempting to map {} Internal:{} External:{} Lifetime:{}", portType, internalPort, externalPort, lifetime);
        
        Validate.notNull(portType);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);

        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        InetAddress internalAddress = getInternalAddress();

        // attempt to map 5 times -- first attempt should be 3 tries to map the externalPort passed in... anything after that is 1 attempt
        // to map a randomized externalPort
        long[] retryDurations = new long[] {5000L, 5000L, 5000L};
        for (int i = 0; i < 5; i++) {
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

            TcpRequest mapHttpRequest = new TcpRequest(
                    internalAddress,
                    controlUrlAddress,
                    new AddPinholeUpnpIgdRequest(
                            controlUrl.getAuthority(),
                            controlUrl.getFile(),
                            serviceType,
                            null,
                            externalPort,
                            internalAddress,
                            internalPort,
                            portType,
                            leaseDuration),
                    new BasicRequestTransformer(),
                    new BytesToResponseTransformer() {
                        @Override
                        public Object create(byte[] buffer) {
                            return new AddPinholeUpnpIgdResponse(buffer);
                        }
                    });

            performTcpRequests(
                    networkBus,
                    Collections.singleton(mapHttpRequest),
                    retryDurations);

            if (mapHttpRequest.getResponse() != null) {
                // server responded, so we're good to go
                String key = ((AddPinholeUpnpIgdResponse) mapHttpRequest.getResponse()).getUniqueId();
                
                MappedPort mappedPort = new FirewallMappedPort(key, internalPort, externalPort, portType, leaseDuration);
                LOG.debug("Map successful {}", mappedPort);
                
                return mappedPort;
            }
            
            // choose another external port for next try -- next try only make 1 attempt
            retryDurations = new long[] {5000L};
            externalPort = RandomUtils.nextInt(
                    externalPortRange.getMinimum().intValue(), // should never be < 1
                    externalPortRange.getMaximum().intValue() + 1); // should never be > 65535
        }
        
        
        
        throw new IllegalStateException();
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        LOG.info("Attempting to unmap {}", mappedPort);
        
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof FirewallMappedPort);

        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        String key = ((FirewallMappedPort) mappedPort).getKey();
        InetAddress internalAddress = getInternalAddress();
        
        TcpRequest httpRequest = new TcpRequest(
                internalAddress,
                controlUrlAddress,
                new DeletePinholeUpnpIgdRequest(
                        controlUrl.getAuthority(),
                        controlUrl.getFile(),
                        serviceType,
                        key),
                new BasicRequestTransformer(),
                new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        return new DeletePinholeUpnpIgdResponse(buffer);
                    }
                });
        
        performTcpRequests(
                networkBus,
                Collections.singleton(httpRequest),
                5000L, 5000L, 5000L);
        
        if (httpRequest.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to unmapping");
        }
        
        LOG.debug("Unmap successful {}", mappedPort);
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        LOG.info("Attempting to refresh mapping {} for {}", mappedPort, lifetime);
        
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof FirewallMappedPort);

        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        Range<Long> leaseDurationRange = getLeaseDurationRange();
        long leaseDuration;
        if (leaseDurationRange.isBefore(lifetime)) {
            leaseDuration = leaseDurationRange.getMaximum();
        } else if (leaseDurationRange.isAfter(lifetime)) {
            leaseDuration = leaseDurationRange.getMinimum();
        } else {
            leaseDuration = lifetime;
        }
        String key = ((FirewallMappedPort) mappedPort).getKey();
        InetAddress internalAddress = getInternalAddress();
        
        TcpRequest httpRequest = new TcpRequest(
                internalAddress,
                controlUrlAddress,
                new UpdatePinholeUpnpIgdRequest(
                        controlUrl.getAuthority(),
                        controlUrl.getFile(),
                        serviceType,
                        key,
                        leaseDuration),
                new BasicRequestTransformer(),
                new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        return new UpdatePinholeUpnpIgdResponse(buffer);
                    }
                });
        
        performTcpRequests(
                networkBus,
                Collections.singleton(httpRequest),
                5000L, 5000L, 5000L);
        
        if (httpRequest.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to refresh");
        }
        
        FirewallMappedPort newMappedPort = new FirewallMappedPort(key, mappedPort.getInternalPort(), mappedPort.getExternalPort(),
                mappedPort.getPortType(), leaseDuration);
        
        LOG.warn("Mapping refreshed {}: ", mappedPort, newMappedPort);
        
        return newMappedPort;
    }

    @Override
    public String toString() {
        return "FirewallUpnpIgdPortMapper{super=" + super.toString() + '}';
    }
    
}
