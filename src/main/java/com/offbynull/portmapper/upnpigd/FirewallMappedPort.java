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
import java.util.Objects;
import org.apache.commons.lang3.Validate;

final class FirewallMappedPort implements MappedPort {
    
    private String key;
    private int internalPort;
    private int externalPort;
    private InetAddress externalAddress;
    private PortType portType;
    private long lifetime;

    FirewallMappedPort(String key, int internalPort, int externalPort, InetAddress externalAddress, PortType portType, long duration) {
        Validate.notNull(key);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1, 65535, externalPort);
        Validate.notNull(externalAddress);
        Validate.notNull(portType);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, duration);
        this.key = key;
        this.internalPort = internalPort;
        this.externalPort = externalPort;
        this.externalAddress = externalAddress;
        this.portType = portType;
        this.lifetime = duration;
    }

    String getKey() {
        return key;
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
        return "FirewallMappedPort{" + "key=" + key + ", internalPort=" + internalPort + ", externalPort=" + externalPort
                + ", externalAddress=" + externalAddress + ", portType=" + portType + ", lifetime=" + lifetime + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.key);
        hash = 41 * hash + this.internalPort;
        hash = 41 * hash + this.externalPort;
        hash = 41 * hash + Objects.hashCode(this.externalAddress);
        hash = 41 * hash + Objects.hashCode(this.portType);
        hash = 41 * hash + (int) (this.lifetime ^ (this.lifetime >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FirewallMappedPort other = (FirewallMappedPort) obj;
        if (this.internalPort != other.internalPort) {
            return false;
        }
        if (this.externalPort != other.externalPort) {
            return false;
        }
        if (this.lifetime != other.lifetime) {
            return false;
        }
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.externalAddress, other.externalAddress)) {
            return false;
        }
        if (this.portType != other.portType) {
            return false;
        }
        return true;
    }

    
}
