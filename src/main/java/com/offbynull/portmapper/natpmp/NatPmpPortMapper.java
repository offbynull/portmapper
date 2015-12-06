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

import com.offbynull.portmapper.natpmp.messages.TcpMappingNatPmpResponse;
import com.offbynull.portmapper.natpmp.messages.UdpMappingNatPmpResponse;
import com.offbynull.portmapper.natpmp.messages.NatPmpResponse;
import com.offbynull.portmapper.natpmp.messages.ExternalAddressNatPmpResponse;
import com.offbynull.portmapper.common.CommunicationType;
import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortMapper;
import com.offbynull.portmapper.PortMapperEventListener;
import com.offbynull.portmapper.PortType;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;

/**
 * A NAT-PMP {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public final class NatPmpPortMapper implements PortMapper {

    private NatPmpController controller;
    private volatile boolean closed;
    

    /**
     * Constructs a {@link NatPmpPortMapper} object.
     * @param gatewayAddress gateway address
     * @param listener event listener
     * @throws NullPointerException if any argument is {@code null}
     * @throws IOException if problems initializing UDP channels
     */
    public NatPmpPortMapper(InetAddress gatewayAddress, final PortMapperEventListener listener) throws IOException {
        Validate.notNull(gatewayAddress);
        Validate.notNull(listener);

        controller = new NatPmpController(gatewayAddress, new NatPmpControllerListener() {
            private boolean lastAvailable;
            private long lastEpoch;
            private long lastRecvTime;

            @Override
            public void incomingResponse(CommunicationType type, NatPmpResponse response) {
                if (closed) {
                    return;
                }

                if (type == CommunicationType.MULTICAST && response instanceof ExternalAddressNatPmpResponse) {
                    listener.resetRequired("Mappings may have been lost via external IP address change.");
                    return;
                }
                
                // As described in section 3.6 of the RFC
                if (!lastAvailable) {
                    lastRecvTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                    lastEpoch = response.getSecondsSinceStartOfEpoch();
                    lastAvailable = true;
                } else {
                    long recvTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                    long epoch = response.getSecondsSinceStartOfEpoch();

                    long elapsedTime = Math.max(0L, recvTime - lastRecvTime); // max just in case
                    long epochWindow = elapsedTime * 7L / 8L;

                    long minEpoch = (lastEpoch + epochWindow - 2L) & 0xFFFFFFFFL; // add and truncate top 32bits

                    if (epoch < minEpoch) {
                        listener.resetRequired("Mappings may have been lost via device reset.");
                        return;
                    }
                    
                    lastRecvTime = recvTime;
                    lastEpoch = epoch;
                }
            }
        });
    }

    @Override
    public MappedPort mapPort(PortType portType, int internalPort, long lifetime) throws InterruptedException {
        Validate.validState(!closed);
        Validate.notNull(portType);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);

        lifetime = Math.min(lifetime, (long) Integer.MAX_VALUE); // cap lifetime

        ExternalAddressNatPmpResponse extAddrResp;
        try {
            extAddrResp = controller.requestExternalAddress(4);
        } catch (RuntimeException re) {
            throw new IllegalStateException(re);
        }
            
        switch (portType) {
            case TCP: {
                try {
                    TcpMappingNatPmpResponse tcpMapResp = controller.requestTcpMappingOperation(4, internalPort, 0, lifetime);
                    return new MappedPort(tcpMapResp.getInternalPort(), tcpMapResp.getExternalPort(),
                            extAddrResp.getAddress(), PortType.TCP, tcpMapResp.getLifetime());
                } catch (RuntimeException re) {
                    throw new IllegalStateException(re);
                }
            }
            case UDP: {
                try {
                    UdpMappingNatPmpResponse udpMapResp = controller.requestUdpMappingOperation(4, internalPort, 0, lifetime);
                    return new MappedPort(udpMapResp.getInternalPort(), udpMapResp.getExternalPort(),
                            extAddrResp.getAddress(), PortType.TCP, udpMapResp.getLifetime());
                } catch (RuntimeException re) {
                    throw new IllegalStateException(re);
                }
            }
            default:
                throw new IllegalStateException(); // should never happen
       }
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        Validate.validState(!closed);
        Validate.notNull(mappedPort);
            
        switch (mappedPort.getPortType()) {
            case TCP: {
                try {
                    controller.requestTcpMappingOperation(4, mappedPort.getInternalPort(), 0, 0);
                } catch (RuntimeException re) {
                    throw new IllegalStateException(re);
                }
                break;
            }
            case UDP: {
                try {
                    controller.requestUdpMappingOperation(4, mappedPort.getInternalPort(), 0, 0);
                } catch (RuntimeException re) {
                    throw new IllegalStateException(re);
                }
                break;
            }
            default:
                throw new IllegalStateException(); // should never happen
       }
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        Validate.validState(!closed);
        Validate.notNull(mappedPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);

        lifetime = Math.min(lifetime, (long) Integer.MAX_VALUE); // cap lifetime

        ExternalAddressNatPmpResponse extAddrResp;
        try {
            extAddrResp = controller.requestExternalAddress(4);
        } catch (RuntimeException re) {
            throw new IllegalStateException(re);
        }

        MappedPort newMappedPort;
        switch (mappedPort.getPortType()) {
            case TCP: {
                try {
                    TcpMappingNatPmpResponse tcpMapResp = controller.requestTcpMappingOperation(4, mappedPort.getInternalPort(),
                            mappedPort.getExternalPort(), lifetime);
                    newMappedPort = new MappedPort(tcpMapResp.getInternalPort(), tcpMapResp.getExternalPort(),
                            extAddrResp.getAddress(), PortType.TCP, tcpMapResp.getLifetime());
                } catch (RuntimeException re) {
                    throw new IllegalStateException(re);
                }
                break;
            }
            case UDP: {
                try {
                    UdpMappingNatPmpResponse udpMapResp = controller.requestUdpMappingOperation(4, mappedPort.getInternalPort(),
                            mappedPort.getExternalPort(), lifetime);
                    newMappedPort = new MappedPort(udpMapResp.getInternalPort(), udpMapResp.getExternalPort(),
                            extAddrResp.getAddress(), PortType.TCP, udpMapResp.getLifetime());
                } catch (RuntimeException re) {
                    throw new IllegalStateException(re);
                }
                break;
            }
            default:
                throw new IllegalStateException(); // should never happen
        }
        
        try {
            Validate.isTrue(mappedPort.getExternalAddress().equals(mappedPort.getExternalAddress()), "External address changed");
            Validate.isTrue(mappedPort.getInternalPort() == mappedPort.getInternalPort(), "External port changed");
        } catch (IllegalStateException ise) {
            // port has been mapped to different external ip and/or port, unmap and return error
            try {
                unmapPort(newMappedPort);
            } catch (RuntimeException re) { // NOPMD
                // do nothing
            }
            
            throw ise;
        }


        return newMappedPort;
    }
    
    @Override
    public void close() throws IOException {
        closed = true;
        controller.close();
    }
}
