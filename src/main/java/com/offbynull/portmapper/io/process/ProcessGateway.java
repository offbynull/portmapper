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
package com.offbynull.portmapper.io.process;

import com.offbynull.portmapper.Bus;

/**
 * Network communication gateway.
 *
 * @author Kasra Faghihi
 */
public final class ProcessGateway {

    private ProcessRunnable runnable;
    private Thread thread;
    
    public static ProcessGateway create() {
        ProcessGateway pg = new ProcessGateway();
        
        pg.runnable = new ProcessRunnable();
        pg.thread = new Thread(pg.runnable);
        pg.thread.setDaemon(true);
        pg.thread.setName("Process IO");
        
        pg.thread.start();
        
        return pg;
    }
    
    private ProcessGateway() {
        // do nothing
    }
    
    public Bus getBus() {
        return runnable.getBus();
    }
    
    public void join() throws InterruptedException {
        thread.join();
    }
}
