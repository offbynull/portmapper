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

/**
 * Network communication gateway.
 *
 * @author Kasra Faghihi
 */
public final class NetworkGateway {

    private NetworkRunnable runnable;
    private Thread thread;
    
    public static NetworkGateway create() {
        NetworkGateway ng = new NetworkGateway();
        
        ng.runnable = new NetworkRunnable();
        ng.thread = new Thread(ng.runnable);
        
        ng.thread.start();
        
        return ng;
    }
    
    private NetworkGateway() {
        // do nothing
    }
    
    public Bus getBus() {
        return runnable.getBus();
    }
    
    public void join() throws InterruptedException {
        thread.join();
    }
}
