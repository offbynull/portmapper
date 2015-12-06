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
package com.offbynull.portmapper.pcp.messages;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents a MAP PCP response. From the RFC:
 * <pre>
 *    The following diagram shows the format of Opcode-specific information
 *    in a response packet for the MAP Opcode:
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
 * 
 *                       Figure 10: MAP Opcode Response
 * 
 *    These fields are described below:
 * 
 *    Lifetime (in common header):  On an error response, this indicates
 *       how long clients should assume they'll get the same error response
 *       from the PCP server if they repeat the same request.  On a success
 *       response, this indicates the lifetime for this mapping, in
 *       seconds.
 * 
 *    Mapping Nonce:  Copied from the request.
 * 
 *    Protocol:  Copied from the request.
 * 
 *    Reserved:  24 reserved bits, MUST be sent as 0 and MUST be ignored
 *       when received.
 * 
 *    Internal Port:  Copied from the request.
 * 
 *    Assigned External Port:  On a success response, this is the assigned
 *       external port for the mapping.  On an error response, the
 *       suggested external port is copied from the request.
 * 
 *    Assigned External IP Address:  On a success response, this is the
 *       assigned external IPv4 or IPv6 address for the mapping.  An IPv4
 *       address is encoded using IPv4-mapped IPv6 address.  On an error
 *       response, the suggested external IP address is copied from the
 *       request.
 * </pre>
 * @author Kasra Faghihi
 */
public final class MapPcpResponse extends PcpResponse {
    private ByteBuffer mappingNonce;
    private int protocol;
    private int internalPort;
    private int assignedExternalPort;
    private InetAddress assignedExternalIpAddress;

    /**
     * Constructs a {@link MapPcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if there's not enough or too much data remaining in the buffer, or if the version doesn't match the
     * expected version (must always be {@code 2}), or if the r-flag isn't set, or if there's an unsuccessful/unrecognized result code,
     * or if the op code doesn't match the MAP opcode, or if the response has a {@code 0} for its {@code internalPort} or
     * {@code assignedExternalPort} field, or if there were problems parsing options
     */
    public MapPcpResponse(ByteBuffer buffer) {
        super(buffer);
        
        Validate.isTrue(super.getOp() == 1);

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
        
        Validate.inclusiveBetween(0, 255, protocol); // should never happen
        Validate.inclusiveBetween(0, 65535, internalPort); // can be 0 if referencing all
        Validate.inclusiveBetween(0, 65535, assignedExternalPort); // can be 0 if removing
        
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
     * Get assigned external port number.
     * @return assigned external port number
     */
    public int getAssignedExternalPort() {
        return assignedExternalPort;
    }

    /**
     * Get assigned external IP address.
     * @return assigned external IP address
     */
    public InetAddress getAssignedExternalIpAddress() {
        return assignedExternalIpAddress;
    }
}
