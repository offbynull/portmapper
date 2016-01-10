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

import org.apache.commons.lang3.Validate;

final class ProcessExitRunnable implements Runnable {

    private final int id;
    private final Process process;
    private final Bus processBus;

    public ProcessExitRunnable(int id, Process process, Bus processBus) {
        Validate.notNull(process);
        Validate.notNull(processBus);
        
        this.id = id;
        this.process = process;
        this.processBus = processBus;
    }
    
    @Override
    public void run() {
        try {
            int exitCode = process.waitFor();
            processBus.send(new TerminatedMessage(exitCode, id));
        } catch (InterruptedException ioe) {
            process.destroy();
            processBus.send(new TerminatedMessage(null, id));
        }
    }
    
}
