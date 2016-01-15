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

/**
 * Created a new socket ID. Response to {@link GetNextSocketIdRequest}.
 * @author Kasra Faghihi
 */
public final class GetNextIdNetworkResponse extends IdentifiableNetworkResponse {
   
    /**
     * Constructs a {@link GetNextSocketIdResponse} object.
     * @param id id to use for for new socket
     */
    public GetNextIdNetworkResponse(int id) {
        super(id);
    }
    
}
