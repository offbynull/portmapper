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

import java.util.Arrays;
import org.apache.commons.lang3.Validate;
/**
 * Represents an unrecognized NAT-PMP request.
 * @author Kasra Faghihi
 */
public final class UnknownNatPmpRequest implements NatPmpRequest {

    private byte[] data;

    /**
     * Construct an {@link UnknownNatPmpRequest} object.
     * @param data buffer containing NAT-PMP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if not enough data is available in {@code data}, or if the version doesn't match the expected
     * version (must always be {@code 0}), or if the op is not in the request op range (must always be {@code >= 0} and {@code <= 127})
     */
    public UnknownNatPmpRequest(byte[] data) {
        Validate.notNull(data);
        InternalUtils.parseNatPmpRequestHeader(data);
        
        this.data = Arrays.copyOf(data, data.length);
    }

    @Override
    public byte[] dump() {
        return Arrays.copyOf(data, data.length);
    }
}
