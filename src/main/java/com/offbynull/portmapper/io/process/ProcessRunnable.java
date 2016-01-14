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
import com.offbynull.portmapper.io.process.internalmessages.CreateProcessRequest;
import com.offbynull.portmapper.io.process.internalmessages.CreateProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.CloseProcessRequest;
import com.offbynull.portmapper.io.process.internalmessages.ErrorProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.ExitProcessNotification;
import com.offbynull.portmapper.io.process.internalmessages.IdentifiableErrorProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.KillProcessRequest;
import com.offbynull.portmapper.io.process.internalmessages.ReadProcessNotification;
import com.offbynull.portmapper.io.process.internalmessages.ReadType;
import com.offbynull.portmapper.io.process.internalmessages.WriteEmptyProcessNotification;
import com.offbynull.portmapper.io.process.internalmessages.WriteProcessRequest;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ProcessRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessRunnable.class);
    
    private final Bus bus;
    private final LinkedBlockingQueue<Object> queue;
    private int nextId = 0;

    ProcessRunnable() {
        queue = new LinkedBlockingQueue<>();
        bus = new BasicBus(queue);
    }
    private Map<Integer, ProcessEntry> idMap = new HashMap<>();

    public Bus getBus() {
        return bus;
    }

    @Override
    public void run() {
        LOG.debug("Starting gateway");
        try {
            while (true) {
                Object msg = queue.take();
                processMessage(msg);
            }
        } catch (KillRequestException kre) {
            // do nothing
        } catch (Exception e) {
            LOG.error("Encountered unexpected exception", e);
            throw new RuntimeException(e); // rethrow exception
        } finally {
            LOG.debug("Stopping gateway");
            shutdownResources();
            LOG.debug("Shutdown of resources complete");
        }
    }

    private void processMessage(Object msg) {
        LOG.debug("Processing message: {}", msg);
        
        if (msg instanceof CreateProcessRequest) {
            CreateProcessRequest req = (CreateProcessRequest) msg;
            Bus responseBus = req.getResponseBus();
            Process process = null;
            Thread monitorThread = null;
            Thread stdoutThread = null;
            Thread stderrThread = null;
            Thread stdinThread = null;
            try {
                String executable = req.getExecutable();
                UnmodifiableList<String> parameters = req.getParameters();
                List<String> command = new LinkedList<>();
                command.add(executable);
                command.addAll(parameters);
                ProcessBuilder pb = new ProcessBuilder(command);
                process = pb.start();
                
                int id = nextId++;
                
                ProcessMonitorRunnable monitorRunnable = new ProcessMonitorRunnable(id, process, bus);
                monitorThread = new Thread(monitorRunnable);
                monitorThread.setDaemon(true);
                monitorThread.setName("Process Monitor");
                ProcessReaderRunnable stdoutRunnable = new ProcessReaderRunnable(id, process.getInputStream(), bus,
                        ReadType.STDOUT);
                stdoutThread = new Thread(stdoutRunnable);
                stdoutThread.setDaemon(true);
                stdoutThread.setName("Stdout Monitor");
                ProcessReaderRunnable stderrRunnable = new ProcessReaderRunnable(id, process.getErrorStream(), bus,
                        ReadType.STDERR);
                stderrThread = new Thread(stderrRunnable);
                stderrThread.setDaemon(true);
                stderrThread.setName("Stderr Monitor");
                ProcessWriterRunnable stdinRunnable = new ProcessWriterRunnable(id, process.getOutputStream(), bus);
                stdinThread = new Thread(stdinRunnable);
                stdinThread.setDaemon(true);
                stdinThread.setName("Stdin Monitor");
                
                ProcessEntry entry = new ProcessEntry(process, monitorThread, stdinThread, stdoutThread, stderrThread,
                        stdinRunnable.getLocalInputBus(), id, responseBus);
                responseBus.send(new CreateProcessResponse(id));
                idMap.put(id, entry);
                
                // just assume at this point it'll never fuck up -- if we get to the point where we're starting threads and one of the
                // threads fails to start, something happened that we didn't expect / couldn't predict
                stdoutThread.start();
                stderrThread.start();
                stdinThread.start();
                monitorThread.start();
            } catch (IOException | RuntimeException re) {
                LOG.error("Unable to create process", re);
                if (stdoutThread != null) {
                    stdoutThread.interrupt();
                }
                if (stderrThread != null) {
                    stderrThread.interrupt();
                }
                if (stdinThread != null) {
                    stdinThread.interrupt();
                }
                if (monitorThread != null) {
                    monitorThread.interrupt();
                }
                if (process != null) {
                    process.destroy();
                }

                responseBus.send(new ErrorProcessResponse());
            }
        } else if (msg instanceof CloseProcessRequest) {
            CloseProcessRequest req = (CloseProcessRequest) msg;
            int id = req.getId();
            ProcessEntry entry = idMap.get(id);
            if (entry != null) {
                entry.getProcess().destroy();
                // what happens next is that the thread responsible for checking the process state will find out that it died, then send a
                // "TerminatedMessage" back to this gateway to initiate cleanup
            }
        } else if (msg instanceof TerminatedMessage) {
            // sent internally once process exits -- not by user
            TerminatedMessage req = (TerminatedMessage) msg;
            int id = req.getId();
            try {
                ProcessEntry entry = idMap.remove(id);
                if (entry != null) {
                    Bus responseBus = entry.getResponseBus();
                    entry.getProcess().destroy();
                    entry.getStdoutThread().interrupt();
                    entry.getStderrThread().interrupt();
                    entry.getStdinThread().interrupt();
                    entry.getExitThread().interrupt();
                    
                    Integer exitCode = req.getExitCode();
                    
                    if (exitCode == null) {
                        responseBus.send(new IdentifiableErrorProcessResponse(id));
                    } else {
                        responseBus.send(new ExitProcessNotification(id, exitCode));
                    }
                }
            } catch (RuntimeException re) {
                LOG.error("Unable to process message", re);
                // do nothing, process should alreayd be dead at this point
            }
        } else if (msg instanceof WriteEmptyMessage) {
            // sent internally once process has nothing else to write out
            WriteEmptyMessage req = (WriteEmptyMessage) msg;
            int id = req.getId();
            ProcessEntry entry = idMap.get(id);
            if (entry != null) {
                Bus responseBus = entry.getResponseBus();
                responseBus.send(new WriteEmptyProcessNotification(id));
            }
        } else if (msg instanceof ReadMessage) {
            // sent internally once process has read something in
            ReadMessage req = (ReadMessage) msg;
            int id = req.getId();
            ProcessEntry entry = idMap.get(id);
            if (entry != null) {
                Bus responseBus = entry.getResponseBus();
                responseBus.send(new ReadProcessNotification(id, req.getData(), req.getReadType()));
            }
        } else if (msg instanceof WriteProcessRequest) {
            WriteProcessRequest req = (WriteProcessRequest) msg;
            Bus responseBus = null;
            Process process = null;
            int id = req.getId();
            try {
                ProcessEntry entry = idMap.get(id);
                responseBus = entry.getResponseBus();
                process = entry.getProcess();
                entry.getStdinBus().send(ByteBuffer.wrap(req.getData()));
            } catch (RuntimeException re) {
                if (process != null) {
                    process.destroy();
                } else if (responseBus != null) {
                    responseBus.send(new IdentifiableErrorProcessResponse(id));
                }
            }
        } else if (msg instanceof KillProcessRequest) {
            throw new KillRequestException();
        }
    }

    private void shutdownResources() {
        LOG.debug("Shutting down all resources");
        
        for (Entry<Integer, ProcessEntry> entry : idMap.entrySet()) {
            int id = entry.getKey();
            
            LOG.debug("{} Attempting to shutdown", id);
            
            ProcessEntry pe = entry.getValue();

            try {
                pe.getProcess().destroy();
                pe.getStdoutThread().interrupt();
                pe.getStderrThread().interrupt();
                pe.getStdinThread().interrupt();
                pe.getStdoutThread().join();
                pe.getStderrThread().join();
                pe.getStdinThread().join();
                pe.getExitThread().join();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            } catch (RuntimeException e) {
                LOG.error(id + " Error shutting down resource", e);
            }

            // shutdownResources() is the last thing that gets called before the ProcessRunnable thread gets shut down. Any messages put on
            // the ProcessRunnable bus by the threads that were interrupted will never be processed, including notifications of the process
            // stopping. As such, we send the notification here that the process is being forcefully stopped.
            pe.getResponseBus().send(new ExitProcessNotification(id, null));
        }
        idMap.clear();
    }
    
    
    private static final class KillRequestException extends RuntimeException {
        private static final long serialVersionUID = 1L;

    }
}
