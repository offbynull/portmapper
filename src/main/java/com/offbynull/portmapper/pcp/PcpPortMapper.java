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
package com.offbynull.portmapper.pcp;

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortMapper;
import com.offbynull.portmapper.PortMapperEventListener;
import com.offbynull.portmapper.PortType;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.commons.lang3.Validate;

/**
 * A PCP {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public final class PcpPortMapper implements PortMapper {

    private PcpController controller;
    private boolean preferIpv6External;
    private volatile boolean closed;

    /**
     * Constructs a {@link PcpPortMapper} object.
     * @param gatewayAddress gateway address
     * @param selfAddress address of this machine on the interface that can talk to the router/gateway
     * @param preferIpv6External if this mapper should tell the router to give it a ipv6 address when asking the router to map a new port
     * @param listener event listener
     * @throws NullPointerException if any argument is {@code null}
     * @throws IOException if problems initializing UDP channels
     */
    public PcpPortMapper(InetAddress gatewayAddress, InetAddress selfAddress, boolean preferIpv6External,
            final PortMapperEventListener listener) throws IOException {
        Validate.notNull(gatewayAddress);
        Validate.notNull(selfAddress);
        Validate.notNull(listener);

        this.preferIpv6External = preferIpv6External;

//        controller = new PcpController(new Random(), gatewayAddress, selfAddress, new PcpControllerListener() {
//
//            private boolean lastAvailable;
//            private long lastEpoch;
//            private long lastRecvTime;
//
//            @Override
//            public void incomingResponse(CommunicationType type, PcpResponse response) {
//                if (closed) {
//                    return;
//                }
//
//                if (type == CommunicationType.MULTICAST && response instanceof AnnouncePcpResponse) {
//                    listener.resetRequired("Mappings may have been lost via device reset and/or external IP change.");
//                    return;
//                }
//                
//                // As described in section 8.5 of the RFC
//                if (!lastAvailable) {
//                    lastRecvTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
//                    lastEpoch = response.getEpochTime();
//                    lastAvailable = true;
//                } else {
//                    long recvTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
//                    long epoch = response.getEpochTime();
//                    
//                    if (epoch < lastEpoch - 1) {
//                        listener.resetRequired("Mappings may have been lost via device reset.");
//                        return;
//                    }
//
//                    long clientDelta = Math.max(0L, recvTime - lastRecvTime); // max just in case
//                    long serverDelta = epoch - lastEpoch;
//                    
//                    if (clientDelta + 2L < serverDelta - serverDelta / 16
//                            || serverDelta + 2L < clientDelta - clientDelta / 16) {
//                        listener.resetRequired("Mappings may have been lost via device reset.");
//                        return;
//                    }
//                    
//                    lastRecvTime = recvTime;
//                    lastEpoch = epoch;
//                }                
//            }
//        });
    }

    @Override
    public MappedPort mapPort(PortType portType, int internalPort, long lifetime) throws InterruptedException {
//        Validate.validState(!closed);
//        Validate.notNull(portType);
//        Validate.inclusiveBetween(1, 65535, internalPort);
//        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
//
//        lifetime = Math.min(lifetime, (long) Integer.MAX_VALUE); // cap lifetime
//
//        InetAddress externalAddress;
//        try {
//            if (preferIpv6External) {
//                externalAddress = InetAddress.getByName("::");
//            } else {
//                externalAddress = InetAddress.getByName("::ffff:0:0"); // NOPMD
//            }
//        } catch (UnknownHostException uhe) {
//            throw new IllegalStateException(uhe);
//        }
//
//        try {
//            MapPcpResponse resp = controller.requestMapOperation(4, portType, internalPort, 0, externalAddress, lifetime);
//
//            return new MappedPort(resp.getInternalPort(), resp.getAssignedExternalPort(), resp.getAssignedExternalIpAddress(),
//                    PortType.fromIanaNumber(resp.getProtocol()), resp.getLifetime());
//        } catch (RuntimeException re) {
//            throw new IllegalStateException(re);
//        }
        return null;
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
//        Validate.validState(!closed);
//        Validate.notNull(mappedPort);
//
//        InetAddress externalAddress;
//        try {
//            externalAddress = InetAddress.getByName("::");
//        } catch (UnknownHostException uhe) {
//            throw new IllegalStateException(uhe);
//        }
//
//        try {
//            controller.requestMapOperation(4, mappedPort.getPortType(), mappedPort.getInternalPort(), 0, externalAddress, 0L);
//        } catch (RuntimeException re) {
//            throw new IllegalStateException(re);
//        }
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
//        Validate.validState(!closed);
//        Validate.notNull(mappedPort);
//        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
//        
//        lifetime = Math.min(lifetime, (long) Integer.MAX_VALUE); // cap lifetime
//        
//        MappedPort newMappedPort;
//        try {
//            MapPcpResponse resp = controller.requestMapOperation(4, mappedPort.getPortType(), mappedPort.getInternalPort(),
//                    mappedPort.getExternalPort(), mappedPort.getExternalAddress(), lifetime); //, new PreferFailurePcpOption());
//            // Preferfailurd does not work on Apple Airport Extreme :( unsupp_option.
//
//            newMappedPort = new MappedPort(resp.getInternalPort(), resp.getAssignedExternalPort(),
//                    resp.getAssignedExternalIpAddress(), PortType.fromIanaNumber(resp.getProtocol()), resp.getLifetime());
//        } catch (RuntimeException re) {
//            throw new IllegalStateException(re);
//        }
//        
//        try {
//            Validate.isTrue(mappedPort.getExternalAddress().equals(mappedPort.getExternalAddress()), "External address changed");
//            Validate.isTrue(mappedPort.getInternalPort() == mappedPort.getInternalPort(), "External port changed");
//        } catch (IllegalStateException ise) {
//            // port has been mapped to different external ip and/or port, unmap and return error
//            try {
//                unmapPort(mappedPort);
//            } catch (RuntimeException re) { // NOPMD
//                // do nothing
//            }
//            
//            throw ise;
//        }
//
//
//        return newMappedPort;
        return null;
    }
    
    @Override
    public void close() throws IOException {
//        closed = true;
//        controller.close();
    }
}
