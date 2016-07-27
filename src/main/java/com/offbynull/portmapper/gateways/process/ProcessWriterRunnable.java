/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.gateways.process;

import com.offbynull.portmapper.gateway.BasicBus;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.helpers.ByteBufferUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.io.IOUtils;
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
            LOG.debug(id + " Interrupted");
        } finally {
            IOUtils.closeQuietly(outputStream);
            LOG.debug("{} Shutting down writer", id);
        }
    }
    
}
