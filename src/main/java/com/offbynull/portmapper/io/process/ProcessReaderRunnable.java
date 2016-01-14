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
import com.offbynull.portmapper.io.process.internalmessages.ReadType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ProcessReaderRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessReaderRunnable.class);

    private int id;
    private final InputStream inputStream;
    private final Bus processBus;
    private final ReadType readType;

    ProcessReaderRunnable(int id, InputStream inputStream, Bus processBus, ReadType readType) {
        Validate.notNull(inputStream);
        Validate.notNull(processBus);
        Validate.notNull(readType);
        
        this.id = id;
        this.inputStream = inputStream;
        this.processBus = processBus;
        this.readType = readType;
    }
    
    @Override
    public void run() {
        LOG.debug("{} Starting up reader {}", id, readType);
        
        byte[] buffer = new byte[8192];
        try {
            while (true) {
                int count = inputStream.read(buffer);
                if (count == -1) {
                    LOG.debug("{} {} ended", id, readType);
                    break;
                }
                
                LOG.debug("{} Read {} bytes from {}", id, count, readType);
                
                processBus.send(new ReadMessage(id, Arrays.copyOf(buffer, count), readType));
            }
        } catch (RuntimeException | IOException ioe) {
            LOG.error(id + " " + readType + " encountered exception", ioe);
        } finally {
            LOG.debug("{} Shutting down reader {}", id, readType);
        }
    }
    
}
