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
package com.offbynull.portmapper.mappers.pcp;

import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.mapper.PortType;
import com.offbynull.portmapper.gateway.Bus;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV4;
import static com.offbynull.portmapper.helpers.NetworkUtils.ZERO_IPV6;
import com.offbynull.portmapper.helpers.TextUtils;
import com.offbynull.portmapper.mapper.MapperIoUtils.BytesToResponseTransformer;
import static com.offbynull.portmapper.mapper.MapperIoUtils.PRESET_IPV4_GATEWAY_ADDRESSES;
import com.offbynull.portmapper.mapper.MapperIoUtils.RequestToBytesTransformer;
import com.offbynull.portmapper.mapper.MapperIoUtils.UdpRequest;
import static com.offbynull.portmapper.mapper.MapperIoUtils.calculateExponentialBackoffTimes;
import static com.offbynull.portmapper.mapper.MapperIoUtils.convertToAddressSet;
import static com.offbynull.portmapper.mapper.MapperIoUtils.getLocalIpAddresses;
import static com.offbynull.portmapper.mapper.MapperIoUtils.performUdpRequests;
import com.offbynull.portmapper.mappers.pcp.InternalUtils.RunProcessRequest;
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
import static com.offbynull.portmapper.mappers.pcp.InternalUtils.runCommandline;
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
     * @return set of found PCP devices
     * @throws NullPointerException if any argument is {@code null}
     * @throws InterruptedException if interrupted
     */
    public static List<PcpPortMapper> identify(Bus networkBus, Bus processBus) throws InterruptedException {
        LOG.info("Attempting to identify devices");
        
        Validate.notNull(networkBus);
        Validate.notNull(processBus);

        // Perform commands to try to grab gateway addresses
        Set<String> cliOutputs = runCommandline(processBus,
                new RunProcessRequest("netstat", "-rn"), //linux mac and windows -- but seems wrong for windows
                new RunProcessRequest("route", "-n"), // linux
                new RunProcessRequest("route", "-n", "get", "default"), // mac
                new RunProcessRequest("ipconfig"), // windows
                new RunProcessRequest("ifconfig")); // linux (and mac?)

        
        
        
        // Aggregate results
        Set<InetAddress> potentialGatewayAddresses = new HashSet<>(PRESET_IPV4_GATEWAY_ADDRESSES);
        
        for (String cliOutput : cliOutputs) {
            List<String> netstatIpv4Addresses = TextUtils.findAllIpv4Addresses(cliOutput);
            List<String> netstatIpv6Addresses = TextUtils.findAllIpv6Addresses(cliOutput);

            potentialGatewayAddresses.addAll(convertToAddressSet(netstatIpv4Addresses));
            potentialGatewayAddresses.addAll(convertToAddressSet(netstatIpv6Addresses));
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