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

/**
 * TCP socket has received data.
 * @author Kasra Faghihi
 */
public final class ReadTcpNetworkNotification extends IdentifiableNetworkNotification {
    private byte[] data;

    /**
     * Constructs a {@link ReadTcpNetworkNotification} object.
     * @param id id of socket
     * @param data received data
     * @throws NullPointerException if any argument is {@code null}
     */
    public ReadTcpNetworkNotification(int id, byte[] data) {
        super(id);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Get data.
     * @return data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

}
