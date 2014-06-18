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

    private ByteBuffer mappingNonce;
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
     * @param lifetime requested lifetime in seconds
     * @param options PCP options to use
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code protocol > 255}, or if
     * {@code internalPort > 65535}, or if {@code suggestedExternalPort > 65535}, or if {@code mappingNonce} does not have {@code 12} bytes
     * remaining, or if {@code protocol == 0} but {@code internalPort != 0}, or if {@code internalPort == 0} but {@code lifetime != 0}
     */
    public MapPcpRequest(ByteBuffer mappingNonce, int protocol, int internalPort, int suggestedExternalPort,
            InetAddress suggestedExternalIpAddress, long lifetime, PcpOption ... options) {
        super(1, lifetime, options);
        
        Validate.notNull(mappingNonce);
        Validate.isTrue(mappingNonce.remaining() == 12);
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

        this.mappingNonce = ByteBufferUtils.copyContents(mappingNonce).asReadOnlyBuffer();
        this.protocol = protocol;
        this.internalPort = internalPort;
        this.suggestedExternalPort = suggestedExternalPort;
        this.suggestedExternalIpAddress = suggestedExternalIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
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
}
