/*
 * Copyright (c) 2013-2014, Kasra Faghihi, All rights reserved.
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
import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.common.TextUtils;
import static com.offbynull.portmapper.natpmp.InternalUtils.PRESET_IPV4_GATEWAY_ADDRESSES;
import com.offbynull.portmapper.natpmp.InternalUtils.ProcessRequest;
import com.offbynull.portmapper.natpmp.InternalUtils.ResponseCreator;
import com.offbynull.portmapper.natpmp.InternalUtils.UdpRequest;
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
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

    public static Set<NatPmpPortMapper> identify(Bus networkBus, Bus processBus) throws InterruptedException, IOException {
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
        
        performUdpRequests(networkBus, udpReqs, 1000L, 1000L, 1000L, 1000L, 1000L);
        
        
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
        
        performUdpRequests(networkBus, Collections.singleton(externalIpReq), 1000L, 1000L, 1000L, 1000L, 1000L);
        
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
        
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), 1000L, 1000L, 1000L, 1000L, 1000L);

        if (mapIpReq.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
        
        MappingNatPmpResponse mappingResp = ((MappingNatPmpResponse) mapIpReq.respMsg);
        
        
        
        return new NatPmpMappedPort(mappingResp.getInternalPort(), mappingResp.getExternalPort(), externalAddress, portType,
                mappingResp.getLifetime());
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
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
        
        performUdpRequests(networkBus, Collections.singleton(mapIpReq), 1000L, 1000L, 1000L, 1000L, 1000L);

        if (mapIpReq.respMsg == null) {
            throw new IllegalStateException("No response/invalid response to mapping port");
        }
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        return mapPort(mappedPort.getPortType(), mappedPort.getInternalPort(), mappedPort.getExternalPort(), lifetime);
    }

    @Override
    public InetAddress getSourceAddress() {
        return internalAddress;
    }

//    @Override
//    public MappedPort mapPort(PortType portType, int internalPort, long lifetime) throws InterruptedException {
//        Validate.validState(!closed);
//        Validate.notNull(portType);
//        Validate.inclusiveBetween(1, 65535, internalPort);
//        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
//
//        lifetime = Math.min(lifetime, (long) Integer.MAX_VALUE); // cap lifetime
//
//        ExternalAddressNatPmpResponse extAddrResp;
//        try {
//            extAddrResp = controller.requestExternalAddress(4);
//        } catch (RuntimeException re) {
//            throw new IllegalStateException(re);
//        }
//            
//        switch (portType) {
//            case TCP: {
//                try {
//                    TcpMappingNatPmpResponse tcpMapResp = controller.requestTcpMappingOperation(4, internalPort, 0, lifetime);
//                    return new MappedPort(tcpMapResp.getInternalPort(), tcpMapResp.getExternalPort(),
//                            extAddrResp.getAddress(), PortType.TCP, tcpMapResp.getLifetime());
//                } catch (RuntimeException re) {
//                    throw new IllegalStateException(re);
//                }
//            }
//            case UDP: {
//                try {
//                    UdpMappingNatPmpResponse udpMapResp = controller.requestUdpMappingOperation(4, internalPort, 0, lifetime);
//                    return new MappedPort(udpMapResp.getInternalPort(), udpMapResp.getExternalPort(),
//                            extAddrResp.getAddress(), PortType.TCP, udpMapResp.getLifetime());
//                } catch (RuntimeException re) {
//                    throw new IllegalStateException(re);
//                }
//            }
//            default:
//                throw new IllegalStateException(); // should never happen
//       }
//        return null;
//    }

//    @Override
//    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
//        Validate.validState(!closed);
//        Validate.notNull(mappedPort);
//            
//        switch (mappedPort.getPortType()) {
//            case TCP: {
//                try {
//                    controller.requestTcpMappingOperation(4, mappedPort.getInternalPort(), 0, 0);
//                } catch (RuntimeException re) {
//                    throw new IllegalStateException(re);
//                }
//                break;
//            }
//            case UDP: {
//                try {
//                    controller.requestUdpMappingOperation(4, mappedPort.getInternalPort(), 0, 0);
//                } catch (RuntimeException re) {
//                    throw new IllegalStateException(re);
//                }
//                break;
//            }
//            default:
//                throw new IllegalStateException(); // should never happen
//       }
//    }

//    @Override
//    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
//        Validate.validState(!closed);
//        Validate.notNull(mappedPort);
//        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
//
//        lifetime = Math.min(lifetime, (long) Integer.MAX_VALUE); // cap lifetime
//
//        ExternalAddressNatPmpResponse extAddrResp;
//        try {
//            extAddrResp = controller.requestExternalAddress(4);
//        } catch (RuntimeException re) {
//            throw new IllegalStateException(re);
//        }
//
//        MappedPort newMappedPort;
//        switch (mappedPort.getPortType()) {
//            case TCP: {
//                try {
//                    TcpMappingNatPmpResponse tcpMapResp = controller.requestTcpMappingOperation(4, mappedPort.getInternalPort(),
//                            mappedPort.getExternalPort(), lifetime);
//                    newMappedPort = new MappedPort(tcpMapResp.getInternalPort(), tcpMapResp.getExternalPort(),
//                            extAddrResp.getAddress(), PortType.TCP, tcpMapResp.getLifetime());
//                } catch (RuntimeException re) {
//                    throw new IllegalStateException(re);
//                }
//                break;
//            }
//            case UDP: {
//                try {
//                    UdpMappingNatPmpResponse udpMapResp = controller.requestUdpMappingOperation(4, mappedPort.getInternalPort(),
//                            mappedPort.getExternalPort(), lifetime);
//                    newMappedPort = new MappedPort(udpMapResp.getInternalPort(), udpMapResp.getExternalPort(),
//                            extAddrResp.getAddress(), PortType.TCP, udpMapResp.getLifetime());
//                } catch (RuntimeException re) {
//                    throw new IllegalStateException(re);
//                }
//                break;
//            }
//            default:
//                throw new IllegalStateException(); // should never happen
//        }
//        
//        try {
//            Validate.isTrue(mappedPort.getExternalAddress().equals(mappedPort.getExternalAddress()), "External address changed");
//            Validate.isTrue(mappedPort.getInternalPort() == mappedPort.getInternalPort(), "External port changed");
//        } catch (IllegalStateException ise) {
//            // port has been mapped to different external ip and/or port, unmap and return error
//            try {
//                unmapPort(newMappedPort);
//            } catch (RuntimeException re) { // NOPMD
//                // do nothing
//            }
//            
//            throw ise;
//        }
//
//
//        return newMappedPort;
//        return null;
//    }
}
