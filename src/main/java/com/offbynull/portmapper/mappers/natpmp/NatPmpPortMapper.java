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
package com.offbynull.portmapper.mappers.natpmp;

import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.helpers.TextUtils;
import com.offbynull.portmapper.mapper.MappedPort;
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
import com.offbynull.portmapper.mapper.PortType;
import static com.offbynull.portmapper.mapper.PortType.TCP;
import static com.offbynull.portmapper.mapper.PortType.UDP;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.ExternalAddressNatPmpRequest;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.ExternalAddressNatPmpResponse;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.MappingNatPmpRequest;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.MappingNatPmpResponse;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.TcpMappingNatPmpRequest;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.TcpMappingNatPmpResponse;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.UdpMappingNatPmpRequest;
import com.offbynull.portmapper.mappers.natpmp.externalmessages.UdpMappingNatPmpResponse;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A NAT-PMP {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public final class NatPmpPortMapper implements PortMapper {
    private static final Logger LOG = LoggerFactory.getLogger(NatPmpPortMapper.class);
    
    private static final int PORT = 5351;
    private Bus networkBus;
    private InetAddress internalAddress;
    private InetAddress gatewayAddress;

    /**
     * Identify NAT-PMP devices on all interfaces.
     * <p>
     * Since NAT-PMP devices have no broadcast discovery mechanism, this method makes use of multiple OS-specific commands to try to find
     * the addresses of gateways. This may is unlikely to change in the future as this is a deprecated protocol. However, once
     * <a href="https://tools.ietf.org/html/draft-ietf-pcp-anycast-08">this RFC draft</a> is implemented, PCP devices that implement that
     * RFC + also support NAT-PMP may be discoverable via anycast/broadcast.
     * @param networkBus network bus
     * @param processBus process bus
     * @param additionalIps additional IPs to check
     * @return set of found PCP devices
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalStateException if encountered issue with gateways
     * @throws InterruptedException if interrupted
     */
    public static List<NatPmpPortMapper> identify(Bus networkBus, Bus processBus, InetAddress ... additionalIps) throws InterruptedException {
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
            List<String> netstatIpv4Addresses = TextUtils.findAllIpv4Addresses(req.getOutput());
            List<String> netstatIpv6Addresses = TextUtils.findAllIpv6Addresses(req.getOutput());

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
                
                // avoid sending anything to 127.x.x.x or ::1, these are loopback addresses and cause the socket to throw an IOException on
                // send for whatever reason when we try to send to it -- which in turn causes the socket to close and none of the other
                // messages will get sent
                if (gatewayAddress.isLoopbackAddress()) {
                    continue;
                }

                UdpRequest udpReq = createGetExternalIpUdpRequest(sourceAddress, gatewayAddress);
                udpReqs.add(udpReq);
            }
        }
        
        performUdpRequests(networkBus, udpReqs, false, 1000L, 1000L, 1000L, 1000L, 1000L); // don't do standard natpmp/pcp retries -- just
                                                                                           // attempting to discover
        
        
        // Create mappers and returns
        List<NatPmpPortMapper> mappers = new LinkedList<>();
        for (UdpRequest udpReq : udpReqs) {
            if (udpReq.getResponse() != null) {
                NatPmpPortMapper portMapper = new NatPmpPortMapper(
                        networkBus,
                        udpReq.getSourceAddress(),
                        udpReq.getDestinationSocketAddress().getAddress());
                mappers.add(portMapper);
            }
        }
        
        return mappers;
    }

    /**
     * Constructs a {@link NatPmpPortMapper} object.
     * @param networkBus bus to network component
     * @param internalAddress local address accessing gateway device
     * @param gatewayAddress gateway address
     * @throws NullPointerException if any argument other than {@code severName} is {@code null}
     */
    public NatPmpPortMapper(Bus networkBus, InetAddress internalAddress, InetAddress gatewayAddress) {
        Validate.notNull(networkBus);
        Validate.notNull(internalAddress);
        Validate.notNull(gatewayAddress);

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

        UdpRequest externalIpReq = createGetExternalIpUdpRequest(internalAddress, gatewayAddress);
        performUdpRequests(networkBus, Collections.singleton(externalIpReq), false, calculateExponentialBackoffTimes(4));
        if (externalIpReq.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to getting external IP");
        }
        InetAddress externalAddress = ((ExternalAddressNatPmpResponse) externalIpReq.getResponse()).getAddress();
        
        
        
        UdpRequest mapIpReq = createMappingUdpRequest(internalAddress, gatewayAddress, portType, internalPort, externalPort, lifetime);
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), false, calculateExponentialBackoffTimes(4));
        if (mapIpReq.getResponse() == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
        MappingNatPmpResponse mappingResp = ((MappingNatPmpResponse) mapIpReq.getResponse());
        
        
        
        MappedPort mappedPort = new NatPmpMappedPort(mappingResp.getInternalPort(), mappingResp.getExternalPort(), externalAddress,
                portType, mappingResp.getLifetime());
        LOG.debug("Map successful {}", mappedPort);
        
        return mappedPort;
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        LOG.info("Attempting to unmap {}", mappedPort);
        
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof NatPmpMappedPort);

        PortType portType = mappedPort.getPortType();
        int internalPort = mappedPort.getInternalPort();
        
        UdpRequest mapIpReq = createMappingUdpRequest(internalAddress, gatewayAddress, portType, internalPort, 0, 0L);
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
        Validate.isTrue(mappedPort instanceof NatPmpMappedPort);
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

    private static UdpRequest createGetExternalIpUdpRequest(InetAddress internalAddress, InetAddress gatewayAddress) {
        UdpRequest externalIpReq = new UdpRequest(
                internalAddress,
                new InetSocketAddress(gatewayAddress, PORT),
                new ExternalAddressNatPmpRequest(),
                new RequestToBytesTransformer() {
                    @Override
                    public byte[] create(Object request) {
                        return ((ExternalAddressNatPmpRequest) request).dump();
                    }
                },
                new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(buffer);
                        if (resp.getResultCode() != NatPmpResultCode.SUCCESS.ordinal()) {
                            throw new IllegalArgumentException();
                        }
                        return resp;
                    }
                });
        return externalIpReq;
    }

    private static UdpRequest createMappingUdpRequest(InetAddress internalAddress, InetAddress gatewayAddress, PortType portType,
            int internalPort, int externalPort, long lifetime) {
        MappingNatPmpRequest request;
        RequestToBytesTransformer requestToBytesTransformer;
        BytesToResponseTransformer bytesToResponseTransformer;
        switch (portType) {
            case TCP:
                request = new TcpMappingNatPmpRequest(internalPort, externalPort, lifetime);
                requestToBytesTransformer = new RequestToBytesTransformer() {
                    @Override
                    public byte[] create(Object request) {
                        return ((TcpMappingNatPmpRequest) request).dump();
                    }
                };
                bytesToResponseTransformer = new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        TcpMappingNatPmpResponse resp = new TcpMappingNatPmpResponse(buffer);
                        if (resp.getResultCode() != NatPmpResultCode.SUCCESS.ordinal()) {
                            throw new IllegalArgumentException();
                        }
                        return resp;
                    }
                };
                break;
            case UDP:
                request = new UdpMappingNatPmpRequest(internalPort, externalPort, lifetime);
                requestToBytesTransformer = new RequestToBytesTransformer() {
                    @Override
                    public byte[] create(Object request) {
                        return ((UdpMappingNatPmpRequest) request).dump();
                    }
                };
                bytesToResponseTransformer = new BytesToResponseTransformer() {
                    @Override
                    public Object create(byte[] buffer) {
                        UdpMappingNatPmpResponse resp = new UdpMappingNatPmpResponse(buffer);
                        if (resp.getResultCode() != NatPmpResultCode.SUCCESS.ordinal()) {
                            throw new IllegalArgumentException();
                        }
                        return resp;
                    }
                };
                break;
            default:
                throw new IllegalStateException(); // should never happen
        }
        
        UdpRequest mapIpReq = new UdpRequest(
                internalAddress,
                new InetSocketAddress(gatewayAddress, PORT),
                request,
                requestToBytesTransformer,
                bytesToResponseTransformer);
        
        return mapIpReq;
    }

    @Override
    public InetAddress getSourceAddress() {
        return internalAddress;
    }

    @Override
    public String toString() {
        return "NatPmpPortMapper{" + "networkBus=" + networkBus + ", internalAddress=" + internalAddress + ", gatewayAddress="
                + gatewayAddress + '}';
    }

}
