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
package com.offbynull.portmapper.io.process.internalmessages;

/**
 * Notification message associated with a process.
 * @author Kasra Faghihi
 */
public abstract class IdentifiableProcessNotification implements ProcessNotification {
    
    private int id;

    IdentifiableProcessNotification(int id) {
        this.id = id;
    }

    /**
     * Get process id.
     * @return id of process
     */
    public final int getId() {
        return id;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public String toString() {
        return "IdentifiableProcessNotification{" + "id=" + id + '}';
    }
    // CHECKSTYLE:ON:DesignForExtension
}
