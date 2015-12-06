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

import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents an ANNOUNCE PCP response. This response has no op-code specific payload.
 * @author Kasra Faghihi
 */
public final class AnnouncePcpResponse extends PcpResponse {

    /**
     * Constructs a {@link AnnouncePcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if there's not enough or too much data remaining in the buffer, or if the version doesn't match the
     * expected version (must always be {@code 2}), or if the r-flag isn't set, or if there's an unsuccessful/unrecognized result code,
     * or if the op code doesn't match the ANNOUNCE opcode, or if there were problems parsing options
     */
    public AnnouncePcpResponse(ByteBuffer buffer) {
        super(buffer);
        
        Validate.isTrue(super.getOp() == 0);
        
        parseOptions(buffer);
    }
}
