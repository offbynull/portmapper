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
 * Represents a UPnP UpdatePinhole request.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class UpdatePinholeUpnpIgdRequest extends UpnpIgdSoapRequest {
    
    /**
     * Constructs a {@link UpdatePinholeUpnpIgdRequest} object.
     * @param host device host
     * @param controlLocation control location
     * @param serviceType service type
     * @param uniqueId uniqueId of the mapping (should be a number between 0 to 65535 -- but not checked by this method)
     * @param leaseDuration lease duration
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code 1L > leaseDuration > 0xFFFFFFFFL}
     */
    public UpdatePinholeUpnpIgdRequest(String host, String controlLocation, String serviceType, String uniqueId, long leaseDuration) {
        super(host, controlLocation, serviceType, "UpdatePinhole", generateArguments(uniqueId, leaseDuration));
    }
    
    private static Map<String, String> generateArguments(
            String uniqueId, // should be a number between 0 to 65535 -- but not checked
            long leaseDuration) { // 1 to max 0xFFFFFFFF
        
        Map<String, String> ret = new LinkedHashMap<>();
        
        Validate.notNull(uniqueId);
        ret.put("UniqueID", uniqueId);
        
        Validate.inclusiveBetween(1L, 0xFFFFFFFFL, leaseDuration);
        ret.put("NewLeaseTime", "" + leaseDuration);
        
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
        final UpdatePinholeUpnpIgdRequest other = (UpdatePinholeUpnpIgdRequest) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
}
