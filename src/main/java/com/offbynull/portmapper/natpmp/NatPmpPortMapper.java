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
package com.offbynull.portmapper.natpmp;

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortMapper;
import com.offbynull.portmapper.PortType;
import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.helpers.TextUtils;
import static com.offbynull.portmapper.natpmp.InternalUtils.PRESET_IPV4_GATEWAY_ADDRESSES;
import com.offbynull.portmapper.natpmp.InternalUtils.ProcessRequest;
import com.offbynull.portmapper.natpmp.InternalUtils.ResponseCreator;
import com.offbynull.portmapper.natpmp.InternalUtils.UdpRequest;
import static com.offbynull.portmapper.natpmp.InternalUtils.calculateRetryTimes;
import static com.offbynull.portmapper.natpmp.InternalUtils.convertToAddressSet;
import static com.offbynull.portmapper.natpmp.InternalUtils.getLocalIpAddresses;
import static com.offbynull.portmapper.natpmp.InternalUtils.performProcessRequests;
import static com.offbynull.portmapper.natpmp.InternalUtils.performUdpRequests;
import com.offbynull.portmapper.natpmp.externalmessages.ExternalAddressNatPmpRequest;
import com.offbynull.portmapper.natpmp.externalmessages.ExternalAddressNatPmpResponse;
import com.offbynull.portmapper.natpmp.externalmessages.MappingNatPmpResponse;
import com.offbynull.portmapper.natpmp.externalmessages.NatPmpResponse;
import com.offbynull.portmapper.natpmp.externalmessages.TcpMappingNatPmpRequest;
import com.offbynull.portmapper.natpmp.externalmessages.TcpMappingNatPmpResponse;
import com.offbynull.portmapper.natpmp.externalmessages.UdpMappingNatPmpRequest;
import com.offbynull.portmapper.natpmp.externalmessages.UdpMappingNatPmpResponse;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * A NAT-PMP {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public final class NatPmpPortMapper implements PortMapper {
    
    private static final int PORT = 5351;
    private Bus networkBus;
    private InetAddress internalAddress;
    private InetAddress gatewayAddress;

    public static Set<NatPmpPortMapper> identify(Bus networkBus, Bus processBus) throws InterruptedException {
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
        
        String netstatOutput = new String(netstateReq.recvData, Charset.forName("US-ASCII"));
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
                udpReq.sendMsg = new ExternalAddressNatPmpRequest();
                udpReq.respCreator = new ResponseCreator() {
                    @Override
                    public NatPmpResponse create(byte[] buffer) {
                        ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(buffer);
                        if (resp.getResultCode() != NatPmpResultCode.SUCCESS.ordinal()) {
                            throw new IllegalArgumentException();
                        }
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
        Validate.notNull(portType);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);

        //
        // GET EXTERNAL IP
        //
        UdpRequest externalIpReq = new UdpRequest();
        externalIpReq.sourceAddress = internalAddress;
        externalIpReq.destinationSocketAddress = new InetSocketAddress(gatewayAddress, PORT);
        externalIpReq.sendMsg = new ExternalAddressNatPmpRequest();
        externalIpReq.respCreator = new ResponseCreator() {
            @Override
            public NatPmpResponse create(byte[] buffer) {
                ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(buffer);
                if (resp.getResultCode() != NatPmpResultCode.SUCCESS.ordinal()) {
                    throw new IllegalArgumentException();
                }
                return resp;
            }
        };
        
        performUdpRequests(networkBus, Collections.singleton(externalIpReq), calculateRetryTimes(9));
        
        if (externalIpReq.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to getting external IP");
        }
        
        InetAddress externalAddress = ((ExternalAddressNatPmpResponse) externalIpReq.respMsg).getAddress();
        
        
        
        //
        // PERFORM MAPPING
        //
        UdpRequest mapIpReq = new UdpRequest();
        mapIpReq.sourceAddress = internalAddress;
        mapIpReq.destinationSocketAddress = new InetSocketAddress(gatewayAddress, PORT);
        switch (portType) {
            case TCP:
                mapIpReq.sendMsg = new TcpMappingNatPmpRequest(internalPort, externalPort, lifetime);
                mapIpReq.respCreator = new ResponseCreator() {
                    @Override
                    public NatPmpResponse create(byte[] buffer) {
                        TcpMappingNatPmpResponse resp = new TcpMappingNatPmpResponse(buffer);
                        if (resp.getResultCode() != NatPmpResultCode.SUCCESS.ordinal()) {
                            throw new IllegalArgumentException();
                        }
                        return resp;
                    }
                };
                break;
            case UDP:
                mapIpReq.sendMsg = new UdpMappingNatPmpRequest(internalPort, externalPort, lifetime);
                mapIpReq.respCreator = new ResponseCreator() {
                    @Override
                    public NatPmpResponse create(byte[] buffer) {
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
        
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), calculateRetryTimes(9));

        if (mapIpReq.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
        
        MappingNatPmpResponse mappingResp = ((MappingNatPmpResponse) mapIpReq.respMsg);
        
        
        
        return new NatPmpMappedPort(mappingResp.getInternalPort(), mappingResp.getExternalPort(), externalAddress, portType,
                mappingResp.getLifetime());
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof NatPmpMappedPort);

        int internalPort = mappedPort.getInternalPort();
        
        UdpRequest mapIpReq = new UdpRequest();
        mapIpReq.sourceAddress = internalAddress;
        mapIpReq.destinationSocketAddress = new InetSocketAddress(gatewayAddress, PORT);
        switch (mappedPort.getPortType()) {
            case TCP:
                mapIpReq.sendMsg = new TcpMappingNatPmpRequest(internalPort, 0, 0L);
                mapIpReq.respCreator = new ResponseCreator() {
                    @Override
                    public NatPmpResponse create(byte[] buffer) {
                        TcpMappingNatPmpResponse resp = new TcpMappingNatPmpResponse(buffer);
                        if (resp.getResultCode() != NatPmpResultCode.SUCCESS.ordinal()) {
                            throw new IllegalArgumentException();
                        }
                        return resp;
                    }
                };
                break;
            case UDP:
                mapIpReq.sendMsg = new UdpMappingNatPmpRequest(internalPort, 0, 0L);
                mapIpReq.respCreator = new ResponseCreator() {
                    @Override
                    public NatPmpResponse create(byte[] buffer) {
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
        
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), calculateRetryTimes(9));

        if (mapIpReq.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        Validate.notNull(mappedPort);
        Validate.isTrue(mappedPort instanceof NatPmpMappedPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
        MappedPort newMappedPort = mapPort(mappedPort.getPortType(), mappedPort.getInternalPort(), mappedPort.getExternalPort(), lifetime);
        
        if (mappedPort.getExternalPort() != newMappedPort.getExternalPort()
                || !Objects.equals(mappedPort.getExternalAddress(), newMappedPort.getExternalAddress())) {
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
        
        return newMappedPort;
    }

    @Override
    public InetAddress getSourceAddress() {
        return internalAddress;
    }

}
