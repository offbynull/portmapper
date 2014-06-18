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
package com.offbynull.portmapper.natpmp;

import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP response. Provides NAT-PMP response header construction functionality, which is basically just the version, the OP,
 * the result code, and the seconds since start of epoch.
 * @author Kasra Faghihi
 */
public abstract class NatPmpResponse {
    private int op;
    private long secondsSinceStartOfEpoch;
    
    /**
     * Constructs a {@link NatPmpResponse} object by parsing a buffer.
     * @param buffer buffer containing NAT-PMP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if the version doesn't match the expected version (must always be {@code 0}), or if the op is
     * {@code < 128}, or if there's an unsuccessful/unrecognized result code
     */
    NatPmpResponse(ByteBuffer buffer) {
        Validate.notNull(buffer);
        
        if (buffer.remaining() < 2) {
            throw new IllegalArgumentException("Bad packet size: " + buffer.remaining());
        }
        
        int version = buffer.get() & 0xFF;
        Validate.isTrue(version == 0, "Unknown version: %d", version);
        
        op = buffer.get() & 0xFF;
        Validate.isTrue(op >= 128, "Op must be >= 128: %d", op);

        int resultCodeNum = buffer.getShort() & 0xFFFF;
        NatPmpResultCode[] resultCodes = NatPmpResultCode.values();
        
        Validate.isTrue(resultCodeNum < resultCodes.length, "Unknown result code encountered: %d", resultCodeNum);
        Validate.isTrue(resultCodeNum == NatPmpResultCode.SUCCESS.ordinal(), "Unsuccessful result code: %s [%s]",
                resultCodes[resultCodeNum].toString(), resultCodes[resultCodeNum].getMessage());
        
        secondsSinceStartOfEpoch = buffer.getInt() & 0xFFFFFFFFL;
    }
    
    /**
     * Get opcode.
     * @return opcode
     */
    public final int getOp() {
        return op;
    }
    
    /**
     * Get the number of seconds since the start of epoch. AKA seconds since this device rebooted.
     * @return number of seconds since start of epoch
     */
    public final long getSecondsSinceStartOfEpoch() {
        return secondsSinceStartOfEpoch;
    }
}
