/*
 * Copyright (c) 2013-2015, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.pcp.messages;

import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.util.Arrays;
import java.util.Objects;
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
    private static final int OPCODE = 2;
    private static final int DATA_LENGTH = 56;
    private static final int NONCE_LENGTH = 12;

    private byte[] mappingNonce;
    private int protocol;
    private int internalPort;
    private int assignedExternalPort;
    private InetAddress assignedExternalIpAddress;
    private int remotePeerPort;
    private InetAddress remotePeerIpAddress;

    /**
     * Constructs a {@link PeerPcpResponse} object.
     * @param mappingNonce random value used to map requests to responses
     * @param protocol IANA protocol number
     * @param internalPort internal port
     * @param assignedExternalPort assigned external port
     * @param assignedExternalIpAddress assigned external IP address
     * @param remotePeerPort remote port
     * @param remotePeerIpAddress remote IP address
     * @param lifetime lifetime in seconds
     * @param epochTime server's epoch time in seconds
     * @param resultCode result code
     * @param options PCP options to use
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if {0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12 || 1 > protocol > 255
     * || 1 > internalPort > 65535 || (resultCode == 0 ? 1 > assignedExternalPort > 65535 : 0 > assignedExternalPort > 65535)
     * || 1 > remotePeerPort > 65535}
     */
    public PeerPcpResponse(byte[] mappingNonce, int protocol, int internalPort, int assignedExternalPort,
            InetAddress assignedExternalIpAddress, int remotePeerPort, InetAddress remotePeerIpAddress, int resultCode, long lifetime,
            long epochTime, PcpOption ... options) {
        super(OPCODE, resultCode, lifetime, epochTime, DATA_LENGTH, options);
        
        Validate.notNull(mappingNonce);
        Validate.notNull(assignedExternalIpAddress);
        Validate.notNull(remotePeerIpAddress);

        this.mappingNonce = Arrays.copyOf(mappingNonce, mappingNonce.length);
        this.protocol = protocol;
        this.internalPort = internalPort;
        this.assignedExternalPort = assignedExternalPort;
        this.assignedExternalIpAddress = assignedExternalIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
        this.remotePeerPort = remotePeerPort;
        this.remotePeerIpAddress = remotePeerIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
        
        validateState();
    }

    /**
     * Constructs a {@link PeerPcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 1 || op == 2 || 0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12
     * || 1 > protocol > 255 || 1 > internalPort > 65535
     * || (resultCode == 0 ? 1 > assignedExternalPort > 65535 : 0 > assignedExternalPort > 65535) || 1 > remotePeerPort > 65535}) or
     * contains an unparseable options region.
     */
    public PeerPcpResponse(byte[] buffer) {
        super(buffer, DATA_LENGTH);
        
        Validate.isTrue(super.getOp() == OPCODE);
        
        int remainingLength = buffer.length - HEADER_LENGTH;
        Validate.isTrue(remainingLength >= DATA_LENGTH); // FYI: remaining length = data block len + options len
        
        int offset = HEADER_LENGTH;
        
        mappingNonce = new byte[NONCE_LENGTH];
        System.arraycopy(buffer, offset, mappingNonce, 0, mappingNonce.length);
        offset += mappingNonce.length;
        
        protocol = buffer[offset] & 0xFF;
        offset++;
        
        offset += 3; // 3 reserved bytes
        
        internalPort = InternalUtils.bytesToShort(buffer, offset);
        offset += 2;
        
        assignedExternalPort = InternalUtils.bytesToShort(buffer, offset);
        offset += 2;

        byte[] ipv6Bytes = new byte[16];
        System.arraycopy(buffer, offset, ipv6Bytes, 0, ipv6Bytes.length);
        try {
            assignedExternalIpAddress = InetAddress.getByAddress(ipv6Bytes);
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe); // should never happen
        }
        offset += ipv6Bytes.length;

        remotePeerPort = InternalUtils.bytesToShort(buffer, offset);
        offset += 2;
        
        offset += 2; // reserved
        
        ipv6Bytes = new byte[16];
        System.arraycopy(buffer, offset, ipv6Bytes, 0, ipv6Bytes.length);
        try {
            remotePeerIpAddress = InetAddress.getByAddress(ipv6Bytes);
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe); // should never happen
        }
        offset += ipv6Bytes.length;
        
        validateState();
    }

    private void validateState() {
        Validate.notNull(mappingNonce);
        Validate.isTrue(mappingNonce.length == NONCE_LENGTH);
        Validate.inclusiveBetween(1, 255, protocol); // can't be 0
        Validate.inclusiveBetween(1, 65535, internalPort); // can't be 0
        if (getResultCode() == 0) {
            Validate.inclusiveBetween(1, 65535, assignedExternalPort); // on success, this is the assigned external port for the mapping
        } else {
            Validate.inclusiveBetween(0, 65535, assignedExternalPort); // on error, 'suggested external port' copied from request (can be 0)
        }
        Validate.notNull(assignedExternalIpAddress);
        Validate.inclusiveBetween(1, 65535, remotePeerPort); // can't be 0
        Validate.notNull(remotePeerIpAddress);
    }
    
    @Override
    public byte[] getData() {
        byte[] data = new byte[DATA_LENGTH];
        
        int offset = 0;
        
        System.arraycopy(mappingNonce, 0, data, offset, mappingNonce.length);
        offset += mappingNonce.length;
        
        data[offset] = (byte) protocol;
        offset++;
        
        offset += 3; // 3 reserved bytes
        
        InternalUtils.shortToBytes(data, offset, (short) internalPort);
        offset += 2;
        
        InternalUtils.shortToBytes(data, offset, (short) assignedExternalPort);
        offset += 2;
        
        byte[] ipv6Array = NetworkUtils.convertToIpv6Array(assignedExternalIpAddress);
        System.arraycopy(ipv6Array, 0, data, offset, ipv6Array.length);
        offset += ipv6Array.length;

        InternalUtils.shortToBytes(data, offset, (short) remotePeerPort);
        offset += 2;
        
        offset += 2; // 2 reserved bytes

        ipv6Array = NetworkUtils.convertToIpv6Array(remotePeerIpAddress);
        System.arraycopy(ipv6Array, 0, data, offset, ipv6Array.length);
        offset += ipv6Array.length;
        
        return data;
    }

    /**
     * Get nonce.
     * @return nonce
     */
    public byte[] getMappingNonce() {
        return Arrays.copyOf(mappingNonce, mappingNonce.length);
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

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + Arrays.hashCode(this.mappingNonce);
        hash = 97 * hash + this.protocol;
        hash = 97 * hash + this.internalPort;
        hash = 97 * hash + this.assignedExternalPort;
        hash = 97 * hash + Objects.hashCode(this.assignedExternalIpAddress);
        hash = 97 * hash + this.remotePeerPort;
        hash = 97 * hash + Objects.hashCode(this.remotePeerIpAddress);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PeerPcpResponse other = (PeerPcpResponse) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (this.protocol != other.protocol) {
            return false;
        }
        if (this.internalPort != other.internalPort) {
            return false;
        }
        if (this.assignedExternalPort != other.assignedExternalPort) {
            return false;
        }
        if (this.remotePeerPort != other.remotePeerPort) {
            return false;
        }
        if (!Arrays.equals(this.mappingNonce, other.mappingNonce)) {
            return false;
        }
        if (!Objects.equals(this.assignedExternalIpAddress, other.assignedExternalIpAddress)) {
            return false;
        }
        if (!Objects.equals(this.remotePeerIpAddress, other.remotePeerIpAddress)) {
            return false;
        }
        return true;
    }
}
