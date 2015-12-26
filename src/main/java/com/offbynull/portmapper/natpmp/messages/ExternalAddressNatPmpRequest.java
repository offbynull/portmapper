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

import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP external address request. From the RFC:
 * <pre>
 * 3.2.  Determining the External Address
 * 
 *    To determine the external address, the client behind the NAT sends
 *    the following UDP payload to port 5351 of the configured gateway
 *    address:
 * 
 *     0                   1
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Vers = 0      | OP = 0        |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * @author Kasra Faghihi
 */
public final class ExternalAddressNatPmpRequest extends NatPmpRequest {

    private static final int LENGTH = 2;
    private static final int OP = 0;

    /**
     * Construct a {@link ExternalAddressNatPmpRequest} object.
     * @param buffer buffer containing NAT-PMP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if not enough data is available in {@code data}, or if the version doesn't match the expected
     * version (must always be {@code 0}), or if the op {@code != 0}
     */
    public ExternalAddressNatPmpRequest(byte[] buffer) {
        super(buffer);

        Validate.notNull(buffer);
        Validate.isTrue(buffer.length == LENGTH);

        Validate.isTrue(getOp() == OP);
    }

    /**
     * Construct a {@link ExternalAddressNatPmpRequest} object.
     */
    public ExternalAddressNatPmpRequest() {
        super(OP);
    }

    @Override
    public byte[] dump() {
        return new byte[] {0, OP};
    }
}
