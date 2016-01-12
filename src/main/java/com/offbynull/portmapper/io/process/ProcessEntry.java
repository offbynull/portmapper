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

final class ProcessEntry {
    
    private final int id;
    private final Bus responseBus;
    private final Process process;
    private final Thread exitThread;
    private final Thread stdinThread;
    private final Thread stdoutThread;
    private final Thread stderrThread;
    private final Bus stdinBus;

    ProcessEntry(Process process, Thread exitThread, Thread stdinThread, Thread stdoutThread, Thread stderrThread, Bus stdinBus,
            int id, Bus responseBus) {
        Validate.notNull(responseBus);
        Validate.notNull(process);
        Validate.notNull(exitThread);
        Validate.notNull(stdinThread);
        Validate.notNull(stdoutThread);
        Validate.notNull(stderrThread);
        Validate.notNull(stdinBus);
        this.id = id;
        this.responseBus = responseBus;
        this.process = process;
        this.exitThread = exitThread;
        this.stdinThread = stdinThread;
        this.stdoutThread = stdoutThread;
        this.stderrThread = stderrThread;
        this.stdinBus = stdinBus;
    }

    int getId() {
        return id;
    }

    Bus getResponseBus() {
        return responseBus;
    }

    Process getProcess() {
        return process;
    }

    Thread getExitThread() {
        return exitThread;
    }

    Thread getStdinThread() {
        return stdinThread;
    }

    Thread getStdoutThread() {
        return stdoutThread;
    }

    Thread getStderrThread() {
        return stderrThread;
    }

    Bus getStdinBus() {
        return stdinBus;
    }
}
