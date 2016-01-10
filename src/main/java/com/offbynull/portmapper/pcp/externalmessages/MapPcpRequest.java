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
package com.offbynull.portmapper.pcp.externalmessages;

import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Represents a MAP PCP request. From the RFC:
 * <pre>
 *    The following diagram shows the format of the Opcode-specific
 *    information in a request for the MAP Opcode.
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
 *      |        Internal Port          |    Suggested External Port    |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |           Suggested External IP Address (128 bits)            |
 *      |                                                               |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                        Figure 9: MAP Opcode Request
 * 
 *    These fields are described below:
 * 
 *    Requested lifetime (in common header):  Requested lifetime of this
 *       mapping, in seconds.  The value 0 indicates "delete".
 * 
 *    Mapping Nonce:  Random value chosen by the PCP client.  See
 *       Section 11.2, "Generating a MAP Request".  Zero is a legal value
 *       (but unlikely, occurring in roughly one in 2^96 requests).
 * 
 *    Protocol:  Upper-layer protocol associated with this Opcode.  Values
 *       are taken from the IANA protocol registry [proto_numbers].  For
 *       example, this field contains 6 (TCP) if the Opcode is intended to
 *       create a TCP mapping.  This field contains 17 (UDP) if the Opcode
 *       is intended to create a UDP mapping.  The value 0 has a special
 *       meaning for 'all protocols'.
 * 
 *    Reserved:  24 reserved bits, MUST be sent as 0 and MUST be ignored
 *       when received.
 * 
 *    Internal Port:  Internal port for the mapping.  The value 0 indicates
 *       'all ports', and is legal when the lifetime is zero (a delete
 *       request), if the protocol does not use 16-bit port numbers, or the
 *       client is requesting 'all ports'.  If the protocol is zero
 *       (meaning 'all protocols'), then internal port MUST be zero on
 *       transmission and MUST be ignored on reception.
 * 
 *    Suggested External Port:  Suggested external port for the mapping.
 *       This is useful for refreshing a mapping, especially after the PCP
 *       server loses state.  If the PCP client does not know the external
 *       port, or does not have a preference, it MUST use 0.
 * 
 *    Suggested External IP Address:  Suggested external IPv4 or IPv6
 *       address.  This is useful for refreshing a mapping, especially
 *       after the PCP server loses state.  If the PCP client does not know
 *       the external address, or does not have a preference, it MUST use
 *       the address-family-specific all-zeros address (see Section 5).
 * 
 *    The internal address for the request is the source IP address of the
 *    PCP request message itself, unless the THIRD_PARTY option is used.
 * </pre>
 * @author Kasra Faghihi
 */
public final class MapPcpRequest extends PcpRequest {
    private static final int OPCODE = 1;
    private static final int DATA_LENGTH = 36;
    private static final int NONCE_LENGTH = 12;

    private byte[] mappingNonce;
    private int protocol;
    private int internalPort;
    private int suggestedExternalPort;
    private InetAddress suggestedExternalIpAddress;

    /**
     * Constructs a {@link MapPcpRequest} object.
     * @param mappingNonce random value used to map requests to responses
     * @param protocol IANA protocol number ({@code 0} is valid, see Javadoc header)
     * @param internalPort internal port ({@code 0} is valid, see Javadoc header)
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param suggestedExternalIpAddress suggested external IP address ({@code ::} for no preference)
     * @param lifetime requested lifetime in seconds ({@code 0} is valid, see Javadoc header)
     * @param internalIp IP address on the interface used to access the PCP server
     * @param options PCP options to use
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if {@code 0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12 || 0 > protocol > 255
     * || 0 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || (protocol == 0 && internalPort != 0)
     * || (internalPort == 0 && lifetime != 0)}
     */
    public MapPcpRequest(byte[] mappingNonce, int protocol, int internalPort, int suggestedExternalPort,
            InetAddress suggestedExternalIpAddress, long lifetime, InetAddress internalIp, PcpOption ... options) {
        super(OPCODE, lifetime, internalIp, DATA_LENGTH, options);
        
        Validate.notNull(mappingNonce);
        Validate.notNull(suggestedExternalIpAddress);

        this.mappingNonce = Arrays.copyOf(mappingNonce, mappingNonce.length);
        this.protocol = protocol;
        this.internalPort = internalPort;
        this.suggestedExternalPort = suggestedExternalPort;
        this.suggestedExternalIpAddress = suggestedExternalIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
    }

    /**
     * Constructs a {@link MapPcpRequest} object by parsing a buffer.
     * @param buffer buffer containing PCP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 0 || op != 1 || 0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12 || 0 > protocol > 255
     * || 0 > internalPort > 65535 || 0 > suggestedExternalPort > 65535}|| (protocol == 0 && internalPort != 0)
     * || (internalPort == 0 && lifetime != 0)) or contains an unparseable options region.
     */
    public MapPcpRequest(byte[] buffer) {
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
        
        suggestedExternalPort = InternalUtils.bytesToShort(buffer, offset);
        offset += 2;

        suggestedExternalIpAddress = NetworkUtils.convertBytesToAddress(buffer, offset, 16);
        offset += 16;
        
        validateState();
    }

    private void validateState() {
        Validate.notNull(mappingNonce);
        Validate.isTrue(mappingNonce.length == NONCE_LENGTH);
        Validate.inclusiveBetween(0, 255, protocol);
        Validate.inclusiveBetween(0, 65535, internalPort);
        Validate.inclusiveBetween(0, 65535, suggestedExternalPort);
        Validate.notNull(suggestedExternalIpAddress);
        
        if (protocol == 0) {
            Validate.isTrue(internalPort == 0);
        }
        
        if (internalPort == 0) {
            Validate.isTrue(super.getLifetime() == 0L);
        }
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
        
        InternalUtils.shortToBytes(data, offset, (short) suggestedExternalPort);
        offset += 2;

        byte[] ipv6Array = NetworkUtils.convertAddressToIpv6Bytes(suggestedExternalIpAddress);
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

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 13 * hash + Arrays.hashCode(this.mappingNonce);
        hash = 13 * hash + this.protocol;
        hash = 13 * hash + this.internalPort;
        hash = 13 * hash + this.suggestedExternalPort;
        hash = 13 * hash + Objects.hashCode(this.suggestedExternalIpAddress);
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
        final MapPcpRequest other = (MapPcpRequest) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (this.protocol != other.protocol) {
            return false;
        }
        if (this.internalPort != other.internalPort) {
            return false;
        }
        if (this.suggestedExternalPort != other.suggestedExternalPort) {
            return false;
        }
        if (!Arrays.equals(this.mappingNonce, other.mappingNonce)) {
            return false;
        }
        if (!Objects.equals(this.suggestedExternalIpAddress, other.suggestedExternalIpAddress)) {
            return false;
        }
        return true;
    }
}
