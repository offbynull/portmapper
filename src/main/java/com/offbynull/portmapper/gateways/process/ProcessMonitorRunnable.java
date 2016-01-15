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

import com.offbynull.portmapper.gateway.Bus;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ProcessMonitorRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessMonitorRunnable.class);

    private final int id;
    private final Process process;
    private final Bus processBus;

    ProcessMonitorRunnable(int id, Process process, Bus processBus) {
        Validate.notNull(process);
        Validate.notNull(processBus);
        
        this.id = id;
        this.process = process;
        this.processBus = processBus;
    }
    
    @Override
    public void run() {
        LOG.debug("{} Starting up monitor", id);
        
        try {
            int exitCode = process.waitFor();
            
            LOG.debug("{} Process closed with exit code {}", id, exitCode);
            
            processBus.send(new TerminatedMessage(id, exitCode));
        } catch (RuntimeException e) {
            LOG.error(id + " Encountered exception", e);
        } catch (InterruptedException ie) {
            Thread.interrupted();
            LOG.error(id + " Interrupted", ie);
            
            process.destroy();
            processBus.send(new TerminatedMessage(id, null));
        } finally {
            LOG.debug("{} Shutting down monitor", id);
        }
    }
    
}
