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
package com.offbynull.portmapper.io.network;

import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.io.network.UdpNetworkEntry.AddressedByteBuffer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.util.LinkedList;

final class UdpNetworkEntry extends NetworkEntry<AddressedByteBuffer> {
    private LinkedList<AddressedByteBuffer> outgoingBuffers;

    UdpNetworkEntry(int id, Channel channel, Bus responseBus) {
        super(id, channel, responseBus);
        outgoingBuffers = new LinkedList<>();
    }

    @Override
    LinkedList<AddressedByteBuffer> getOutgoingBuffers() {
        return outgoingBuffers;
    }
    
    static final class AddressedByteBuffer  {
        private ByteBuffer buffer;
        private InetSocketAddress socketAddres;

        AddressedByteBuffer(ByteBuffer buffer, InetSocketAddress socketAddres) {
            this.buffer = buffer;
            this.socketAddres = socketAddres;
        }

        ByteBuffer getBuffer() {
            return buffer;
        }

        InetSocketAddress getSocketAddress() {
            return socketAddres;
        }
        
    }
}
