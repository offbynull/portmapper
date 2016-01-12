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
    private static final int OP_CODE = 2;
    private static final int DATA_LENGTH = 0;

    /**
     * Constructs a {@link PreferFailurePcpOption} by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @param offset offset in {@code buffer} where the PCP option starts
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} is malformed (doesn't contain enough bytes
     * / length is not a multiple of 4 (not enough padding) / data doesn't contain enough bytes / code is not 2)
     */
    public PreferFailurePcpOption(byte[] buffer, int offset) {
        super(buffer, offset);
        
        Validate.isTrue(super.getCode() == OP_CODE);
        Validate.isTrue(super.getDataLength() == DATA_LENGTH);
    }

    /**
     * Constructs a {@link ThirdPartyPcpOption}.
     */
    public PreferFailurePcpOption() {
        super(OP_CODE, DATA_LENGTH);
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
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
        // final PreferFailurePcpOption other = (PreferFailurePcpOption) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
}
