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
package com.offbynull.portmapper.gateways.process;

import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.Gateway;

/**
 * Process gateway.
 * @author Kasra Faghihi
 */
public final class ProcessGateway implements Gateway {

    private ProcessRunnable runnable;
    private Thread thread;
    
    /**
     * Creates a {@link ProcessGateway} object.
     * @return new {@link ProcessGateway}
     */
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
    
    @Override
    public Bus getBus() {
        return runnable.getBus();
    }
    
    @Override
    public void join() throws InterruptedException {
        thread.join();
    }
}
