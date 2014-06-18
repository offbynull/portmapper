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

import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents a THIRD_PARTY PCP option. From the RFC:
 * <pre>
 *    The THIRD_PARTY option is formatted as follows:
 * 
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Option Code=1 |  Reserved     |   Option Length=16            |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                                                               |
 *    |                Internal IP Address (128 bits)                 |
 *    |                                                               |
 *    |                                                               |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                        Figure 13: THIRD_PARTY Option
 * 
 *    The fields are described below:
 * 
 *    Internal IP Address:  Internal IP address for this mapping.
 * 
 *       Option Name: THIRD_PARTY
 *       Number: 1
 *       Purpose: Indicates the MAP or PEER request is for a host other
 *       than the host sending the PCP option.
 *       Valid for Opcodes: MAP, PEER
 *       Length: 16 octets
 *       May appear in: request.  May appear in response only if it
 *       appeared in the associated request.
 *       Maximum occurrences: 1
 * 
 *    A THIRD_PARTY option MUST NOT contain the same address as the source
 *    address of the packet.  This is because many PCP servers may not
 *    implement the THIRD_PARTY option at all, and with those servers a
 *    client redundantly using the THIRD_PARTY option to specify its own IP
 *    address would cause such mapping requests to fail where they would
 *    otherwise have succeeded.  A PCP server receiving a THIRD_PARTY
 *    option specifying the same address as the source address of the
 *    packet MUST return a MALFORMED_REQUEST result code.
 * </pre>
 * @author Kasra Faghihi
 */
public final class ThirdPartyPcpOption extends PcpOption {

    private InetAddress internalIpAddress;

    /**
     * Constructs a {@link ThirdPartyPcpOption} by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if option code is not {@code 1}
     */
    public ThirdPartyPcpOption(ByteBuffer buffer) {
        super(buffer);
        Validate.isTrue(super.getCode() == 1);
        byte[] addrArr = new byte[16];
        buffer.get(addrArr);
        try {
            internalIpAddress = InetAddress.getByAddress(addrArr);
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe); // should never happen
        }
    }

    /**
     * Constructs a {@link ThirdPartyPcpOption}.
     * @param internalIpAddress internal IP address field
     * @throws NullPointerException if any argument is {@code null}
     */
    public ThirdPartyPcpOption(InetAddress internalIpAddress) {
        super(1, toDataSection(internalIpAddress));
        this.internalIpAddress = internalIpAddress;
    }
    
    private static ByteBuffer toDataSection(InetAddress internalIpAddress) {
        Validate.notNull(internalIpAddress);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put(NetworkUtils.convertToIpv6Array(internalIpAddress));
        
        return buffer;
    }

    /**
     * Get internal IP address.
     * @return internal IP address
     */
    public InetAddress getInternalIpAddress() {
        return internalIpAddress;
    }
}
