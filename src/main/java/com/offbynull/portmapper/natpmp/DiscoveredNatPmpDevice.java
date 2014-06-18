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
package com.offbynull.portmapper.natpmp;

import java.net.InetAddress;
import java.util.Objects;

/**
 * A NAT-PMP device discovered through {@link NatPmpDiscovery}.
 * @author Kasra Faghihi
 */
public final class DiscoveredNatPmpDevice {
    private InetAddress localAddress;
    private InetAddress gatewayAddress;

    DiscoveredNatPmpDevice(InetAddress localAddress, InetAddress gatewaAddress) {
        this.localAddress = localAddress;
        this.gatewayAddress = gatewaAddress;
    }

    /**
     * Get the local address on this machine that routes to the discovered NAT-PMP gateway.
     * @return local address on this machine that routes to the discovered NAT-PMP gateway
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Get the discovered NAT-PMP gateway address.
     * @return discovered NAT-PMP gateway address
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
        final DiscoveredNatPmpDevice other = (DiscoveredNatPmpDevice) obj;
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
        return "DiscoveredNatPmpDevice{" + "localAddress=" + localAddress + ", gatewayAddress=" + gatewayAddress + '}';
    }
    
}
