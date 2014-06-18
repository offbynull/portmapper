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
package com.offbynull.portmapper.natpmp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP external address response. From the RFC:
 * <pre>
 *    A compatible NAT gateway MUST generate a response with the following
 *    format:
 * 
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Vers = 0      | OP = 128 + 0  | Result Code (net byte order)  |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Seconds Since Start of Epoch (in network byte order)          |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | External IPv4 Address (a.b.c.d)                               |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *    This response indicates that the NAT gateway implements this version
 *    of the protocol, and returns the external IPv4 address of the NAT
 *    gateway.  If the result code is non-zero, the value of the External
 *    IPv4 Address field is undefined (MUST be set to zero on transmission,
 *    and MUST be ignored on reception).
 * 
 *    The NAT gateway MUST fill in the Seconds Since Start of Epoch field
 *    with the time elapsed since its port mapping table was initialized on
 *    startup, or reset for any other reason (see Section 3.6, "Seconds
 *    Since Start of Epoch").
 * </pre>
 * @author Kasra Faghihi
 */
public final class ExternalAddressNatPmpResponse extends NatPmpResponse {
    private InetAddress address;

    /**
     * Constructs a {@link ExternalAddressNatPmpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if the version doesn't match the expected version (must always be {@code 0}), or if the op
     * {@code != 128}, or if there's an unsuccessful/unrecognized result code
     */
    public ExternalAddressNatPmpResponse(ByteBuffer buffer) {
        super(buffer);
        
        Validate.isTrue(getOp() == 128);
        
        byte[] addr = new byte[4];
        buffer.get(addr);
        
        try {
            address = InetAddress.getByAddress(addr);
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe); // should never happen, will always be 4 bytes
        }
    }

    /**
     * External IP address.
     * @return external IP address
     */
    public InetAddress getAddress() {
        return address;
    }
    
}
