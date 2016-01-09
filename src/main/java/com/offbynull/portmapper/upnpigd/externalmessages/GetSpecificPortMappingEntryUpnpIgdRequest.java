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
package com.offbynull.portmapper.upnpigd.externalmessages;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP GetSpecificPortMappingEntry request.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class GetSpecificPortMappingEntryUpnpIgdRequest extends UpnpIgdSoapRequest {
    
    /**
     * Constructs a {@link GetSpecificPortMappingEntryUpnpIgdRequest} object.
     * @param host device host
     * @param controlLocation control location
     * @param serviceType service type
     * @param remoteHost remote address ({@code null} means wildcard) -- <b>should</b> be IPv4
     * @param externalPort external port ({@code 0} means wildcard)
     * @param protocol protocol to target for port mapping (TCP/UDP)
     * @throws NullPointerException if any argument other than {@code remoteHost} is {@code null}
     * @throws IllegalArgumentException if {@code 0 > externalPort > 65535}
     */
    public GetSpecificPortMappingEntryUpnpIgdRequest(String host, String controlLocation, String serviceType,
            InetAddress remoteHost,
            int externalPort,
            Protocol protocol) {
        super(host, controlLocation, serviceType, "GetSpecificPortMappingEntry",
                generateArguments(remoteHost, externalPort, protocol));
    }
    
    private static Map<String, String> generateArguments(
            InetAddress remoteHost,
            int externalPort,
            Protocol protocol) {
        
        Map<String, String> ret = new LinkedHashMap<>();
        
        if (remoteHost == null) {
            ret.put("NewRemoteHost", "");
        } else {
            ret.put("NewRemoteHost", remoteHost.getHostAddress());
        }
        
        Validate.inclusiveBetween(0, 65535, externalPort);
        ret.put("NewExternalPort", "" + externalPort);
        
        Validate.notNull(protocol);
        ret.put("NewProtocol", protocol.toString());
        
        return ret;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
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
        final GetSpecificPortMappingEntryUpnpIgdRequest other = (GetSpecificPortMappingEntryUpnpIgdRequest) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
}
