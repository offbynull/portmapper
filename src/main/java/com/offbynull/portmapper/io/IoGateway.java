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

/**
 * Network communication gateway.
 *
 * @author Kasra Faghihi
 */
public final class IoGateway {

    private IoRunnable runnable;
    private Thread thread;
    
    public static IoGateway create() {
        IoGateway ng = new IoGateway();
        
        ng.runnable = new IoRunnable();
        ng.thread = new Thread(ng.runnable);
        
        ng.thread.start();
        
        return ng;
    }
    
    private IoGateway() {
        // do nothing
    }
    
    public Bus getBus() {
        return runnable.getBus();
    }
    
    public void join() throws InterruptedException {
        thread.join();
    }
}
