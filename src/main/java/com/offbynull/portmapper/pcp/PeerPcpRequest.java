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

import com.offbynull.portmapper.common.ByteBufferUtils;
import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents a PEER PCP request. From the RFC:
 * <pre>
 *    The following diagram shows the Opcode response for the PEER Opcode:
 * 
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |                 Mapping Nonce (96 bits)                       |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |   Protocol    |          Reserved (24 bits)                   |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |        Internal Port          |    Assigned External Port     |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |            Assigned External IP Address (128 bits)            |
 *      |                                                               |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |       Remote Peer Port        |     Reserved (16 bits)        |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |               Remote Peer IP Address (128 bits)               |
 *      |                                                               |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                       Figure 12: PEER Opcode Response
 * 
 *    Lifetime (in common header):  On a success response, this indicates
 *       the lifetime for this mapping, in seconds.  On an error response,
 *       this indicates how long clients should assume they'll get the same
 *       error response from the PCP server if they repeat the same
 *       request.
 * 
 *    Mapping Nonce:  Copied from the request.
 * 
 *    Protocol:  Copied from the request.
 * 
 *    Reserved:  24 reserved bits, MUST be set to 0 on transmission, MUST
 *       be ignored on reception.
 * 
 *    Internal Port:  Copied from request.
 * 
 *    Assigned External Port:  On a success response, this is the assigned
 *       external port for the mapping.  On an error response, the
 *       suggested external port is copied from the request.
 * 
 *    Assigned External IP Address:  On a success response, this is the
 *       assigned external IPv4 or IPv6 address for the mapping.  On an
 *       error response, the suggested external IP address is copied from
 *       the request.
 * 
 *    Remote Peer Port:  Copied from request.
 * 
 *    Reserved:  16 reserved bits, MUST be set to 0 on transmission, MUST
 *       be ignored on reception.
 * 
 *    Remote Peer IP Address:  Copied from the request.
 * </pre>
 * @author Kasra Faghihi
 */
public final class PeerPcpRequest extends PcpRequest {

    private ByteBuffer mappingNonce;
    private int protocol;
    private int internalPort;
    private int suggestedExternalPort;
    private InetAddress suggestedExternalIpAddress;
    private int remotePeerPort;
    private InetAddress remotePeerIpAddress;
    
    /**
     * Constructs a {@link PeerPcpRequest} object.
     * @param mappingNonce random value used to map requests to responses
     * @param protocol IANA protocol number
     * @param internalPort internal port
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param suggestedExternalIpAddress suggested external IP address ({@code null} or {@code ::} for no preference)
     * @param remotePeerPort remote port
     * @param remotePeerIpAddress remote IP address
     * @param lifetime requested lifetime in seconds
     * @param options PCP options to use
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if protocol is {@code protocol < 1 or > 255}, or if
     * {@code internalPort < 1 or > 65535}, or if {@code suggestedExternalPort > 65535}, or if {@code mappingNonce} does not have {@code 12}
     * bytes remaining, or if {@code remotePort < 1 or > 65535}
     */
    public PeerPcpRequest(ByteBuffer mappingNonce, int protocol, int internalPort, int suggestedExternalPort,
            InetAddress suggestedExternalIpAddress, int remotePeerPort, InetAddress remotePeerIpAddress, long lifetime,
            PcpOption ... options) {
        super(2, lifetime, options);
        
        Validate.notNull(mappingNonce);
        Validate.isTrue(mappingNonce.remaining() == 12);
        Validate.inclusiveBetween(1, 255, protocol);
        Validate.inclusiveBetween(1, 65535, internalPort); // must not be 0
        Validate.inclusiveBetween(0, 65535, suggestedExternalPort); // 0 = no preference
        Validate.notNull(suggestedExternalIpAddress);
        Validate.inclusiveBetween(1, 65535, remotePeerPort); // cannot be 0
        Validate.notNull(remotePeerIpAddress);

        this.mappingNonce = ByteBufferUtils.copyContents(mappingNonce).asReadOnlyBuffer();
        this.protocol = protocol;
        this.internalPort = internalPort;
        this.suggestedExternalPort = suggestedExternalPort;
        this.suggestedExternalIpAddress = suggestedExternalIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
        this.remotePeerPort = remotePeerPort;
        this.remotePeerIpAddress = remotePeerIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
    }
    
    @Override
    protected void dumpOpCodeSpecificInformation(ByteBuffer dst) {
        dst.put(mappingNonce.asReadOnlyBuffer());
        dst.put((byte) protocol);
        
        for (int i = 0; i < 3; i++) { // reserved block
            dst.put((byte) 0);
        }
        
        dst.putShort((short) internalPort);
        dst.putShort((short) suggestedExternalPort);
        dst.put(NetworkUtils.convertToIpv6Array(suggestedExternalIpAddress));
        dst.putShort((short) remotePeerPort);
        
        for (int i = 0; i < 2; i++) { // reserved block
            dst.put((byte) 0);
        }
        
        dst.put(NetworkUtils.convertToIpv6Array(remotePeerIpAddress));
    }

    /**
     * Get nonce.
     * @return nonce (read-only buffer)
     */
    public ByteBuffer getMappingNonce() {
        return mappingNonce.asReadOnlyBuffer();
    }

    /**
     * Get IANA protocol number.
     * @return IANA protocol number
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Get internal port number.
     * @return internal port number
     */
    public int getInternalPort() {
        return internalPort;
    }

    /**
     * Get suggested external port number.
     * @return suggested external port number
     */
    public int getSuggestedExternalPort() {
        return suggestedExternalPort;
    }

    /**
     * Get suggested external IP address.
     * @return suggested external IP address
     */
    public InetAddress getSuggestedExternalIpAddress() {
        return suggestedExternalIpAddress;
    }

    /**
     * Get remote peer port number.
     * @return remote peer port number
     */
    public int getRemotePeerPort() {
        return remotePeerPort;
    }

    /**
     * Get remote peer IP address.
     * @return remote peer IP address
     */
    public InetAddress getRemotePeerIpAddress() {
        return remotePeerIpAddress;
    }
}
