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

import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents a PREFER_FAILURE PCP option. From the RFC:
 * <pre>
 *    The PREFER_FAILURE option is formatted as follows:
 * 
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Option Code=2 |  Reserved     |   Option Length=0             |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                      Figure 14: PREFER_FAILURE Option
 * 
 *       Option Name: PREFER_FAILURE
 *       Number: 2
 *       Purpose: indicates that the PCP server should not create an
 *       alternative mapping if the suggested external port and address
 *       cannot be mapped.
 *       Valid for Opcodes: MAP
 *       Length: 0
 *       May appear in: request.  May appear in response only if it
 *       appeared in the associated request.
 *       Maximum occurrences: 1
 * </pre>
 * @author Kasra Faghihi
 */
public final class PreferFailurePcpOption extends PcpOption {
    
    /**
     * Constructs a {@link PreferFailurePcpOption} by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if option code is not {@code 2}
     */
    public PreferFailurePcpOption(ByteBuffer buffer) {
        super(buffer);
        Validate.isTrue(super.getCode() == 2);
    }

    /**
     * Constructs a {@link PreferFailurePcpOption}.
     */
    public PreferFailurePcpOption() {
        super(2, ByteBuffer.allocate(0));
    }
}
