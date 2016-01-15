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
package com.offbynull.portmapper.mappers.pcp;

import com.offbynull.portmapper.gateway.BasicBus;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateways.process.internalmessages.CloseProcessRequest;
import com.offbynull.portmapper.gateways.process.internalmessages.CreateProcessRequest;
import com.offbynull.portmapper.gateways.process.internalmessages.ErrorProcessResponse;
import com.offbynull.portmapper.gateways.process.internalmessages.ExitProcessNotification;
import com.offbynull.portmapper.gateways.process.internalmessages.GetNextIdProcessRequest;
import com.offbynull.portmapper.gateways.process.internalmessages.GetNextIdProcessResponse;
import com.offbynull.portmapper.gateways.process.internalmessages.IdentifiableErrorProcessResponse;
import com.offbynull.portmapper.gateways.process.internalmessages.ReadProcessNotification;
import com.offbynull.portmapper.gateways.process.internalmessages.ReadType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class InternalUtils {
    private static final Logger LOG = LoggerFactory.getLogger(InternalUtils.class);
    
    private InternalUtils() {
        
    }
    
    static Set<String> runCommandline(Bus processBus, RunProcessRequest ... reqs) throws InterruptedException {

        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus selfBus = new BasicBus(queue);
        long timeout = 10000L;
        long endTime = System.currentTimeMillis() + timeout;

        // Create processes
        Map<Integer, ByteArrayOutputStream> readBuffers = new HashMap<>();
        next:
        for (RunProcessRequest req : reqs) {
            LOG.debug("Starting process {}", req);

            int id;

            processBus.send(new GetNextIdProcessRequest(selfBus));
            while (true) {
                long sleepTime = endTime - System.currentTimeMillis();
                Validate.validState(sleepTime > 0, "Failed to create all processes in time");

                Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);
                if (resp instanceof GetNextIdProcessResponse) {
                    id = ((GetNextIdProcessResponse) resp).getId();
                    break;
                }
            }
            
            processBus.send(new CreateProcessRequest(id, selfBus, req.getExecutable(), req.getParameters()));
        }

        // Read data from sockets
        int runningProcs = reqs.length;
        while (runningProcs > 0) {
            long sleepTime = endTime - System.currentTimeMillis();
            if (sleepTime <= 0L) {
                break;
            }

            Object resp = queue.poll(sleepTime, TimeUnit.MILLISECONDS);

            if (resp instanceof ReadProcessNotification) {
                // On read, put in to readBuffer
                ReadProcessNotification readResp = (ReadProcessNotification) resp;
                if (readResp.getReadType() == ReadType.STDOUT) {
                    Integer id = readResp.getId();

                    ByteArrayOutputStream baos = readBuffers.get(id);
                    if (baos == null) {
                        baos = new ByteArrayOutputStream();
                        readBuffers.put(id, baos);
                    }

                    try {
                        baos.write(readResp.getData());
                    } catch (IOException ioe) {
                        throw new IllegalStateException(); // should never happen
                    }
                }
            } else if (resp instanceof ExitProcessNotification) {
                runningProcs--;
            } else if (resp instanceof IdentifiableErrorProcessResponse) {
                runningProcs--;
            } else if (resp instanceof ErrorProcessResponse) {
                runningProcs--;
            }
        }

        // Issue closes
        for (int id : readBuffers.keySet()) {
            processBus.send(new CloseProcessRequest(id));
        }

        // Process responses
        Set<String> ret = new HashSet<>();
        for (Entry<Integer, ByteArrayOutputStream> entry : readBuffers.entrySet()) {
            String resp = new String(entry.getValue().toByteArray(), Charset.forName("US-ASCII"));
            LOG.debug("Process respose {}", resp);
            ret.add(resp);
        }
        
        return ret;
    }

    static final class RunProcessRequest {
        private String executable;
        private String[] parameters;
        
        RunProcessRequest(String executable, String ... parameters) {
            this.executable = executable;
            this.parameters = parameters;
        }

        public String[] getParameters() {
            return parameters;
        }

        public void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        public String getExecutable() {
            return executable;
        }

        public void setExecutable(String executable) {
            this.executable = executable;
        }

        @Override
        public String toString() {
            return "RunProcessRequest{" + "executable=" + executable + ", parameters=" + Arrays.toString(parameters) + '}';
        }
        
    }
}
