/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.mappers.upnpigd;

import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortType;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.mapper.MapperIoUtils.BytesToResponseTransformer;
import com.offbynull.portmapper.mapper.MapperIoUtils.TcpRequest;
import static com.offbynull.portmapper.mapper.MapperIoUtils.performTcpRequests;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.AddAnyPortMappingUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.AddAnyPortMappingUpnpIgdResponse;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.AddPortMappingUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.AddPortMappingUpnpIgdResponse;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.DeletePortMappingUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.DeletePortMappingUpnpIgdResponse;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.GetExternalIpAddressUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.GetExternalIpAddressUpnpIgdResponse;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(PortMapperUpnpIgdPortMapper.class);

    private final InetSocketAddress controlUrlAddress;
    private final boolean hasAddAnyPortMappingMethod;
    
    /**
     * Constructs a {@link PortMapperUpnpIgdPortMapper} object.
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
        
        controlUrlAddress = getAddressFromUrl(controlUrl);
        this.hasAddAnyPortMappingMethod = hasAddAnyPortMappingMethod;
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
        
        
        
        //
        // GET EXTERNAL IP
        //
        TcpRequest externalIpHttpRequest = new TcpRequest(
                internalAddress,
                controlUrlAddress,
                new GetExternalIpAddressUpnpIgdRequest(
                        controlUrl.getAuthority(),
                        controlUrl.getFile(),
                        serviceType),
                new BasicRequestTransformer(),
                new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        return new GetExternalIpAddressUpnpIgdResponse(buffer);
                    }
                });

        performTcpRequests(
                networkBus,
                Collections.singleton(externalIpHttpRequest),
                5000L, 5000L, 5000L);
        
        if (externalIpHttpRequest.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to getting external IP");
        }
        
        InetAddress externalAddress = ((GetExternalIpAddressUpnpIgdResponse) externalIpHttpRequest.getResponse()).getIpAddress();



        //
        // PERFORM MAPPING
        //
        MappedPort mappedPort;
        if (hasAddAnyPortMappingMethod) {
            mappedPort = newMapPort(portType, internalPort, externalPort, lifetime, externalAddress);
        } else {
            mappedPort = oldMapPort(portType, internalPort, externalPort, lifetime, externalAddress);
        }
        LOG.debug("Map successful {}", mappedPort);
        
        return mappedPort;
    }
    
    private MappedPort newMapPort(PortType portType, int internalPort, int externalPort, long lifetime, InetAddress externalAddress)
            throws InterruptedException {
        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        InetAddress internalAddress = getInternalAddress();
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
                new AddAnyPortMappingUpnpIgdRequest(
                        controlUrl.getAuthority(),
                        controlUrl.getFile(),
                        serviceType,
                        null,
                        externalPort,
                        portType,
                        internalPort,
                        internalAddress,
                        true,
                        "",
                        leaseDuration),
                new BasicRequestTransformer(),
                new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        return new AddAnyPortMappingUpnpIgdResponse(buffer);
                    }
                });

        performTcpRequests(
                networkBus,
                Collections.singleton(mapHttpRequest),
                5000L, 5000L, 5000L);
        
        if (mapHttpRequest.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to mapping");
        }
        
        
        
        int reservedExternalPort = ((AddAnyPortMappingUpnpIgdResponse) mapHttpRequest.getResponse()).getReservedPort();
        return new PortMapperMappedPort(
                internalPort,
                reservedExternalPort,
                externalAddress,
                portType,
                leaseDuration);
    }
    
    private MappedPort oldMapPort(PortType portType, int internalPort, int externalPort, long lifetime, InetAddress externalAddress)
            throws InterruptedException {
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
                    new AddPortMappingUpnpIgdRequest(
                            controlUrl.getAuthority(),
                            controlUrl.getFile(),
                            serviceType,
                            null,
                            externalPort,
                            portType,
                            internalPort,
                            internalAddress,
                            true,
                            "",
                            leaseDuration),
                    new BasicRequestTransformer(),
                    new BytesToResponseTransformer() {
                        @Override
                        public Object create(byte[] buffer) {
                            return new AddPortMappingUpnpIgdResponse(buffer);
                        }
                    });

            performTcpRequests(
                    networkBus,
                    Collections.singleton(mapHttpRequest),
                    retryDurations);

            if (mapHttpRequest.getResponse() != null) {
                // server responded, so we're good to go
                return new PortMapperMappedPort(
                        internalPort,
                        externalPort,
                        externalAddress,
                        portType,
                        leaseDuration);
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
        Validate.isTrue(mappedPort instanceof PortMapperMappedPort);

        Bus networkBus = getNetworkBus();
        URL controlUrl = getControlUrl();
        String serviceType = getServiceType();
        int externalPort = mappedPort.getExternalPort();
        InetAddress internalAddress = getInternalAddress();
        PortType portType = mappedPort.getPortType();
        
        TcpRequest httpRequest = new TcpRequest(
                internalAddress,
                controlUrlAddress,
                new DeletePortMappingUpnpIgdRequest(
                        controlUrl.getAuthority(),
                        controlUrl.getFile(),
                        serviceType,
                        null,
                        externalPort,
                        portType),
                new BasicRequestTransformer(),
                new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        return new DeletePortMappingUpnpIgdResponse(buffer);
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
        Validate.isTrue(mappedPort instanceof PortMapperMappedPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
        MappedPort newMappedPort = mapPort(mappedPort.getPortType(), mappedPort.getInternalPort(), mappedPort.getExternalPort(), lifetime);
        
        if (mappedPort.getExternalPort() != newMappedPort.getExternalPort()
                || !Objects.equals(mappedPort.getExternalAddress(), newMappedPort.getExternalAddress())) {
            LOG.warn("Failed refresh mapping {}: ", mappedPort, newMappedPort);
            try {
                unmapPort(newMappedPort);
            } catch (IllegalStateException ise) {
                // do nothing
            }
            
            throw new IllegalStateException("External IP/port changed from "
                    + mappedPort.getExternalAddress() + ":" + mappedPort.getExternalPort()
                    + " to "
                    + newMappedPort.getExternalAddress() + ":" + newMappedPort.getExternalPort());
        }
        
        LOG.debug("Mapping refreshed {}: ", mappedPort, newMappedPort);
        
        return newMappedPort;
    }

    @Override
    public String toString() {
        return "PortMapperUpnpIgdPortMapper{super=" + super.toString() + ", hasAddAnyPortMappingMethod=" + hasAddAnyPortMappingMethod + '}';
    }

}
