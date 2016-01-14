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

import com.offbynull.portmapper.BasicBus;
import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.helpers.ByteBufferUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ProcessWriterRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessWriterRunnable.class);

    private int id;
    private final OutputStream outputStream;
    private final LinkedBlockingQueue<Object> localInputBusQueue;
    private final Bus localInputBus;
    private final Bus processBus;

    ProcessWriterRunnable(int id, OutputStream outputStream, Bus processBus) {
        Validate.notNull(outputStream);
        Validate.notNull(processBus);
        
        this.id = id;
        this.outputStream = outputStream;
        this.localInputBusQueue = new LinkedBlockingQueue<>();
        this.localInputBus = new BasicBus(localInputBusQueue);
        this.processBus = processBus;
    }

    Bus getLocalInputBus() {
        return localInputBus;
    }
    
    @Override
    public void run() {
        LOG.debug("{} Starting up writer", id);
        
        try {
            while (true) {
                ByteBuffer sendBuffer = (ByteBuffer) localInputBusQueue.poll();
                if (sendBuffer == null) {
                    LOG.debug("{} Write empty", id);
                    processBus.send(new WriteEmptyMessage(id));
                    sendBuffer = (ByteBuffer) localInputBusQueue.take();
                }
                byte[] buffer = ByteBufferUtils.copyContentsToArray(sendBuffer);
                outputStream.write(buffer);

                LOG.debug("{} Write {} bytes", id, buffer.length);
            }
        } catch (RuntimeException | IOException ioe) {
            LOG.error(id + " Encountered exception", ioe);
        } catch (InterruptedException ie) {
            Thread.interrupted();
            LOG.error(id + " Interrupted", ie);
        } finally {
            LOG.debug("{} Shutting down writer", id);
        }
    }
    
}
