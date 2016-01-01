/*
 * Copyright (c) 2013-2015, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.upnpigd.messages;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP GetExternalIPAddress response.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class GetExternalIpAddressUpnpIgdResponse extends UpnpIgdSoapResponse {

    /**
     * Constructs a {@link GetExternalIpAddressUpnpIgdResponse} object.
     * @param buffer buffer containing response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} was malformed
     */
    public GetExternalIpAddressUpnpIgdResponse(byte[] buffer) {
        super("GetExternalIPAddressResponse", Collections.<String>emptySet(), buffer);
    }
    
    /**
     * Get external IP address.
     * @return external IP address -- <b>should</b> be IPv4
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public InetAddress getIpAddress() {
        String extIpStr = getArgumentIgnoreCase("NewExternalIPAddress");
        Validate.validState(extIpStr != null);
        try {
            return InetAddress.getByName(extIpStr);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }
}
