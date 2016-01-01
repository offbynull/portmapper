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
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP AddPinhole request.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class AddPinholeUpnpIgdRequest extends UpnpIgdSoapRequest {
    
    /**
     * Constructs a {@link AddPinholeUpnpIgdRequest} object.
     * @param host device host
     * @param controlLocation control location
     * @param serviceType service type
     * @param remoteHost remote address ({@code null} means wildcard) -- <b>should</b> be IPv6
     * @param remotePort external port ({@code 0} means wildcard)
     * @param internalClient internal address -- <b>should</b> be IPv4
     * @param internalPort internal port ({@code 0} means wildcard)
     * @param protocol protocol to target for port mapping (TCP/UDP) -- ({@code null} means wildcard) 
     * @param enabled port mapping enabled
     * @param leaseDuration lease duration
     * @throws NullPointerException if any argument other than {@code remoteHost} is {@code null}
     * @throws IllegalArgumentException if {@code 0 > externalPort > 65535 || 1 > internalPort > 65535 || 0L > leaseDuration > 0xFFFFFFFFL}
     */
    public AddPinholeUpnpIgdRequest(String host, String controlLocation, String serviceType,
            InetAddress remoteHost,
            int remotePort,
            InetAddress internalClient,
            int internalPort,
            Protocol protocol,
            boolean enabled,
            long leaseDuration) {
        super(host, controlLocation, serviceType, "AddPinhole",
                generateArguments(remoteHost, remotePort, protocol, internalPort, internalClient, enabled, leaseDuration));
    }
    
    private static Map<String, String> generateArguments(
            InetAddress remoteHost, // must be IPv6 address (don't bother checking) -- null means wildcard ("")
            int externalPort, // 0 to 65535 -- 0 means wildcard 
            Protocol protocol, // must be either "TCP" or "UDP" -- null means wildcard
            int internalPort, //  0 to 65535 -- 0 means wildcard
            InetAddress internalClient, // must be IPv6 address of interface accessing server (don't bother checking)
            boolean enabled,
            long leaseDuration) { // 0 to max 0xFFFFFFFF
        
        Map<String, String> ret = new LinkedHashMap<>();
        
        if (remoteHost == null) {
            ret.put("NewRemoteHost", "");
        } else {
            ret.put("NewRemoteHost", remoteHost.getHostAddress());
        }
        
        Validate.inclusiveBetween(0, 65535, externalPort);
        ret.put("NewExternalPort", "" + externalPort);
        
        ret.put("NewProtocol", (protocol == null ? "65535" : "" + protocol.getIana())); // 65535 is wildcard
        
        Validate.inclusiveBetween(0, 65535, internalPort);
        ret.put("NewInternalPort", "" + internalPort);
        
        Validate.notNull(internalClient);
        ret.put("NewInternalClient", internalClient.getHostAddress());
        
        ret.put("NewEnabled", enabled ? "1" : "0");
        
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, leaseDuration);
        ret.put("NewLeaseDuration", "" + leaseDuration);
        
        return ret;
    }
}
