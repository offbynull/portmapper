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
package com.offbynull.portmapper.upnpigd;

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortType;
import java.net.InetAddress;
import org.apache.commons.lang3.Validate;

final class PortMapperMappedPort implements MappedPort {
    
    private int internalPort;
    private int externalPort;
    private InetAddress externalAddress;
    private PortType portType;
    private long lifetime;

    PortMapperMappedPort(int internalPort, int externalPort, InetAddress externalAddress, PortType portType, long duration) {
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1, 65535, externalPort);
        Validate.notNull(externalAddress);
        Validate.notNull(portType);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, duration);
        this.internalPort = internalPort;
        this.externalPort = externalPort;
        this.externalAddress = externalAddress;
        this.portType = portType;
        this.lifetime = duration;
    }

    @Override
    public int getInternalPort() {
        return internalPort;
    }

    @Override
    public int getExternalPort() {
        return externalPort;
    }

    @Override
    public InetAddress getExternalAddress() {
        return externalAddress;
    }

    @Override
    public PortType getPortType() {
        return portType;
    }

    @Override
    public long getLifetime() {
        return lifetime;
    }

    @Override
    public String toString() {
        return "PortMapperMappedPort{" + "internalPort=" + internalPort + ", externalPort=" + externalPort + ", externalAddress="
                + externalAddress + ", portType=" + portType + ", lifetime=" + lifetime + '}';
    }
}
