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
package com.offbynull.portmapper.pcp;

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortType;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

final class PcpMappedPort implements MappedPort {
    
    private byte[] nonce;
    private int internalPort;
    private int externalPort;
    private InetAddress externalAddress;
    private PortType portType;
    private long lifetime;

    PcpMappedPort(byte[] nonce, int internalPort, int externalPort, InetAddress externalAddress, PortType portType, long duration) {
        Validate.notNull(nonce);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1, 65535, externalPort);
        Validate.notNull(externalAddress);
        Validate.notNull(portType);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, duration);
        Validate.isTrue(nonce.length == 12);
        this.nonce = Arrays.copyOf(nonce, nonce.length);
        this.internalPort = internalPort;
        this.externalPort = externalPort;
        this.externalAddress = externalAddress;
        this.portType = portType;
        this.lifetime = duration;
    }

    byte[] getNonce() {
        return Arrays.copyOf(nonce, nonce.length);
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
}
