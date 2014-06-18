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

import java.nio.ByteBuffer;

/**
 * Represents an ANNOUNCE PCP request. This request has no op-code specific payload.
 * @author Kasra Faghihi
 */
public final class AnnouncePcpRequest extends PcpRequest {

    /**
     * Constructs a {@link AnnouncePcpRequest} object.
     * @param options PCP options to use
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     */
    public AnnouncePcpRequest(PcpOption ... options) {
        super(0, 0L, options);
    }

    @Override
    protected void dumpOpCodeSpecificInformation(ByteBuffer dst) {
        // no opcode specific data
    }
    
}
