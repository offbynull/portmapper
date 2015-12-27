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
package com.offbynull.portmapper.natpmp.messages;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
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
    private static final int LENGTH = 12;
    private static final int OP = 128;

    private InetAddress inetAddress;

    /**
     * Constructs a {@link ExternalAddressNatPmpResponse} object by parsing a buffer.
     * @param buffer buffer containing NAT-PMP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} isn't the right size or is malformed ({@code op != 128 || version != 0 ||
     * 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || 0L > lifetime > 0xFFFFFFFFL})
     */
    public ExternalAddressNatPmpResponse(byte[] buffer) {
        super(buffer);

        Validate.notNull(buffer);
        Validate.isTrue(buffer.length == LENGTH);

        Validate.isTrue(getOp() == OP);
        
        byte[] addr = Arrays.copyOfRange(buffer, 8, 12);
        try {
            inetAddress = InetAddress.getByAddress(addr);
            Validate.validState(inetAddress instanceof Inet4Address); // should never happen -- sanity check
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe); // should never happen, will always be 4 bytes
        }
    }

    /**
     * Constructs a {@link ExternalAddressNatPmpResponse} object.
     * @param resultCode result code
     * @param secondsSinceStartOfEpoch seconds since start of epoch
     * @param inetAddress external IP address
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code !(inetAddress instanceof Inet4Address)}
     */
    public ExternalAddressNatPmpResponse(int resultCode, long secondsSinceStartOfEpoch, InetAddress inetAddress) {
        super(OP, resultCode, secondsSinceStartOfEpoch);
        
        Validate.notNull(inetAddress);
        Validate.isTrue(inetAddress instanceof Inet4Address);
        
        this.inetAddress = inetAddress;
    }

    @Override
    public byte[] dump() {
        byte[] data = new byte[LENGTH];

        data[0] = 0;
        data[1] = (byte) OP;
        InternalUtils.shortToBytes(data, 2, (short) getResultCode());
        InternalUtils.intToBytes(data, 4, (int) getSecondsSinceStartOfEpoch());
        byte[] addressBytes = inetAddress.getAddress();
        data[8] = addressBytes[0];
        data[9] = addressBytes[1];
        data[10] = addressBytes[2];
        data[11] = addressBytes[3];

        return data;
    }

    /**
     * External IP address.
     * @return external IP address
     */
    public InetAddress getAddress() {
        return inetAddress;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 79 * hash + Objects.hashCode(this.inetAddress);
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
        final ExternalAddressNatPmpResponse other = (ExternalAddressNatPmpResponse) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (!Objects.equals(this.inetAddress, other.inetAddress)) {
            return false;
        }
        return true;
    }
}
