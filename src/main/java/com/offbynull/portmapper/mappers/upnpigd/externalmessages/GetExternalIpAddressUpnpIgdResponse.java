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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP GetExternalIPAddress response.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class GetExternalIpAddressUpnpIgdResponse extends UpnpIgdSoapResponse {

    /**
     * Constructs a {@link GetExternalIpAddressUpnpIgdResponse} object.
     * @param buffer buffer containing response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} was malformed
     */
    public GetExternalIpAddressUpnpIgdResponse(byte[] buffer) {
        super("GetExternalIPAddressResponse", new HashSet<>(Arrays.asList("NewExternalIPAddress")), buffer);
    }
    
    /**
     * Get external IP address.
     * @return external IP address -- <b>should</b> be IPv4
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public InetAddress getIpAddress() {
        String extIpStr = getArgumentIgnoreCase("NewExternalIPAddress");
        Validate.validState(extIpStr != null);
        try {
            return InetAddress.getByName(extIpStr);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return "GetExternalIpAddressUpnpIgdResponse{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
