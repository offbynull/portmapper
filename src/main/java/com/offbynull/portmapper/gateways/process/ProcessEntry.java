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

import com.offbynull.portmapper.gateway.Bus;
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
