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
package com.offbynull.portmapper.io.network.internalmessages;

import java.util.Arrays;
import org.apache.commons.lang3.Validate;

/**
 * Send data to a TCP socket. Possible responses are {@link WriteTcpNetworkResponse} and {@link IdentifiableErrorNetworkResponse}).
 * @author Kasra Faghihi
 */
public final class WriteTcpNetworkRequest extends IdentifiableNetworkRequest {
    private byte[] data;

    /**
     * Constructs a {@link WriteTcpNetworkRequest} object.
     * @param id id of socket
     * @param data send data
     * @throws NullPointerException if any argument is {@code null}
     */
    public WriteTcpNetworkRequest(int id, byte[] data) {
        super(id);
        Validate.notNull(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Get send data.
     * @return send data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

}
