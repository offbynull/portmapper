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
package com.offbynull.portmapper;

import java.net.InetAddress;

/**
 * Represents a mapped port.
 * @author Kasra Faghihi
 */
public interface MappedPort {

    /**
     * Get internal port.
     * @return internal port
     */
    int getInternalPort();

    /**
     * Get external port.
     * @return external port
     */
    int getExternalPort();

    /**
     * Get external address (optional).
     * @return external address ({@code null} if underlying protocol doesn't support getting the external address)
     */
    InetAddress getExternalAddress();
    
    /**
     * Get port type.
     * @return port type
     */
    PortType getPortType();

    /**
     * Get mapping lifetime.
     * @return mapping lifetime
     */
    long getLifetime();
}
