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
package com.offbynull.portmapper.io.messages;

import java.net.InetSocketAddress;
import java.util.Arrays;

public final class WriteUdpBlockNetworkRequest implements NetworkRequest {
    private int id;
    private InetSocketAddress outgoingSocketAddress;
    private byte[] data;

    public WriteUdpBlockNetworkRequest(int id, InetSocketAddress outgoingSocketAddress, byte[] data) {
        this.id = id;
        this.outgoingSocketAddress = outgoingSocketAddress;
        this.data = Arrays.copyOf(data, data.length);
    }

    public int getId() {
        return id;
    }

    public InetSocketAddress getOutgoingSocketAddress() {
        return outgoingSocketAddress;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

}
