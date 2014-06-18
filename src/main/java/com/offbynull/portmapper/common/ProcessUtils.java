/*
 * Copyright (c) 2013-2014, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.common;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * Utility class to help with {@link Process}s.
 * @author Kasra Faghihi
 */
public final class ProcessUtils {
    private ProcessUtils() {
        //
    }
    
    /**
     * Run a process and dump the stdout stream to a string.
     * @param timeout maximum amount of time the process can take to run
     * @param command command
     * @param args arguments
     * @return stdout from the process dumped to a string
     * @throws IOException if the process encounters an error
     * @throws NullPointerException if any arguments are {@code null} or contains {@code null}
     * @throws IllegalArgumentException any numeric argument is negative
     */
    public static String runProcessAndDumpOutput(long timeout, String command, String ... args) throws IOException {
        Validate.notNull(command);
        Validate.noNullElements(args);
        Validate.inclusiveBetween(0L, Long.MAX_VALUE, timeout);
        
        String[] pbCmd = new String[args.length + 1];
        pbCmd[0] = command;
        System.arraycopy(args, 0, pbCmd, 1, args.length);
        
        ProcessBuilder builder = new ProcessBuilder(pbCmd);
        
        final AtomicBoolean failedFlag = new AtomicBoolean();
        
        Timer timer = new Timer("Process timeout timer", true);
        Process proc = null;
        try {
            proc = builder.start();
            
            final Process finalProc = proc;
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    failedFlag.set(true);
                    finalProc.destroy();
                }
            }, timeout);
            
            String ret = IOUtils.toString(proc.getInputStream());
            if (failedFlag.get()) {
                throw new IOException("Process failed");
            }
            
            return ret;
        } finally {
            if (proc != null) {
                proc.destroy();
            }
            timer.cancel();
            timer.purge();
        }
    } 
}
