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
package com.offbynull.portmapper.io.process.internalmessages;

import java.util.Arrays;

/**
 * Send data to a process. Possible responses are {@link WriteProcesskResponse} and {@link IdentifiableErrorProcessResponse}).
 * @author Kasra Faghihi
 */
public final class WriteProcessRequest extends IdentifiableProcessRequest {
    private byte[] data;

    /**
     * Constructs a {@link WriteProcessRequest} object.
     * @param id id of process
     * @param data send data
     * @throws NullPointerException if any argument is {@code null}
     */
    public WriteProcessRequest(int id, byte[] data) {
        super(id);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Get send data.
     * @return send data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "WriteProcessRequest{super=" + super.toString() + "data=" + Arrays.toString(data) + '}';
    }

}
