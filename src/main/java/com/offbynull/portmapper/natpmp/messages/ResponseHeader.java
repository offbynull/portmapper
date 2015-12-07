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

import org.apache.commons.lang3.Validate;

final class ResponseHeader {
    
    private final int op;
    private final int resultCode;
    private final long secondsSinceStartOfEpoch;

    public ResponseHeader(int op, int resultCode, long secondsSinceStartOfEpoch) {
        Validate.inclusiveBetween(128, 255, op, "Op is out of range: %d", op);
        Validate.inclusiveBetween(0, 5, resultCode, "Result code is out of range: %d", op);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, secondsSinceStartOfEpoch, "Epoch is out of range: %d", secondsSinceStartOfEpoch);
        this.op = op;
        this.resultCode = resultCode;
        this.secondsSinceStartOfEpoch = secondsSinceStartOfEpoch;
    }

    public int getOp() {
        return op;
    }

    public int getResultCode() {
        return resultCode;
    }

    public long getSecondsSinceStartOfEpoch() {
        return secondsSinceStartOfEpoch;
    }
    
}
