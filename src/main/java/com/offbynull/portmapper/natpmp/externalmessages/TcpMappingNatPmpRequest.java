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
package com.offbynull.portmapper.natpmp.externalmessages;

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
     * @throws IllegalArgumentException if {@code buffer} isn't the right size or is malformed ({@code op != 2 || version != 0 ||
     * @code 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || 0 > lifetime > 0xFFFFFFFFL})
     */
    public TcpMappingNatPmpRequest(byte[] data) {
        super(OP, data);
    }

    /**
     * Construct a {@link TcpMappingNatPmpRequest} object.
     * @param internalPort internal port
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param lifetime desired lifetime of mapping ({@code 0} to destroy mapping)
     * @throws IllegalArgumentException if {@code 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535
     * || 0 > lifetime > 0xFFFFFFFFL}
     */
    public TcpMappingNatPmpRequest(int internalPort, int suggestedExternalPort, long lifetime) {
        super(OP, internalPort, suggestedExternalPort, lifetime);
    }
}
