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

import java.nio.BufferOverflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP request. Provides NAT-PMP request header construction functionality, which is basically just the version and the OP.
 * @author Kasra Faghihi
 */
public abstract class NatPmpRequest {
    private int op;

    /**
     * Constructs a {@link NatPmpRequest} object.
     * @param op NAT-PMP opcode
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code op > 127}
     */
    public NatPmpRequest(int op) {
        Validate.inclusiveBetween(0, 127, op);
        this.op = op;
    }

    /**
     * Get opcode.
     * @return opcode
     */
    public final int getOp() {
        return op;
    }
    
    /**
     * Dump this NAT-PMP request in to a byte buffer.
     * @param dst byte buffer to dump to
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferOverflowException if {@code dst} doesn't have enough space to write this option
     * @throws ReadOnlyBufferException if {@code dst} is read-only
     */
    public final void dump(ByteBuffer dst) {
        Validate.notNull(dst);
        
        dst.put((byte) 0);
        dst.put((byte) op);
        
        dumpOpCodeSpecificInformation(dst);
    }
    
    /**
     * Called by {@link #dump(java.nio.ByteBuffer) } to write op-code specific data to the buffer.
     * @param dst byte buffer to dump to
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferOverflowException if {@code dst} doesn't have enough space to write this option
     * @throws ReadOnlyBufferException if {@code dst} is read-only
     */
    protected abstract void dumpOpCodeSpecificInformation(ByteBuffer dst);
}
