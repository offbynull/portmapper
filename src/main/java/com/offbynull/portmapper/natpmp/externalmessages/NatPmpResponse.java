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
package com.offbynull.portmapper.natpmp.externalmessages;

import static com.offbynull.portmapper.natpmp.externalmessages.InternalUtils.NAT_PMP_VERSION;
import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP response.
 * @author Kasra Faghihi
 */
public abstract class NatPmpResponse implements NatPmpMessage {
    private static final int HEADER_LENGTH = 8;
    
    private int op;
    private int resultCode;
    private long secondsSinceStartOfEpoch;

    NatPmpResponse(int op, int resultCode, long secondsSinceStartOfEpoch) {
        this.op = op;
        this.resultCode = resultCode;
        this.secondsSinceStartOfEpoch = secondsSinceStartOfEpoch;
        
        validateState();
    }

    NatPmpResponse(byte[] buffer) {
        Validate.notNull(buffer);
        Validate.isTrue(buffer.length >= HEADER_LENGTH);
        
        int offset = 0;

        int version = buffer[offset] & 0xFF;
        Validate.isTrue(version == NAT_PMP_VERSION); // check pcp version
        offset++;
        
        op = buffer[offset] & 0xFF;
        offset++;

        resultCode = InternalUtils.bytesToShort(buffer, offset);
        offset += 2;
        
        secondsSinceStartOfEpoch = InternalUtils.bytesToInt(buffer, offset) & 0xFFFFFFFFL;
        offset += 4;
        
        validateState();
    }

    private void validateState() {
        Validate.inclusiveBetween(128, 255, op);
        Validate.inclusiveBetween(0, 65535, resultCode);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, secondsSinceStartOfEpoch);
    }

    @Override
    public final int getOp() {
        return op;
    }
        
    /**
     * Get result code. 0 means success.
     * @return result code
     */
    public final int getResultCode() {
        return resultCode;
    }

    /**
     * Get the number of seconds since the device's port mapping table was initialized/reset.
     * @return number of seconds the device's port mapping table has been up (up to {@code 0xFFFFFFFFL})
     */
    public final long getSecondsSinceStartOfEpoch() {
        return secondsSinceStartOfEpoch;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.op;
        hash = 59 * hash + this.resultCode;
        hash = 59 * hash + (int) (this.secondsSinceStartOfEpoch ^ (this.secondsSinceStartOfEpoch >>> 32));
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
        final NatPmpResponse other = (NatPmpResponse) obj;
        if (this.op != other.op) {
            return false;
        }
        if (this.resultCode != other.resultCode) {
            return false;
        }
        if (this.secondsSinceStartOfEpoch != other.secondsSinceStartOfEpoch) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
