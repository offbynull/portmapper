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

import com.offbynull.portmapper.mapper.PortType;
import java.net.InetAddress;
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
            PortType protocol) {
        super(host, controlLocation, serviceType, "GetSpecificPortMappingEntry",
                generateArguments(remoteHost, externalPort, protocol));
    }
    
    private static Map<String, String> generateArguments(
            InetAddress remoteHost,
            int externalPort,
            PortType protocol) {
        
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
    public String toString() {
        return "GetSpecificPortMappingEntryUpnpIgdRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
