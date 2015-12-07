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

/**
 * Represents a NAT-PMP TCP mapping request.
 * @author Kasra Faghihi
 */
public final class TcpMappingNatPmpRequest extends MappingNatPmpRequest {
    private static final int OP = 2;
    
    /**
     * Construct a {@link TcpMappingNatPmpRequest} object.
     * @param data buffer containing NAT-PMP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if not enough data is available in {@code data}, or if the version doesn't match the expected
     * version (must always be {@code 2}), or if the op {@code != 0}
     */
    public TcpMappingNatPmpRequest(byte[] data) {
        super(OP, data);
    }

    /**
     * Construct a {@link TcpMappingNatPmpRequest} object.
     * @param internalPort internal port
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param lifetime desired lifetime of mapping ({@code 0} to destroy mapping)
     * @throws IllegalArgumentException if {@code internalPort < 1 || > 65535}, or if {@code suggestedExternalPort < 0 || > 65535}, or if
     * {@code lifetime < 0 || > 0xFFFFFFFFL}
     */
    public TcpMappingNatPmpRequest(int internalPort, int suggestedExternalPort, long lifetime) {
        super(OP, internalPort, suggestedExternalPort, lifetime);
    }
}
