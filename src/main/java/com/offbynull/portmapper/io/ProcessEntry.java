package com.offbynull.portmapper.io;

import com.offbynull.portmapper.common.Bus;
import org.apache.commons.lang3.Validate;

final class ProcessEntry extends IoEntry {
    
    private final Process process;
    private final Thread exitThread;
    private final Thread stdinThread;
    private final Thread stdoutThread;
    private final Thread stderrThread;

    public ProcessEntry(Process process, Thread exitThread, Thread stdinThread, Thread stdoutThread, Thread stderrThread, int id,
            Bus responseBus) {
        super(id, responseBus);
        
        Validate.notNull(process);
        Validate.notNull(stdinThread);
        Validate.notNull(stdoutThread);
        Validate.notNull(stderrThread);
        this.process = process;
        this.exitThread = exitThread;
        this.stdinThread = stdinThread;
        this.stdoutThread = stdoutThread;
        this.stderrThread = stderrThread;
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
}
