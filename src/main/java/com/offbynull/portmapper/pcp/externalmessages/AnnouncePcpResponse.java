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

/**
 * Represents an ANNOUNCE PCP response. This response has no op-code specific payload.
 * @author Kasra Faghihi
 */
public final class AnnouncePcpResponse extends PcpResponse {
    // FROM THE RFC:
    // The PCP ANNOUNCE Opcode requests and responses have no Opcode-specific payload (that is, the length of the Opcode-specific data is
    // zero).  The Requested Lifetime field of requests and Lifetime field of responses are both set to 0 on transmission and ignored on
    // reception.
    private static final int LIFETIME = 0;
    private static final int OPCODE = 0;
    private static final int OPCODE_SPECIFIC_DATA_LENGTH = 0;

    /**
     * Constructs a {@link AnnouncePcpResponse} object.
     * @param epochTime server's epoch time in seconds
     * @param resultCode result code
     * @param options PCP options
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if {@code 0L > epochTime > 0xFFFFFFFFL}
     */
    public AnnouncePcpResponse(int resultCode, long epochTime, PcpOption... options) {
        super(OPCODE, resultCode, LIFETIME, epochTime, OPCODE_SPECIFIC_DATA_LENGTH, options);
    }

    /**
     * Constructs a {@link AnnouncePcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 1 || op != 0}) or contains an unparseable options region.
     */
    public AnnouncePcpResponse(byte[] buffer) {
        super(buffer, OPCODE_SPECIFIC_DATA_LENGTH);
        // Validate.isTrue(getLifetime() == LIFETIME); // should be 0, but RFC says to ignore on reception -- this provides no extra
                                                       // information not does it doesn't corrupt/invalidate anything, so don't bother
                                                       // verifying
    }

    @Override
    public byte[] getData() {
        return new byte[OPCODE_SPECIFIC_DATA_LENGTH];
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
        // final AnnouncePcpResponse other = (AnnouncePcpResponse) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
}
