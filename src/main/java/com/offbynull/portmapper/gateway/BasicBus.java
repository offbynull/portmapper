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
package com.offbynull.portmapper.gateway;

import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.Validate;

/**
 * A bus that's backed by a {@link LinkedBlockingQueue}.
 * @author Kasra Faghihi
 */
public final class BasicBus implements Bus {
    private LinkedBlockingQueue<Object> queue;

    /**
     * Constructs a {@link BasicBus} object.
     * @param queue internal queue to use for this bus
     * @throws NullPointerException if any argument is {@code null}
     */
    public BasicBus(LinkedBlockingQueue<Object> queue) {
        Validate.notNull(queue);
        this.queue = queue;
    }

    @Override
    public void send(Object msg) {
        Validate.notNull(msg);
        queue.add(msg);
    }
}
