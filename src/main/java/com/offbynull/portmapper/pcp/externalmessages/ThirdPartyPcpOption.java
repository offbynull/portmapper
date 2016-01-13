/*
 * Copyright (c) 2013-2016, Kasra Faghihi, All rights reserved.
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
import java.util.Objects;
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
    private static final int OP_CODE = 1;
    private static final int DATA_LENGTH = 16;

    private InetAddress internalIpAddress;

    /**
     * Constructs a {@link ThirdPartyPcpOption} by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @param offset offset in {@code buffer} where the PCP option starts
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} is malformed (doesn't contain enough bytes
     * / length is not a multiple of 4 (not enough padding) / length exceeds 65535 / data doesn't contain enough bytes / code is not 1)
     */
    public ThirdPartyPcpOption(byte[] buffer, int offset) {
        super(buffer, offset);
        
        Validate.isTrue(super.getCode() == OP_CODE);
        Validate.isTrue(super.getDataLength() == DATA_LENGTH);

        offset += HEADER_LENGTH;

        internalIpAddress = NetworkUtils.convertBytesToAddress(buffer, offset, 16);
        offset += 16;
    }

    /**
     * Constructs a {@link ThirdPartyPcpOption}.
     * @param internalIpAddress internal IP address field
     * @throws NullPointerException if any argument is {@code null}
     */
    public ThirdPartyPcpOption(InetAddress internalIpAddress) {
        super(OP_CODE, DATA_LENGTH);
        
        Validate.notNull(internalIpAddress);

        this.internalIpAddress = internalIpAddress;
    }

    /**
     * Get internal IP address.
     * @return internal IP address
     */
    public InetAddress getInternalIpAddress() {
        return internalIpAddress;
    }

    @Override
    public byte[] getData() {
        byte[] data = new byte[DATA_LENGTH];

        // write ip
        byte[] ipv6AsBytes = NetworkUtils.convertAddressToIpv6Bytes(internalIpAddress);
        Validate.validState(ipv6AsBytes.length == 16); // sanity check, should never throw exception
        System.arraycopy(ipv6AsBytes, 0, data, 0, ipv6AsBytes.length);
        
        return data;
    }

    @Override
    public String toString() {
        return "ThirdPartyPcpOption{super=" + super.toString() + "internalIpAddress=" + internalIpAddress + '}';
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 23 * hash + Objects.hashCode(this.internalIpAddress);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ThirdPartyPcpOption other = (ThirdPartyPcpOption) obj;
        if (!Objects.equals(this.internalIpAddress, other.internalIpAddress)) {
            return false;
        }
        return true;
    }
    
}
