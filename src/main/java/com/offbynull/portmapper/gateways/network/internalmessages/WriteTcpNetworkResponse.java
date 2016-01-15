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
package com.offbynull.portmapper.gateways.network.internalmessages;

import org.apache.commons.lang3.Validate;

/**
 * Sent data through a TCP socket. Successful response to {@link WriteTcpNetworkRequest}.
 * @author Kasra Faghihi
 */
public final class WriteTcpNetworkResponse extends IdentifiableNetworkResponse {
    private int amountWritten;

    /**
     * Constructs a {@link WriteTcpNetworkResponse} object.
     * @param id id of socket
     * @param amountWritten amount of data written out (in bytes)
     */
    public WriteTcpNetworkResponse(int id, int amountWritten) {
        super(id);
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    /**
     * Amount of data written out.
     * @return bytes written out
     */
    public int getAmountWritten() {
        return amountWritten;
    }

    @Override
    public String toString() {
        return "WriteTcpNetworkResponse{super=" + super.toString() + "amountWritten=" + amountWritten + '}';
    }
    
}
