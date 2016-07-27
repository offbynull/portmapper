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
package com.offbynull.portmapper.mappers.pcp;

import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.mapper.PortType;
import com.offbynull.portmapper.gateway.Bus;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV4;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV6;
import com.offbynull.portmapper.helpers.TextUtils;
import com.offbynull.portmapper.mapper.MapperIoUtils;
import com.offbynull.portmapper.mapper.MapperIoUtils.BytesToResponseTransformer;
import static com.offbynull.portmapper.mapper.MapperIoUtils.PRESET_IPV4_GATEWAY_ADDRESSES;
import com.offbynull.portmapper.mapper.MapperIoUtils.RequestToBytesTransformer;
import com.offbynull.portmapper.mapper.MapperIoUtils.UdpRequest;
import static com.offbynull.portmapper.mapper.MapperIoUtils.calculateExponentialBackoffTimes;
import static com.offbynull.portmapper.mapper.MapperIoUtils.convertToAddressSet;
import static com.offbynull.portmapper.mapper.MapperIoUtils.getLocalIpAddresses;
import static com.offbynull.portmapper.mapper.MapperIoUtils.performUdpRequests;
import static com.offbynull.portmapper.mapper.MapperIoUtils.runProcesses;
import com.offbynull.portmapper.mappers.pcp.externalmessages.MapPcpRequest;
import com.offbynull.portmapper.mappers.pcp.externalmessages.MapPcpResponse;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PCP {@link PortMapper} implementation.
 * @author Kasra Faghihi
 */
public final class PcpPortMapper implements PortMapper {
    
    private static final Logger LOG = LoggerFactory.getLogger(PcpPortMapper.class);
    
    private static final int PORT = 5351;
    
    private Bus networkBus;
    private InetAddress internalAddress;
    private InetAddress gatewayAddress;
    private Random random;

    /**
     * Identify PCP devices on all interfaces.
     * <p>
     * Since PCP devices have no broadcast discovery mechanism, this method makes use of multiple OS-specific commands to try to find the
     * addresses of gateways. This may change in the future -- see <a href="https://tools.ietf.org/html/draft-ietf-pcp-anycast-08">this RFC
     * draft</a>.
     * @param networkBus network bus
     * @param processBus process bus
     * @param additionalIps additional IPs to check
     * @return set of found PCP devices
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws InterruptedException if interrupted
     */
    public static List<PcpPortMapper> identify(Bus networkBus, Bus processBus, InetAddress ... additionalIps) throws InterruptedException {
        LOG.info("Attempting to identify devices");
        
        Validate.notNull(networkBus);
        Validate.notNull(processBus);
        Validate.notNull(additionalIps);
        Validate.noNullElements(additionalIps);

        // Perform commands to try to grab gateway addresses
        List<MapperIoUtils.ProcessRequest> processReqs = new ArrayList<>();
        processReqs.add(new MapperIoUtils.ProcessRequest("netstat", "-rn")); //linux mac and windows -- but seems wrong for windows
        processReqs.add(new MapperIoUtils.ProcessRequest("route", "-n")); // linux
        processReqs.add(new MapperIoUtils.ProcessRequest("route", "-n", "get", "default")); // mac
        processReqs.add(new MapperIoUtils.ProcessRequest("ipconfig")); // windows
        processReqs.add(new MapperIoUtils.ProcessRequest("ifconfig")); // linux (and mac?)
        runProcesses(processBus, processReqs, 10000L);
        
        
        
        // Aggregate results
        Set<InetAddress> potentialGatewayAddresses = new HashSet<>();
        potentialGatewayAddresses.addAll(PRESET_IPV4_GATEWAY_ADDRESSES);
        potentialGatewayAddresses.addAll(Arrays.asList(additionalIps));
        
        for (MapperIoUtils.ProcessRequest req : processReqs) {
            List<String> netstatOutputIpv4Addresses = TextUtils.findAllIpv4Addresses(req.getOutput());
            List<String> netstatOutputIpv6Addresses = TextUtils.findAllIpv6Addresses(req.getOutput());
            List<String> netstatErrorIpv4Addresses = TextUtils.findAllIpv4Addresses(req.getError());
            List<String> netstatErrorIpv6Addresses = TextUtils.findAllIpv6Addresses(req.getError());

            potentialGatewayAddresses.addAll(convertToAddressSet(netstatOutputIpv4Addresses));
            potentialGatewayAddresses.addAll(convertToAddressSet(netstatOutputIpv6Addresses));
            potentialGatewayAddresses.addAll(convertToAddressSet(netstatErrorIpv4Addresses));
            potentialGatewayAddresses.addAll(convertToAddressSet(netstatErrorIpv6Addresses));
        }
        
        
        
        // Filter out non-valid addresses
        //   Remove any local address (e.g. 0.0.0.0)
        //   Remove loopback address (e.g. 127.0.0.1)
        //   Remove multicast address (e.g. 224.0.0.1)
        Iterator<InetAddress> pgaIt = potentialGatewayAddresses.iterator();
        while (pgaIt.hasNext()) {
            InetAddress address = pgaIt.next();
            if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isMulticastAddress()) {
                pgaIt.remove();
            }
        }
        
        
        
        // Query -- send each query to every interface
        List<UdpRequest> udpReqs = new LinkedList<>();
        
        Set<InetAddress> sourceAddresses = getLocalIpAddresses(networkBus);
        for (InetAddress sourceAddress : sourceAddresses) {
            for (InetAddress gatewayAddress : potentialGatewayAddresses) {
                // both addresses must be ipv4 or both address must be ipv6
                if (!sourceAddress.getClass().equals(gatewayAddress.getClass())) {
                    continue;
                }

                // Send a map pcp request to identify PCP-enabled routers...
                // Should get back an error, but this should be fine because all we're looking for is a response (doesn't matter if it's
                // an error response or not). Also, we need to pass in MAP because Apple's bullshit routers give back NATPMP responses when
                // you pass in a PCP ANNOUNCE message.
                UdpRequest udpReq = new UdpRequest(
                        sourceAddress,
                        new InetSocketAddress(gatewayAddress, PORT),
                        new MapPcpRequest(new byte[12], 0, 0, 0, ZERO_IPV6, 0L, ZERO_IPV4),
                        new RequestToBytesTransformer() {
                            @Override
                            public byte[] create(Object request) {
                                return ((MapPcpRequest) request).dump();
                            }
                        },
                        new BytesToResponseTransformer() {
                            @Override
                            public Object create(byte[] buffer) {
                                // so long as version is 2, we can assume that this is a PCP router's response
                                if (buffer.length < 4 || buffer[0] != 2) {
                                    throw new IllegalArgumentException();
                                }

                                MapPcpResponse resp = new MapPcpResponse(buffer);
                                return resp;
                            }
                        });
                
                udpReqs.add(udpReq);
            }
        }
        
        performUdpRequests(networkBus, udpReqs, false, 1000L, 1000L, 1000L, 1000L, 1000L); // don't do standard natpmp/pcp retries -- just
                                                                                           // attempting to discover
        
        
        // Create mappers and returns
        List<PcpPortMapper> mappers = new LinkedList<>();
        for (UdpRequest udpReq : udpReqs) {
            if (udpReq.getResponse() == null) {
                continue;
            }

            PcpPortMapper portMapper = new PcpPortMapper(
                    networkBus,
                    udpReq.getSourceAddress(),
                    udpReq.getDestinationSocketAddress().getAddress());
            mappers.add(portMapper);
        }
        
        return mappers;
    }

    /**
     * Constructs a {@link PcpPortMapper} object.
     * @param networkBus bus to network component
     * @param internalAddress local address accessing gateway device
     * @param gatewayAddress gateway address
     * @throws NullPointerException if any argument other than {@code severName} is {@code null}
     */
    public PcpPortMapper(Bus networkBus, InetAddress internalAddress, InetAddress gatewayAddress) {
        Validate.notNull(networkBus);
        Validate.notNull(internalAddress);
        Validate.notNull(gatewayAddress);

        this.random = new Random();
        this.networkBus = networkBus;
        this.internalAddress = internalAddress;
        this.gatewayAddress = gatewayAddress;
    }


    @Override
    public MappedPort mapPort(PortType portType, int internalPort, int externalPort, long lifetime) throws InterruptedException {
        LOG.info("Attempting to map {} Internal:{} External:{} Lifetime:{}", portType, internalPort, externalPort, lifetime);
        
        Validate.notNull(portType);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);

        //
        // PERFORM MAPPING
        //
        byte[] nonce = nextNonce();
        UdpRequest mapIpReq = createMappingUdpRequest(nonce, portType, internalPort, externalPort, lifetime);
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), false, calculateExponentialBackoffTimes(4));
        if (mapIpReq.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
        MapPcpResponse mappingResp = ((MapPcpResponse) mapIpReq.getResponse());
        
        
        
        MappedPort mappedPort = new PcpMappedPort(nonce, mappingResp.getInternalPort(), mappingResp.getAssignedExternalPort(),
                mappingResp.getAssignedExternalIpAddress(), portType, mappingResp.getLifetime());
        LOG.debug("Map successful {}", mappedPort);
        
        return mappedPort;
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        LOG.info("Attempting to unmap {}", mappedPort);
        
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof PcpMappedPort);

        byte[] nonce = ((PcpMappedPort) mappedPort).getNonce();
        PortType portType = mappedPort.getPortType();
        int internalPort = mappedPort.getInternalPort();
        
        UdpRequest mapIpReq = createMappingUdpRequest(nonce, portType, internalPort, 0, 0L);
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), false, calculateExponentialBackoffTimes(4));
        if (mapIpReq.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
        
        LOG.debug("Unmap successful {}", mappedPort);
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        LOG.info("Attempting to refresh mapping {} for {}", mappedPort, lifetime);
        
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof PcpMappedPort);
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

    private UdpRequest createMappingUdpRequest(byte[] nonce, PortType portType, int internalPort, int externalPort, long lifetime) {
        UdpRequest mapIpReq = new UdpRequest(
                internalAddress,
                new InetSocketAddress(gatewayAddress, PORT),
                new MapPcpRequest(nonce, portType.getProtocolNumber(), internalPort, externalPort, ZERO_IPV6, lifetime, internalAddress),
                new RequestToBytesTransformer() {
                    @Override
                    public byte[] create(Object request) {
                        return ((MapPcpRequest) request).dump();
                    }
                },
                new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        MapPcpResponse resp = new MapPcpResponse(buffer);
                        if (resp.getResultCode() != PcpResultCode.SUCCESS.ordinal()) {
                            throw new IllegalArgumentException();
                        }
                        return resp;
                    }
                });
        return mapIpReq;
    }

    @Override
    public InetAddress getSourceAddress() {
        return internalAddress;
    }

    private byte[] nextNonce() {
        byte[] mappingNonce = new byte[12];
        random.nextBytes(mappingNonce);
        return mappingNonce;
    }

    @Override
    public String toString() {
        return "PcpPortMapper{" + "networkBus=" + networkBus + ", internalAddress=" + internalAddress + ", gatewayAddress="
                + gatewayAddress + ", random=" + random + '}';
    }
    
}
