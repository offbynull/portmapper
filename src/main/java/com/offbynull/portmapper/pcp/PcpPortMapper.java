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
package com.offbynull.portmapper.pcp;

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortMapper;
import com.offbynull.portmapper.PortType;
import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.helpers.TextUtils;
import com.offbynull.portmapper.natpmp.NatPmpPortMapper;
import static com.offbynull.portmapper.pcp.InternalUtils.PRESET_IPV4_GATEWAY_ADDRESSES;
import com.offbynull.portmapper.pcp.InternalUtils.ProcessRequest;
import com.offbynull.portmapper.pcp.InternalUtils.ResponseCreator;
import com.offbynull.portmapper.pcp.InternalUtils.UdpRequest;
import static com.offbynull.portmapper.pcp.InternalUtils.calculateRetryTimes;
import static com.offbynull.portmapper.pcp.InternalUtils.convertToAddressSet;
import static com.offbynull.portmapper.pcp.InternalUtils.getLocalIpAddresses;
import static com.offbynull.portmapper.pcp.InternalUtils.performProcessRequests;
import static com.offbynull.portmapper.pcp.InternalUtils.performUdpRequests;
import com.offbynull.portmapper.pcp.externalmessages.MapPcpRequest;
import com.offbynull.portmapper.pcp.externalmessages.MapPcpResponse;
import com.offbynull.portmapper.pcp.externalmessages.PcpResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * A PCP {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public final class PcpPortMapper implements PortMapper {
    
    private static final int PORT = 5351;
    private static final InetAddress ZERO_IPV6;;
    private static final InetAddress ZERO_IPV4;
    static {
        try {
            ZERO_IPV6 = InetAddress.getByName("::");
            ZERO_IPV4 = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private Bus networkBus;
    private InetAddress internalAddress;
    private InetAddress gatewayAddress;
    private Random random;

    public static Set<NatPmpPortMapper> identify(Bus networkBus, Bus processBus) throws InterruptedException, IOException {
        Validate.notNull(networkBus);
        Validate.notNull(processBus);

        // Perform NETSTAT command
        ProcessRequest netstateReq = new ProcessRequest();
        
        netstateReq.executable = "netstat";
        netstateReq.parameters = new String[] { "-rn" };
        netstateReq.sendData = new byte[0];
        
        List<ProcessRequest> procReqs = Arrays.asList(netstateReq);

        performProcessRequests(processBus, procReqs);
        
        
        // Aggregate results
        Set<InetAddress> potentialGatewayAddresses = new HashSet<>(PRESET_IPV4_GATEWAY_ADDRESSES);
        
        String netstatOutput = new String(netstateReq.recvData, "US-ASCII");
        List<String> netstatIpv4Addresses = TextUtils.findAllIpv4Addresses(netstatOutput);
        List<String> netstatIpv6Addresses = TextUtils.findAllIpv6Addresses(netstatOutput);
        
        potentialGatewayAddresses.addAll(convertToAddressSet(netstatIpv4Addresses));
        potentialGatewayAddresses.addAll(convertToAddressSet(netstatIpv6Addresses));
        
        
        // Query -- send each query to every interface
        List<UdpRequest> udpReqs = new LinkedList<>();
        
        Set<InetAddress> sourceAddresses = getLocalIpAddresses(networkBus);
        for (InetAddress sourceAddress : sourceAddresses) {
            for (InetAddress gatewayAddress : potentialGatewayAddresses) {
                UdpRequest udpReq = new UdpRequest();
                udpReq.sourceAddress = sourceAddress;
                udpReq.destinationSocketAddress = new InetSocketAddress(gatewayAddress, PORT);
                // Send a map pcp request to identify PCP-enabled routers...
                // Should get back an error, but this should be fine because all we're looking for is a response (doesn't matter if it's
                // an error response or not). Also, we need to pass in MAP because Apple's bullshit routers give back NATPMP responses when
                // you pass in a PCP ANNOUNCE message.
                udpReq.sendMsg = new MapPcpRequest(new byte[12], 0, 0, 0, ZERO_IPV6, 0L, ZERO_IPV4);
                udpReq.respCreator = new ResponseCreator() {
                    @Override
                    public PcpResponse create(byte[] buffer) {
                        // so long as version is 2, we can assume that this is a PCP router's response
                        if (buffer.length < 4 || buffer[0] != 2) {
                            throw new IllegalArgumentException();
                        }

                        MapPcpResponse resp = new MapPcpResponse(buffer);
                        return resp;
                    }
                };
                
                udpReqs.add(udpReq);
            }
        }
        
        performUdpRequests(networkBus, udpReqs, 1000L, 1000L, 1000L, 1000L, 1000L); // don't do standard natpmp/pcp retries -- just
                                                                                    // attempting to discover
        
        
        // Create mappers and returns
        Set<NatPmpPortMapper> mappers = new HashSet<>();
        for (UdpRequest udpReq : udpReqs) {
            if (udpReq.respMsg != null) {
                NatPmpPortMapper portMapper = new NatPmpPortMapper(
                        networkBus,
                        udpReq.sourceAddress,
                        udpReq.destinationSocketAddress.getAddress());
                mappers.add(portMapper);
            }
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
        Validate.notNull(portType);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);

        //
        // PERFORM MAPPING
        //
        UdpRequest mapIpReq = new UdpRequest();
        mapIpReq.sourceAddress = internalAddress;
        mapIpReq.destinationSocketAddress = new InetSocketAddress(gatewayAddress, PORT);
        mapIpReq.sendMsg = new MapPcpRequest(nextNonce(), portType.getProtocolNumber(), internalPort, externalPort, ZERO_IPV6, lifetime,
                internalAddress);
        mapIpReq.respCreator = new ResponseCreator() {
            @Override
            public PcpResponse create(byte[] buffer) {
                MapPcpResponse resp = new MapPcpResponse(buffer);
                if (resp.getResultCode() != PcpResultCode.SUCCESS.ordinal()) {
                    throw new IllegalArgumentException();
                }
                return resp;
            }
        };
        
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), calculateRetryTimes(9));

        if (mapIpReq.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
        
        MapPcpResponse mappingResp = ((MapPcpResponse) mapIpReq.respMsg);
        
        
        
        return new PcpMappedPort(mappingResp.getInternalPort(), mappingResp.getAssignedExternalPort(),
                mappingResp.getAssignedExternalIpAddress(), portType, mappingResp.getLifetime());
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof PcpMappedPort);

        int internalPort = mappedPort.getInternalPort();
        
        UdpRequest mapIpReq = new UdpRequest();
        mapIpReq.sourceAddress = internalAddress;
        mapIpReq.destinationSocketAddress = new InetSocketAddress(gatewayAddress, PORT);
        mapIpReq.sendMsg = new MapPcpRequest(nextNonce(), mappedPort.getPortType().getProtocolNumber(), internalPort, 0, ZERO_IPV6, 0L,
                internalAddress);
        mapIpReq.respCreator = new ResponseCreator() {
            @Override
            public PcpResponse create(byte[] buffer) {
                MapPcpResponse resp = new MapPcpResponse(buffer);
                if (resp.getResultCode() != PcpResultCode.SUCCESS.ordinal()) {
                    throw new IllegalArgumentException();
                }
                return resp;
            }
        };
        
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), calculateRetryTimes(9));

        if (mapIpReq.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof PcpMappedPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
        return mapPort(mappedPort.getPortType(), mappedPort.getInternalPort(), mappedPort.getExternalPort(), lifetime);
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
}
