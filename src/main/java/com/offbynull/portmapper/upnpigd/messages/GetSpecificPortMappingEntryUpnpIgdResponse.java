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
package com.offbynull.portmapper.upnpigd.messages;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP GetSpecificPortMappingEntry response.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class GetSpecificPortMappingEntryUpnpIgdResponse extends UpnpIgdSoapResponse {
    
    /**
     * Constructs a {@link GetSpecificPortMappingEntryUpnpIgdResponse} object.
     * @param buffer buffer containing response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} was malformed
     */
    public GetSpecificPortMappingEntryUpnpIgdResponse(byte[] buffer) {
        super("GetSpecificPortMappingEntryResponse",
                new HashSet<>(Arrays.asList(
                        "NewInternalPort",
                        "NewInternalClient",
                        "NewEnabled",
                        "NewPortMappingDescription",
                        "NewLeaseDuration")),
                buffer);
    }

    /**
     * Get internal port.
     * @return internal port
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public int getInternalPort() {
        String internalPortStr = getArgumentIgnoreCase("NewInternalPort");
        Validate.isTrue(internalPortStr != null); // this tag is vital for identification
        int internalPort = Integer.parseInt(internalPortStr); // throws nfe, which is an iae
        Validate.inclusiveBetween(1, 65535, internalPort);
        
        return internalPort;
    }

    /**
     * Get internal client IP address.
     * @return internal client IP address
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public InetAddress getInternalClient() {
        String internalClientStr = getArgumentIgnoreCase("NewInternalClient");
        Validate.validState(internalClientStr != null);
        try {
            return InetAddress.getByName(internalClientStr);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e); // this tag is vital for identification
        }
    }

    /**
     * Get port mapping enabled flag ({@code false} means port mapping is disabled, while {@code true} means its enabled}.
     * @return port mapping enabled flag
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public boolean getEnabled() {
        String enabledStr = getArgumentIgnoreCase("NewEnabled");
        Validate.validState(enabledStr != null);
        if (enabledStr.equalsIgnoreCase("1")) {
            return true;
        } else if (enabledStr.equalsIgnoreCase("0")) {
            return false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get description.
     * @return description
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public String getDescription() {
        String description = getArgumentIgnoreCase("NewPortMappingDescription");
        Validate.validState(description != null);
        return description;
    }

    /**
     * Get lease duration.
     * @return lease duration
     * @throws IllegalStateException if was not found or could not be interpreted
     */
    public Long getLeaseDuration() {
        String leaseDurationStr = getArgumentIgnoreCase("NewLeaseDuration");
        Validate.validState(leaseDurationStr != null);
        long leaseDuration = Long.parseLong(leaseDurationStr); // throws nfe, which is an iae
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, leaseDuration);
        return leaseDuration;
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
        final GetSpecificPortMappingEntryUpnpIgdResponse other = (GetSpecificPortMappingEntryUpnpIgdResponse) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
}
