/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

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
