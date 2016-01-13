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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP service description request.
 * <p>
 * Specifications are documented at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class ServiceDescriptionUpnpIgdRequest extends UpnpIgdHttpRequest {

    /**
     * Constructs a {@link ServiceDescriptionRequest} object.
     * @param host device host
     * @param scpdLocation SCPD location (found during discovery)
     */
    public ServiceDescriptionUpnpIgdRequest(String host, String scpdLocation) {
        super("GET", scpdLocation, generateHeaders(host), null);
    }
    
    private static Map<String, String> generateHeaders(String host) {
        Validate.notNull(host);
        
        Map<String, String> ret = new HashMap<>();
        
        // content-length is added by parent class
        ret.put("Host", host);
        ret.put("Connection", "Close");
        
        return ret;
    }

    @Override
    public String toString() {
        return "ServiceDescriptionUpnpIgdRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
