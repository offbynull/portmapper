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

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP DeletePinhole request.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class DeletePinholeUpnpIgdRequest extends UpnpIgdSoapRequest {
    
    /**
     * Constructs a {@link DeletePinholeUpnpIgdRequest} object.
     * @param host device host
     * @param controlLocation control location
     * @param serviceType service type
     * @param uniqueId uniqueId of the mapping (should be a number between 0 to 65535 -- but not checked by this method)
     * @throws NullPointerException if any argument is {@code null}
     */
    public DeletePinholeUpnpIgdRequest(String host, String controlLocation, String serviceType, String uniqueId) {
        super(host, controlLocation, serviceType, "DeletePinhole", generateArguments(uniqueId));
    }
    
    private static Map<String, String> generateArguments(
            String uniqueId) { // should be a number between 0 to 65535 -- but not checked
        
        Map<String, String> ret = new LinkedHashMap<>();
        
        Validate.notNull(uniqueId);
        ret.put("UniqueID", uniqueId);
        
        return ret;
    }

    @Override
    public String toString() {
        return "DeletePinholeUpnpIgdRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
