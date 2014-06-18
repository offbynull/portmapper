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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
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
public final class PeerPcpResponse extends PcpResponse {
    private ByteBuffer mappingNonce;
    private int protocol;
    private int internalPort;
    private int assignedExternalPort;
    private InetAddress assignedExternalIpAddress;
    private int remotePeerPort;
    private InetAddress remotePeerIpAddress;

    /**
     * Constructs a {@link PeerPcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if there's not enough or too much data remaining in the buffer, or if the version doesn't match the
     * expected version (must always be {@code 2}), or if the r-flag isn't set, or if there's an unsuccessful/unrecognized result code,
     * or if the op code doesn't match the PEER opcode, or if the response has a {@code 0} for its {@code internalPort} or
     * {@code assignedExternalPort} or {@code remotePeerPort} or {@code protocol} field, or if there were problems parsing options
     */
    public PeerPcpResponse(ByteBuffer buffer) {
        super(buffer);
        
        Validate.isTrue(super.getOp() == 2);

        mappingNonce = ByteBuffer.allocate(12);
        buffer.get(mappingNonce.array());
        mappingNonce = mappingNonce.asReadOnlyBuffer();
        this.protocol = buffer.get() & 0xFF;
        
        for (int i = 0; i < 3; i++) { // reserved block
            buffer.get();
        }
        
        this.internalPort = buffer.getShort() & 0xFFFF;
        this.assignedExternalPort = buffer.getShort() & 0xFFFF;
        byte[] addrArr = new byte[16];
        buffer.get(addrArr);
        try {
            this.assignedExternalIpAddress = InetAddress.getByAddress(addrArr); // should automatically shift down to ipv4 if ipv4-to-ipv6
                                                                                // mapped address
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException(uhe); // should never happen, will always be 16 bytes
        }
        this.remotePeerPort = buffer.getShort() & 0xFFFF;
        
        for (int i = 0; i < 2; i++) { // reserved block
            buffer.get();
        }
        
        buffer.get(addrArr);
        try {
            this.remotePeerIpAddress = InetAddress.getByAddress(addrArr); // should automatically shift down to ipv4 if ipv4-to-ipv6
                                                                          // mapped address
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException(uhe); // should never happen, will always be 16 bytes
        }
        
        Validate.inclusiveBetween(1, 255, protocol);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1, 65535, assignedExternalPort);
        Validate.inclusiveBetween(1, 65535, remotePeerPort);
        
        parseOptions(buffer);
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
    public int getAssignedExternalPort() {
        return assignedExternalPort;
    }

    /**
     * Get suggested external IP address.
     * @return suggested external IP address
     */
    public InetAddress getAssignedExternalIpAddress() {
        return assignedExternalIpAddress;
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
