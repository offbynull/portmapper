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
package com.offbynull.portmapper.pcp;

import java.net.InetAddress;
import java.util.Objects;

/**
 * A PCP device discovered through {@link PcpDiscovery}.
 * @author Kasra Faghihi
 */
public final class DiscoveredPcpDevice {
    private InetAddress localAddress;
    private InetAddress gatewayAddress;

    DiscoveredPcpDevice(InetAddress localAddress, InetAddress gatewaAddress) {
        this.localAddress = localAddress;
        this.gatewayAddress = gatewaAddress;
    }

    /**
     * Get the local address on this machine that routes to the discovered PCP gateway.
     * @return local address on this machine that routes to the discovered PCP gateway
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Get the discovered PCP gateway address.
     * @return discovered PCP gateway address
     */
    public InetAddress getGatewayAddress() {
        return gatewayAddress;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.localAddress);
        hash = 61 * hash + Objects.hashCode(this.gatewayAddress);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DiscoveredPcpDevice other = (DiscoveredPcpDevice) obj;
        if (!Objects.equals(this.localAddress, other.localAddress)) {
            return false;
        }
        if (!Objects.equals(this.gatewayAddress, other.gatewayAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DiscoveredPcpDevice{" + "localAddress=" + localAddress + ", gatewayAddress=" + gatewayAddress + '}';
    }
    
}
