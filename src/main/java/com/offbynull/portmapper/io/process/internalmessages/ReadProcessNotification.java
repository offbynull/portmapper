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

import java.util.Arrays;
import org.apache.commons.lang3.Validate;

/**
 * Process has received data.
 * @author Kasra Faghihi
 */
public final class ReadProcessNotification extends IdentifiableProcessNotification {
    private byte[] data;
    private ReadType readType;

    /**
     * Constructs a {@link ReadProcessNotification} object.
     * @param id id of process
     * @param data received data
     * @param readType stream which received the data (STDOUT or STDERR)
     * @throws NullPointerException if any argument is {@code null}
     */
    public ReadProcessNotification(int id, byte[] data, ReadType readType) {
        super(id);
        Validate.notNull(data);
        Validate.notNull(readType);
        this.data = Arrays.copyOf(data, data.length);
        this.readType = readType;
    }

    /**
     * Get data.
     * @return data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Get stream which received data.
     * @return stream which received data
     */
    public ReadType getReadType() {
        return readType;
    }

    @Override
    public String toString() {
        return "ReadProcessNotification{super=" + super.toString() + "data=" + Arrays.toString(data) + ", readType=" + readType + '}';
    }

}
