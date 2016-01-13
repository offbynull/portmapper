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
package com.offbynull.portmapper.io.network.internalmessages;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Got local IP addresses. Successful response to {@link GetLocalIpAddressesNetworkRequest}.
 * @author Kasra Faghihi
 */
public final class GetLocalIpAddressesNetworkResponse implements NetworkResponse {
    private Set<InetAddress> localAddresses;

    /**
     * Constructs a {@link GetLocalIpAddressesNetworkRequest} object.
     * @param localAddresses local addresses
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     */
    public GetLocalIpAddressesNetworkResponse(Set<InetAddress> localAddresses) {
        Validate.notNull(localAddresses);
        Validate.noNullElements(localAddresses);
        this.localAddresses = new HashSet<>(localAddresses);
    }

    /**
     * Get local addresses.
     * @return local addresses
     */
    public Set<InetAddress> getLocalAddresses() {
        return localAddresses;
    }

    @Override
    public String toString() {
        return "GetLocalIpAddressesNetworkResponse{" + "localAddresses=" + localAddresses + '}';
    }
    
}
