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
import java.nio.channels.Channel;
import java.util.LinkedList;
import org.apache.commons.lang3.Validate;

abstract class NetworkEntry<B> {
    private final int id;
    private final Bus responseBus;
    private final Channel channel;
    private int selectionKey;
    private boolean notifiedOfWritable;
    
    NetworkEntry(int id, Channel channel, Bus responseBus) {
        Validate.notNull(channel);
        Validate.notNull(responseBus);
        
        this.id = id;
        this.responseBus = responseBus;
        this.channel = channel;
        this.selectionKey = 0;
        this.notifiedOfWritable = false;
    }

    int getId() {
        return id;
    }

    Bus getResponseBus() {
        return responseBus;
    }

    Channel getChannel() {
        return channel;
    }

    int getSelectionKey() {
        return selectionKey;
    }

    void setSelectionKey(int selectionKey) {
        this.selectionKey = selectionKey;
    }

    boolean isNotifiedOfWritable() {
        return notifiedOfWritable;
    }

    void setNotifiedOfWritable(boolean notifiedOfWritable) {
        this.notifiedOfWritable = notifiedOfWritable;
    }
    
    abstract LinkedList<B> getOutgoingBuffers();
}
