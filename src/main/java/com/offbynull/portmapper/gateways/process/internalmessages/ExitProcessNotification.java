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
package com.offbynull.portmapper.gateways.process.internalmessages;

/**
 * Process exited.
 * @author Kasra Faghihi
 */
public final class ExitProcessNotification extends IdentifiableProcessResponse {
    private final Integer exitCode;

    /**
     * Constructs a {@link ExitProcessNotification} object.
     * @param id id of process
     * @param exitCode exit code ({@code null} if not available)
     */
    public ExitProcessNotification(int id, Integer exitCode) {
        super(id);
        this.exitCode = exitCode;
    }

    /**
     * Get exit code of process.
     * @return exit code of process ({@code null} if not available)
     */
    public Integer getExitCode() { // null if not available
        return exitCode;
    }

    @Override
    public String toString() {
        return "ExitProcessNotification{super=" + super.toString() + "exitCode=" + exitCode + '}';
    }
    
}
