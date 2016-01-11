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

public final class ReadProcessNotification extends IdentifiableProcessNotification {
    private byte[] data;
    private ReadType readType;

    public ReadProcessNotification(int id, byte[] data, ReadType readType) {
        super(id);
        Validate.notNull(data);
        Validate.notNull(readType);
        this.data = Arrays.copyOf(data, data.length);
        this.readType = readType;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    public ReadType getReadType() {
        return readType;
    }

}