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

import java.util.Collections;

/**
 * Represents a UPnP GetExternalIPAddress request.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class GetExternalIpAddressUpnpIgdRequest extends UpnpIgdSoapRequest {
    
    /**
     * Constructs a {@link GetExternalIpAddressUpnpIgdRequest} object.
     * @param host device host
     * @param controlLocation control location
     * @param serviceType service type
     * @throws NullPointerException if any argument other than {@code remoteHost} is {@code null}
     */
    public GetExternalIpAddressUpnpIgdRequest(String host, String controlLocation, String serviceType) {
        super(host, controlLocation, serviceType, "GetExternalIPAddress", Collections.<String, String>emptyMap());
    }
}
