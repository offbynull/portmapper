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

import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.io.process.internalmessages.ReadProcessNotification;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.lang3.Validate;

final class ProcessReaderRunnable implements Runnable {

    private int id;
    private final InputStream inputStream;
    private final Bus directOutputBus;

    public ProcessReaderRunnable(int id, InputStream inputStream, Bus directOutputBus) {
        Validate.notNull(inputStream);
        Validate.notNull(directOutputBus);
        
        this.id = id;
        this.inputStream = inputStream;
        this.directOutputBus = directOutputBus;
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[8192];
        try {
            while (true) {
                int count = inputStream.read(buffer);
                if (count == -1) {
                    break;
                }
                
                directOutputBus.send(new ReadProcessNotification(id, Arrays.copyOf(buffer, count)));
            }
        } catch (IOException ioe) {
            // do nothing
        } finally {
            
        }
    }
    
}
