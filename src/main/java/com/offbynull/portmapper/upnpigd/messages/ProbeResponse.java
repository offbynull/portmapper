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

/**
 * Represents a UPnP probe response.
 * @author Kasra Faghihi
 */
public final class ProbeResponse extends UpnpIgdHttpResponse {

    private static final String LOCATION_KEY = "HOST";
    private static final String SERVER_KEY = "SERVER";
    private static final String USN_KEY = "USN";
    private static final String ST_KEY = "ST";

    // http://quimby.gnus.org/internet-drafts/draft-cai-ssdp-v1-03.txt

//    // examples for Javadoc taken from http://www.upnp-hacks.org/upnp.html
//    /**
//     * Constructs a {@link ProbeResponse} object.
//     * @param location type of device to probe for (IPv4 or IPv6)
//     * @param server name of the device replying to the probe (can be {@code null} -- should be there but not required for identifying
//     * UPnP-IGD devices -- e.g. {@code "SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)"})
//     * @param usn unique service name of the device replying to the probe (can be {@code null} -- should be there but not required for
//     * identifying UPnP-IGD devices -- e.g. {@code "uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1"})
//     * @param serviceType service type of the device replying to the probe (can be {@code null} -- should be there but not required for
//     * identifying UPnP-IGD devices (action names are used instead) -- e.g. {@code "urn:schemas-upnp-org:service:WANPPPConnection:1"})
//     * @throws NullPointerException if {@code location} is {@code null}
//     */
//    public ProbeResponse(String location, String server, String usn, String serviceType) {
//        super(generateHeaders(location, server, usn, serviceType), null);
//    }
//
//    private static Map<String, String> generateHeaders(String location, String server, String usn, String serviceType) {
//        Validate.notNull(location);
//        
//        Map<String, String> ret = new HashMap<>();
//        ret.put(LOCATION_KEY, location);
//        if (server != null) {
//            ret.put(ST_KEY, server);
//        }
//        if (usn != null) {
//            ret.put(ST_KEY, usn);
//        }
//        if (serviceType != null) {
//            ret.put(ST_KEY, serviceType);
//        }
//        
//        return ret;
//    }

    /**
     * Constructs a {@link ProbeResponse} object by parsing a buffer.
     * @param buffer buffer containing probe request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if buffer is malformed (response is not 200 OK, or LOCATION header is missing)
     */
    public ProbeResponse(byte[] buffer) {
        super(buffer);
    }

    /**
     * Get location to access service.
     * @return location
     */
    public String getLocation() {
        return getHeaderIgnoreCase(LOCATION_KEY);
    }

    /**
     * Get server description.
     * @return server description (may be {@code null})
     */
    public String getServer() {
        return getHeaderIgnoreCase(SERVER_KEY);
    }

    /**
     * Get unique service identifier.
     * @return unique service identifier (may be {@code null})
     */
    public String getUsn() {
        return getHeaderIgnoreCase(USN_KEY);
    }

    /**
     * Get service type.
     * @return service type (may be {@code null})
     */
    public String getServiceType() {
        return getHeaderIgnoreCase(ST_KEY);
    }
}
