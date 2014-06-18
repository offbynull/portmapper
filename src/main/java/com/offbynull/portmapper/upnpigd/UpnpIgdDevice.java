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
package com.offbynull.portmapper.upnpigd;

import java.net.InetAddress;
import java.net.URI;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPNP-IGD router.
 * @author Kasra Faghihi
 */
public final class UpnpIgdDevice {
    private InetAddress selfAddress;
    private InetAddress gatewayAddress;
    private String name;
    private URI url;

    /**
     * Constructs a UPNP-IGD router.
     * @param selfAddress address of this machine (on the interface that routes to gateway address)
     * @param gatewayAddress router address
     * @param name router name
     * @param url root XML URL
     * @throws NullPointerException if any argument other than {@code name} is {@code null}
     */
    public UpnpIgdDevice(InetAddress selfAddress, InetAddress gatewayAddress, String name, URI url) {
        Validate.notNull(selfAddress);
        Validate.notNull(gatewayAddress);
        Validate.notNull(url);
        this.selfAddress = selfAddress;
        this.gatewayAddress = gatewayAddress;
        this.name = name;
        this.url = url;
    }

    /**
     * Get self address.
     * @return self address
     */
    public InetAddress getSelfAddress() {
        return selfAddress;
    }

    /**
     * Get router address.
     * @return router address
     */
    public InetAddress getGatewayAddress() {
        return gatewayAddress;
    }

    /**
     * Get router name.
     * @return router name
     */
    public String getName() {
        return name;
    }

    /**
     * Get root XML URL.
     * @return root XML URL
     */
    public URI getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.selfAddress);
        hash = 17 * hash + Objects.hashCode(this.gatewayAddress);
//        hash = 17 * hash + Objects.hashCode(this.name);
//        hash = 17 * hash + Objects.hashCode(this.url);
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
        final UpnpIgdDevice other = (UpnpIgdDevice) obj;
        if (!Objects.equals(this.selfAddress, other.selfAddress)) {
            return false;
        }
        if (!Objects.equals(this.gatewayAddress, other.gatewayAddress)) {
            return false;
        }
//        if (!Objects.equals(this.name, other.name)) {
//            return false;
//        }
//        if (!Objects.equals(this.url, other.url)) {
//            return false;
//        }
        return true;
    }

    @Override
    public String toString() {
        return "UpnpIgdDevice{" + "selfAddress=" + selfAddress + ", gatewayAddress=" + gatewayAddress + ", name=" + name + ", url="
                + url + '}';
    }


}
