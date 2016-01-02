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
package com.offbynull.portmapper.io;

import com.offbynull.portmapper.common.Bus;
import java.nio.channels.Channel;
import java.util.LinkedList;
import org.apache.commons.lang3.Validate;

abstract class NetworkEntry<B> {
    private int id;
    private Channel channel;
    private Bus responseBus;
    private int selectionKey;

    public NetworkEntry(int id, Channel channel, Bus responseBus) {
        Validate.notNull(channel);
        Validate.notNull(responseBus);
        
        this.id = id;
        this.channel = channel;
        this.responseBus = responseBus;
        this.selectionKey = 0;
    }

    public int getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public Bus getResponseBus() {
        return responseBus;
    }

    public int getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(int selectionKey) {
        this.selectionKey = selectionKey;
    }
    
    public abstract LinkedList<B> getOutgoingBuffers();
}
