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

import static com.offbynull.portmapper.natpmp.messages.InternalUtils.NAT_PMP_VERSION;
import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP request.
 * @author Kasra Faghihi
 */
public abstract class NatPmpRequest implements NatPmpMessage {
    private static final int HEADER_LENGTH = 2;
    
    private final int op;

    NatPmpRequest(int op) {
        // Checks are redundant, but keep anyways to be safe.
        this.op = op;
        
        validateState();
    }

    NatPmpRequest(byte[] buffer) {
        Validate.notNull(buffer);
        Validate.isTrue(buffer.length >= HEADER_LENGTH);
        
        int offset = 0;

        int version = buffer[offset] & 0xFF;
        Validate.isTrue(version == NAT_PMP_VERSION); // check pcp version
        offset++;
        
        op = buffer[offset] & 0xFF;
        offset++;
        
        validateState();
    }
    
    private void validateState() {
        Validate.inclusiveBetween(0, 127, op);
    }

    @Override
    public final int getOp() {
        return op;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.op;
        return hash;
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
        final NatPmpRequest other = (NatPmpRequest) obj;
        if (this.op != other.op) {
            return false;
        }
        return true;
    }
}
