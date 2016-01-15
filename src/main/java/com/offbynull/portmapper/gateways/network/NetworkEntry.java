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
package com.offbynull.portmapper.gateways.network;

import com.offbynull.portmapper.Bus;
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
