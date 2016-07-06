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

import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP AddPinhole response.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class AddPinholeUpnpIgdResponse extends UpnpIgdSoapResponse {

    /**
     * Constructs a {@link AddPinholeUpnpIgdResponse} object.
     * @param buffer buffer containing response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} was malformed
     */
    public AddPinholeUpnpIgdResponse(byte[] buffer) {
        super("AddPinholeResponse", new HashSet<>(Arrays.asList("UniqueID")), buffer);
    }
    
    /**
     * Get unique ID. This should be a number (0 to 65535), but it's returned as a string because it never gets manipulated (only passed
     * around).
     * @return unique ID of the pinhole mapping
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public String getUniqueId() {
        String uniqueIdStr = getArgumentIgnoreCase("UniqueID");
        Validate.validState(uniqueIdStr != null);
        return uniqueIdStr;
    }

    @Override
    public String toString() {
        return "AddPinholeUpnpIgdResponse{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
