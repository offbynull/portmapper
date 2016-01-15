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
package com.offbynull.portmapper.gateways.process;

final class TerminatedMessage {
    private int id;
    private Integer exitCode;

    TerminatedMessage(int id, Integer exitCode) { // exitcode may be null
        this.id = id;
        this.exitCode = exitCode;
    }

    int getId() {
        return id;
    }

    Integer getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "TerminatedMessage{" + "id=" + id + ", exitCode=" + exitCode + '}';
    }

}
