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
 * Represents a UPnP AddPortMapping request.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class AddPortMappingUpnpIgdRequest extends UpnpIgdSoapRequest {
    
    /**
     * Constructs a {@link AddPortMappingUpnpIgdRequest} object.
     * @param host device host
     * @param controlLocation control location
     * @param serviceType service type
     * @param remoteHost remote address ({@code null} means wildcard) -- <b>should</b> be IPv4
     * @param externalPort external port ({@code 0} means wildcard)
     * @param protocol protocol to target for port mapping (TCP/UDP)
     * @param internalPort internal port
     * @param internalClient internal address -- <b>should</b> be IPv4
     * @param enabled port mapping enabled
     * @param description port mapping description
     * @param leaseDuration lease duration
     * @throws NullPointerException if any argument other than {@code remoteHost} is {@code null}
     * @throws IllegalArgumentException if {@code 0 > externalPort > 65535 || 1 > internalPort > 65535 || 0L > leaseDuration > 0xFFFFFFFFL}
     */
    // CHECKSTYLE:OFF:ParameterNumber
    public AddPortMappingUpnpIgdRequest(String host, String controlLocation, String serviceType,
            InetAddress remoteHost,
            int externalPort,
            PortType protocol,
            int internalPort,
            InetAddress internalClient,
            boolean enabled,
            String description,
            long leaseDuration) {
    // CHECKSTYLE:ON
        super(host, controlLocation, serviceType, "AddPortMapping",
                generateArguments(remoteHost, externalPort, protocol, internalPort, internalClient, enabled, description, leaseDuration));
    }
    
    private static Map<String, String> generateArguments(
            InetAddress remoteHost, // must be IPv4 address (don't bother checking) -- null means wildcard ("")
            int externalPort, // 0 to 65535 -- 0 means wildcard 
            PortType protocol, // must be either "TCP" or "UDP"
            int internalPort, //  1 to 65535
            InetAddress internalClient, // must be IPv4 address of interface accessing server (don't bother checking)
            boolean enabled,
            String description, // if null then set to empty string
            long leaseDuration) { // 0 to max 0xFFFFFFFF
        
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
        
        Validate.inclusiveBetween(1, 65535, internalPort);
        ret.put("NewInternalPort", "" + internalPort);
        
        Validate.notNull(internalClient);
        ret.put("NewInternalClient", internalClient.getHostAddress());
        
        ret.put("NewEnabled", enabled ? "1" : "0");
        
        Validate.notNull(description);
        ret.put("NewPortMappingDescription", description);
        
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, leaseDuration);
        ret.put("NewLeaseDuration", "" + leaseDuration);
        
        return ret;
    }

    @Override
    public String toString() {
        return "AddPortMappingUpnpIgdRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
