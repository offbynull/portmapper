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
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.util.LinkedList;

final class TcpNetworkEntry extends NetworkEntry<ByteBuffer> {
    private boolean connecting = true;
    private boolean readFinished;
    private LinkedList<ByteBuffer> outgoingBuffers;

    TcpNetworkEntry(int id, Channel channel, Bus responseBus) {
        super(id, channel, responseBus);
        outgoingBuffers = new LinkedList<>();
    }

    @Override
    public LinkedList<ByteBuffer> getOutgoingBuffers() {
        return outgoingBuffers;
    }

    boolean isConnecting() {
        return connecting;
    }

    void setConnecting(boolean connecting) {
        this.connecting = connecting;
    }

    public boolean isReadFinished() {
        return readFinished;
    }

    public void setReadFinished(boolean readFinished) {
        this.readFinished = readFinished;
    }
    
}
