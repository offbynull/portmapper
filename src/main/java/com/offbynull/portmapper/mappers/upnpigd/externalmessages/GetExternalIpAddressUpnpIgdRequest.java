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
     * @throws NullPointerException if any argument is {@code null}
     */
    public GetExternalIpAddressUpnpIgdRequest(String host, String controlLocation, String serviceType) {
        super(host, controlLocation, serviceType, "GetExternalIPAddress", Collections.<String, String>emptyMap());
    }

    @Override
    public String toString() {
        return "GetExternalIpAddressUpnpIgdRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
