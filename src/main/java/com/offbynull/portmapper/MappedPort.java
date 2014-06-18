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
import org.apache.commons.lang3.Validate;

/**
 * Describes a mapped port.
 * @author Kasra Faghihi
 */
public final class MappedPort {

    private int internalPort;
    private int externalPort;
    private InetAddress externalAddress;
    private PortType portType;
    private long lifetime;

    /**
     * Constructs a {@link MappedPort} object.
     * @param internalPort internal port
     * @param externalPort external port
     * @param externalAddress external address
     * @param portType port type
     * @param duration mapping lifetime
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is non-positive, or if {@code internalPort > 65535 || externalPort > 65535}
     */
    public MappedPort(int internalPort, int externalPort, InetAddress externalAddress, PortType portType, long duration) {
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1, 65535, externalPort);
        Validate.notNull(externalAddress);
        Validate.notNull(portType);
        Validate.inclusiveBetween(0L, Long.MAX_VALUE, duration);

        this.internalPort = internalPort;
        this.externalPort = externalPort;
        this.externalAddress = externalAddress;
        this.portType = portType;
        this.lifetime = duration;
    }

    /**
     * Get internal port.
     * @return internal port
     */
    public int getInternalPort() {
        return internalPort;
    }

    /**
     * Get external port.
     * @return external port
     */
    public int getExternalPort() {
        return externalPort;
    }

    /**
     * Get external address.
     * @return external address
     */
    public InetAddress getExternalAddress() {
        return externalAddress;
    }
    
    /**
     * Get port type.
     * @return port type
     */
    public PortType getPortType() {
        return portType;
    }

    /**
     * Get mapping lifetime.
     * @return mapping lifetime
     */
    public long getLifetime() {
        return lifetime;
    }

    @Override
    public String toString() {
        return "MappedPort{" + "internalPort=" + internalPort + ", externalPort=" + externalPort + ", externalAddress=" + externalAddress 
                + ", portType=" + portType + ", lifetime=" + lifetime + '}';
    }
    
}
