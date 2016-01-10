package com.offbynull.portmapper.io.process;

import com.offbynull.portmapper.common.Bus;
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

    public ProcessEntry(Process process, Thread exitThread, Thread stdinThread, Thread stdoutThread, Thread stderrThread, Bus stdinBus,
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

    public int getId() {
        return id;
    }

    public Bus getResponseBus() {
        return responseBus;
    }

    public Process getProcess() {
        return process;
    }

    public Thread getExitThread() {
        return exitThread;
    }

    public Thread getStdinThread() {
        return stdinThread;
    }

    public Thread getStdoutThread() {
        return stdoutThread;
    }

    public Thread getStderrThread() {
        return stderrThread;
    }

    public Bus getStdinBus() {
        return stdinBus;
    }
}
