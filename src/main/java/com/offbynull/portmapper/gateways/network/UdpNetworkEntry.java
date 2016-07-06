/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.gateways.network;

import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateways.network.UdpNetworkEntry.AddressedByteBuffer;
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
