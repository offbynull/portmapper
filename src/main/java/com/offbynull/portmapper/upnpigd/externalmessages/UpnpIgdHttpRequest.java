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

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP-IGD request. Note that these messages aren't bound to any specific protocol. Some will be sent over UDP broadcast and
 * others will be sent via TCP (HTTP).
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdHttpRequest {

    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String TERMINATOR = "\r\n";
//    private static final String HEADER_SPLIT_POINT = TERMINATOR + TERMINATOR;

    private final String method;
    private final String location;
    private final Map<String, String> headers;
    private final String content;

    UpnpIgdHttpRequest(String method, String location, Map<String, String> headers, String content) {
        Validate.notNull(method);
        Validate.notNull(location);
        Validate.notNull(headers);
        Validate.noNullElements(headers.keySet());
        Validate.noNullElements(headers.values());
//        Validate.notNull(content); // content may be null

        // content len calculated on dump
        for (String header : headers.keySet()) {
            if (header.equalsIgnoreCase("Content-Length")) {
                throw new IllegalArgumentException();
            }
        }

        this.method = method;
        this.location = location;
        this.headers = new LinkedHashMap<>(headers);
        this.content = content;
    }

    /**
     * Dump out the UPnP-IGD request as a buffer.
     * @return UPnP-IGD packet/buffer
     */
    public final byte[] dump() {
        StringBuilder sb = new StringBuilder();

        sb.append(method).append(' ').append(location).append(' ').append(HTTP_VERSION).append(TERMINATOR);
        for (Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(TERMINATOR);
        }
        
        if (content != null) {
            byte[] contentBytes = content.getBytes(Charset.forName("US-ASCII"));
         
            sb.append("Content-Length: ").append(contentBytes.length).append(TERMINATOR);
            sb.append(TERMINATOR); // split
            byte[] headerBytes = sb.toString().getBytes(Charset.forName("US-ASCII"));
            
            byte[] finalBytes = new byte[contentBytes.length + headerBytes.length];
            System.arraycopy(headerBytes, 0, finalBytes, 0, headerBytes.length);
            System.arraycopy(contentBytes, 0, finalBytes, headerBytes.length, contentBytes.length);
            
            return finalBytes;
        } else {
            sb.append(TERMINATOR); // split
            return sb.toString().getBytes(Charset.forName("US-ASCII"));
        }
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.method);
        hash = 29 * hash + Objects.hashCode(this.location);
        hash = 29 * hash + Objects.hashCode(this.headers);
        hash = 29 * hash + Objects.hashCode(this.content);
        return hash;
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
        final UpnpIgdHttpRequest other = (UpnpIgdHttpRequest) obj;
        if (!Objects.equals(this.method, other.method)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        if (!Objects.equals(this.headers, other.headers)) {
            return false;
        }
        return true;
    }
    
}
