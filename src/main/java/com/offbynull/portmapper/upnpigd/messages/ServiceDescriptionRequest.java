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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP service description request.
 * @author Kasra Faghihi
 */
public final class ServiceDescriptionRequest extends UpnpIgdHttpRequest {

    /**
     * Constructs a {@link ServiceDescriptionRequest} object.
     * @param host device host
     * @param scpdLocation SCPD location (found during discovery)
     */
    public ServiceDescriptionRequest(String host, String scpdLocation) {
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
}
