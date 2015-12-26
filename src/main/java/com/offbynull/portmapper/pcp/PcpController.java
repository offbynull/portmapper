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

import com.offbynull.portmapper.pcp.messages.PcpResponse;
import com.offbynull.portmapper.pcp.messages.PcpRequest;
import com.offbynull.portmapper.pcp.messages.PcpOption;
import com.offbynull.portmapper.pcp.messages.AnnouncePcpResponse;
import com.offbynull.portmapper.pcp.messages.MapPcpRequest;
import com.offbynull.portmapper.pcp.messages.AnnouncePcpRequest;
import com.offbynull.portmapper.pcp.messages.PeerPcpResponse;
import com.offbynull.portmapper.pcp.messages.PeerPcpRequest;
import com.offbynull.portmapper.pcp.messages.MapPcpResponse;
import com.offbynull.portmapper.common.ResponseException;
import com.offbynull.portmapper.common.CommunicationType;
import com.offbynull.portmapper.PortType;
import com.offbynull.portmapper.common.ByteBufferUtils;
import com.offbynull.portmapper.common.NetworkUtils;
import com.offbynull.portmapper.common.UdpCommunicator;
import com.offbynull.portmapper.common.UdpCommunicatorListener;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * PCP controller.
 * @author Kasra Faghihi
 */
public final class PcpController implements Closeable {
    private InetSocketAddress gateway;
    private UdpCommunicator communicator;
    private InetAddress selfAddress;
    private Random random;
    
    // closed by udp comm
    private DatagramChannel unicastChannel;
    private DatagramChannel ipv4MulticastChannel;
    private DatagramChannel ipv6MulticastChannel;

    /**
     * Constructs a {@link PcpController} object.
     * @param random used to generate nonce values for requests
     * @param gatewayAddress address of router/gateway
     * @param selfAddress address of this machine on the interface that can talk to the router/gateway
     * @param listener a listener to listen for all PCP packets from this router
     * @throws NullPointerException if any argument is {@code null}
     * @throws IOException if problems initializing UDP channels
     */
    public PcpController(Random random, InetAddress gatewayAddress, InetAddress selfAddress, final PcpControllerListener listener)
            throws IOException {
        Validate.notNull(random);
        Validate.notNull(gatewayAddress);
        Validate.notNull(selfAddress);
        
        this.gateway = new InetSocketAddress(gatewayAddress, 5351);
        
        List<DatagramChannel> channels = new ArrayList<>(3);

        try {
            unicastChannel = DatagramChannel.open();
            unicastChannel.configureBlocking(false);
            unicastChannel.socket().bind(new InetSocketAddress(0));

            ipv4MulticastChannel = DatagramChannel.open(StandardProtocolFamily.INET);
            ipv4MulticastChannel.configureBlocking(false);
            ipv4MulticastChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            ipv4MulticastChannel.socket().bind(new InetSocketAddress(5350));
            NetworkUtils.multicastListenOnAllIpv4InterfaceAddresses(ipv4MulticastChannel);

            ipv6MulticastChannel = DatagramChannel.open(StandardProtocolFamily.INET6);
            ipv6MulticastChannel.configureBlocking(false);
            ipv6MulticastChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            ipv6MulticastChannel.socket().bind(new InetSocketAddress(5350));
            NetworkUtils.multicastListenOnAllIpv6InterfaceAddresses(ipv6MulticastChannel);
        } catch (IOException ioe) {
            IOUtils.closeQuietly(unicastChannel);
            IOUtils.closeQuietly(ipv4MulticastChannel);
            IOUtils.closeQuietly(ipv6MulticastChannel);
            throw ioe;
        }
        
        channels.add(unicastChannel);
        channels.add(ipv4MulticastChannel);
        channels.add(ipv6MulticastChannel);
        
        this.communicator = new UdpCommunicator(channels);
        this.selfAddress = selfAddress;
        this.random = random;
        
        this.communicator.startAsync().awaitRunning();
        
        if (listener != null) {
            this.communicator.addListener(new UdpCommunicatorListener() {

                @Override
                public void incomingPacket(InetSocketAddress sourceAddress, DatagramChannel channel, ByteBuffer packet) {
                    CommunicationType type;
                    if (channel == unicastChannel) {
                        type = CommunicationType.UNICAST;
                    } else if (channel == ipv4MulticastChannel || channel == ipv6MulticastChannel) {
                        type = CommunicationType.MULTICAST;
                    } else {
                        return; // unknown, do nothing
                    }
                    
                    byte[] packetData = ByteBufferUtils.copyContentsToArray(packet, false);
                    try {
                        listener.incomingResponse(type, new AnnouncePcpResponse(packetData));
                    } catch (BufferUnderflowException | IllegalArgumentException e) {
                        // ignore
                    }
                    
                    try {
                        listener.incomingResponse(type, new MapPcpResponse(packetData));
                    } catch (BufferUnderflowException | IllegalArgumentException e) {
                        // ignore
                    }
                    
                    try {
                        packet.mark();
                        listener.incomingResponse(type, new PeerPcpResponse(packetData));
                    } catch (BufferUnderflowException | IllegalArgumentException e) {
                        // ignore
                    }
                }
            });
        }
    }
    
    /**
     * Send a ANNOUNCE request to the gateway.
     * @param sendAttempts number of times to try to submit each request
     * @return ANNOUNCE response
     * @throws BufferUnderflowException if the message is too big to be written in to the buffer
     * @throws ResponseException if no response available
     * @throws InterruptedException if thread was interrupted while waiting
     * @throws IllegalArgumentException if {@code sendAttempts < 1 || > 9}
     */
    public AnnouncePcpResponse requestAnnounceOperation(int sendAttempts) throws InterruptedException {
        AnnouncePcpRequest req = new AnnouncePcpRequest(selfAddress);
        
        AnnounceResponseCreator creator = new AnnounceResponseCreator();
        return performRequest(sendAttempts, req, creator);
    }

//    /**
//     * Send a ANNOUNCE request to the gateway. Only sends 1 packet, does not implement retry algorithm like blocking equivalent.
//     * @return PCP request
//     * @throws BufferUnderflowException if the message is too big to be written in to the buffer
//     */
//    public AnnouncePcpRequest requestAnnounceOperationAsync() {
//        AnnouncePcpRequest req = new AnnouncePcpRequest();
//        performRequestAsync(req);
//        
//        return req;
//    }
    
    /**
     * Send a request to the gateway to create an inbound mapping (open a port that you can listen on).
     * @param sendAttempts number of times to try to submit each request
     * @param portType port type
     * @param internalPort internal port ({@code 0} is valid, see {@link MapPcpRequest} header)
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param suggestedExternalIpAddress suggested external IP address ({@code ::} for no preference)
     * @param lifetime requested lifetime in seconds
     * @param options PCP options to use
     * @return MAP response
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws BufferUnderflowException if the message is too big to be written in to the buffer
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code protocol > 255}, or if
     * {@code internalPort > 65535}, or if {@code suggestedExternalPort > 65535}
     * remaining, or if {@code protocol == 0} but {@code internalPort != 0}, or if {@code internalPort == 0} but {@code lifetime != 0}, or
     * if {@code sendAttempts < 1 || > 9}
     * @throws ResponseException if the expected response never came in
     * @throws InterruptedException if thread was interrupted while waiting
     */
    public MapPcpResponse requestMapOperation(int sendAttempts, PortType portType, int internalPort, int suggestedExternalPort,
            InetAddress suggestedExternalIpAddress, long lifetime, PcpOption ... options) throws InterruptedException {
        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        
        MapPcpRequest req = new MapPcpRequest(nonce, portType.getProtocolNumber(), internalPort, suggestedExternalPort,
                suggestedExternalIpAddress, lifetime, selfAddress, options);

        MapResponseCreator creator = new MapResponseCreator(req);
        return performRequest(sendAttempts, req, creator);
    }

//    /**
//     * Send a request to the gateway to create an inbound mapping (open a port that you can listen on). Only sends 1 packet, does not
//     * implement retry algorithm like blocking equivalent.
//     * @param portType port type
//     * @param internalPort internal port ({@code 0} is valid, see {@link MapPcpRequest} header)
//     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
//     * @param suggestedExternalIpAddress suggested external IP address ({@code ::} for no preference)
//     * @param lifetime requested lifetime in seconds
//     * @param options PCP options to use
//     * @return PCP request
//     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
//     * @throws BufferUnderflowException if the message is too big to be written in to the buffer
//     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code protocol > 255}, or if
//     * {@code internalPort > 65535}, or if {@code suggestedExternalPort > 65535}
//     * remaining, or if {@code protocol == 0} but {@code internalPort != 0}, or if {@code internalPort == 0} but {@code lifetime != 0}
//     */
//    public MapPcpRequest requestMapOperationAsync(PortType portType, int internalPort, int suggestedExternalPort,
//            InetAddress suggestedExternalIpAddress, long lifetime, PcpOption ... options) {
//        byte[] nonce = new byte[12];
//        random.nextBytes(nonce);
//        
//        MapPcpRequest req = new MapPcpRequest(ByteBuffer.wrap(nonce), portType.getProtocolNumber(), internalPort, suggestedExternalPort,
//                suggestedExternalIpAddress, lifetime, options);
//
//        performRequestAsync(req);
//        
//        return req;
//    }

    /**
     * Send a request to the gateway to create an outbound mapping (open a port that you can connect from).
     * @param sendAttempts number of times to try to submit each request
     * @param portType port type
     * @param internalPort internal port
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param suggestedExternalIpAddress suggested external IP address ({@code null} or {@code ::} for no preference)
     * @param remotePeerPort remote port
     * @param remotePeerIpAddress remote IP address
     * @param lifetime requested lifetime in seconds
     * @param options PCP options to use
     * @return PEER response
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws BufferUnderflowException if the message is too big to be written in to the buffer
     * @throws IllegalArgumentException if {@code internalPort < 1 or > 65535}, or if {@code suggestedExternalPort > 65535},
     * or if {@code remotePort < 1 or > 65535}, or if {@code sendAttempts < 1 || > 9}
     * @throws ResponseException if the expected response never came in
     * @throws InterruptedException if thread was interrupted while waiting
     */
    public PeerPcpResponse requestPeerOperation(int sendAttempts, PortType portType, int internalPort, int suggestedExternalPort,
            InetAddress suggestedExternalIpAddress, int remotePeerPort, InetAddress remotePeerIpAddress, long lifetime,
            PcpOption ... options) throws InterruptedException {
        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        
        PeerPcpRequest req = new PeerPcpRequest(nonce, portType.getProtocolNumber(), internalPort, suggestedExternalPort,
                suggestedExternalIpAddress, remotePeerPort, remotePeerIpAddress, lifetime, selfAddress, options);

        PeerResponseCreator creator = new PeerResponseCreator(req);
        return performRequest(sendAttempts, req, creator);
    }

//    /**
//     * Send a request to the gateway to create an outbound mapping (open a port that you can connect from).  Only sends 1 packet, does not
//     * implement retry algorithm like blocking equivalent.
//     * @param portType port type
//     * @param internalPort internal port
//     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
//     * @param suggestedExternalIpAddress suggested external IP address ({@code null} or {@code ::} for no preference)
//     * @param remotePeerPort remote port
//     * @param remotePeerIpAddress remote IP address
//     * @param lifetime requested lifetime in seconds
//     * @param options PCP options to use
//     * @return PCP request
//     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
//     * @throws BufferUnderflowException if the message is too big to be written in to the buffer
//     * @throws IllegalArgumentException if {@code internalPort < 1 or > 65535}, or if {@code suggestedExternalPort > 65535},
//     * or if {@code remotePort < 1 or > 65535}
//     */
//    public PeerPcpRequest requestPeerOperationAsync(PortType portType, int internalPort, int suggestedExternalPort,
//            InetAddress suggestedExternalIpAddress, int remotePeerPort, InetAddress remotePeerIpAddress, long lifetime,
//            PcpOption ... options) {
//        byte[] nonce = new byte[12];
//        random.nextBytes(nonce);
//        
//        PeerPcpRequest req = new PeerPcpRequest(ByteBuffer.wrap(nonce), portType.getProtocolNumber(), internalPort, suggestedExternalPort,
//                suggestedExternalIpAddress, remotePeerPort, remotePeerIpAddress, lifetime, options);
//
//        performRequestAsync(req);
//        
//        return req;
//    }

    private <T extends PcpResponse> T performRequest(int sendAttempts, PcpRequest request, Creator<T> creator) throws InterruptedException {
        Validate.inclusiveBetween(1, 9, sendAttempts);
        
        byte[] buffer = request.dump();
        
        for (int i = 1; i <= sendAttempts; i++) {
            T response = attemptRequest(ByteBuffer.wrap(buffer), i, creator);
            if (response != null) {
                return response;
            }
        }
        
        throw new ResponseException();
    }

//    private void performRequestAsync(PcpRequest request) {
//        ByteBuffer sendBuffer = ByteBuffer.allocate(1100);
//            
//        request.dump(sendBuffer, selfAddress);
//        sendBuffer.flip();
//        
//        communicator.send(sendBuffer);
//    }

    private <T extends PcpResponse> T attemptRequest(ByteBuffer sendBuffer, int attempt, Creator<T> creator)
            throws InterruptedException {
        
        final LinkedBlockingQueue<ByteBuffer> recvBufferQueue = new LinkedBlockingQueue<>();
        
        UdpCommunicatorListener listener = new UdpCommunicatorListener() {

            @Override
            public void incomingPacket(InetSocketAddress sourceAddress, DatagramChannel channel, ByteBuffer packet) {
                if (channel != unicastChannel) {
                    return;
                }
                
                recvBufferQueue.add(packet);
            }
        };

        
        // timeout duration should double each iteration, starting from 250 according to spec
        // i = 1, maxWaitTime = (1 << (1-1)) * 250 = (1 << 0) * 250 = 1 * 250 = 250
        // i = 2, maxWaitTime = (1 << (2-1)) * 250 = (1 << 1) * 250 = 2 * 250 = 500
        // i = 3, maxWaitTime = (1 << (3-1)) * 250 = (1 << 2) * 250 = 4 * 250 = 1000
        // i = 4, maxWaitTime = (1 << (4-1)) * 250 = (1 << 3) * 250 = 8 * 250 = 2000
        // ...
        try {
            communicator.addListener(listener);
            communicator.send(unicastChannel, gateway, sendBuffer);

            int maxWaitTime = (1 << (attempt - 1)) * 250; // NOPMD

            T pcpResponse = null;

            long endTime = System.currentTimeMillis() + maxWaitTime;
            long waitTime;
            while ((waitTime = endTime - System.currentTimeMillis()) > 0L) {
                waitTime = Math.max(waitTime, 0L); // must be at least 0, probably should never happen

                ByteBuffer recvBuffer = recvBufferQueue.poll(waitTime, TimeUnit.MILLISECONDS);

                if (recvBuffer != null) {
                    pcpResponse = creator.create(recvBuffer);
                    if (pcpResponse != null) {
                        break;
                    }
                }
            }

            return pcpResponse;
        } finally {
            communicator.removeListener(listener);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            communicator.stopAsync().awaitTerminated();
        } catch (IllegalStateException iae) {
            throw new IOException(iae);
        }
    }
    
    private interface Creator<T extends PcpResponse> {
        T create(ByteBuffer response);
    }

    private static final class AnnounceResponseCreator implements Creator<AnnouncePcpResponse> {
        public AnnounceResponseCreator() {
        }

        @Override
        public AnnouncePcpResponse create(ByteBuffer recvBuffer) {
            AnnouncePcpResponse response;
            try {
                byte[] data = ByteBufferUtils.copyContentsToArray(recvBuffer, true);
                response = new AnnouncePcpResponse(data);
            } catch (BufferUnderflowException | BufferOverflowException | IllegalArgumentException e) {
                return null;
            }

            return response;
        }
    }

    private static final class MapResponseCreator implements Creator<MapPcpResponse> {
        private MapPcpRequest request;

        public MapResponseCreator(MapPcpRequest request) {
            Validate.notNull(request);
            this.request = request;
        }

        @Override
        public MapPcpResponse create(ByteBuffer recvBuffer) {
            MapPcpResponse response;
            try {
                byte[] data = ByteBufferUtils.copyContentsToArray(recvBuffer, true);
                response = new MapPcpResponse(data);
            } catch (BufferUnderflowException | BufferOverflowException | IllegalArgumentException e) {
                //throw new BadResponseException(e);
                return null;
            }
            
            if (Arrays.equals(response.getMappingNonce(), request.getMappingNonce())
                    && response.getProtocol() == request.getProtocol()
                    && response.getInternalPort() == request.getInternalPort()) {
                return response;
            }
            
            return null;
        }
    }
    
    private static final class PeerResponseCreator implements Creator<PeerPcpResponse> {
        private PeerPcpRequest request;
        
        public PeerResponseCreator(PeerPcpRequest request) {
            Validate.notNull(request);
            this.request = request;
        }

        @Override
        public PeerPcpResponse create(ByteBuffer recvBuffer) {
            PeerPcpResponse response;
            try {
                byte[] data = ByteBufferUtils.copyContentsToArray(recvBuffer, true);
                response = new PeerPcpResponse(data);
            } catch (BufferUnderflowException | BufferOverflowException | IllegalArgumentException e) {
                //throw new BadResponseException(e);
                return null;
            }

            if (Arrays.equals(response.getMappingNonce(), request.getMappingNonce())
                    && response.getProtocol() == request.getProtocol()
                    && response.getRemotePeerPort() == request.getRemotePeerPort()
                    && response.getRemotePeerIpAddress().equals(request.getRemotePeerIpAddress())) {
                return response;
            }
            
            return null;
        }
    }
}
