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
package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP-IGD response. Note that these messages aren't bound to any specific protocol. Some will be sent over UDP broadcast and
 * others will be sent via TCP (HTTP).
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdHttpResponse {

    private static final String TERMINATOR = "\r\n";
    private static final String HEADER_SPLIT_POINT = TERMINATOR + TERMINATOR;

    private final Map<String, String> headers;
    private final String content;

    UpnpIgdHttpResponse(Map<String, String> headers, String content) {
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

        this.headers = new HashMap<>(headers);
        this.content = content;
    }

    UpnpIgdHttpResponse(byte[] buffer) {
        Validate.notNull(buffer);

        // Convert buffer to string
        String bufferStr = new String(buffer, Charset.forName("US-ASCII"));

        // Split buffer to header and content
        int splitIdx = bufferStr.indexOf(HEADER_SPLIT_POINT);
        String headersStr;
        String contentStr;
        if (splitIdx == -1) {
            // No content, so just grab headers and say we don't have content? -- trying to be fault tolerant here 
            headersStr = bufferStr;
            contentStr = null;
        } else {
            headersStr = bufferStr.substring(0, splitIdx);
            contentStr = bufferStr.substring(splitIdx + HEADER_SPLIT_POINT.length());
        }

        // Parse resp and headers
        StringTokenizer tokenizer = new StringTokenizer(headersStr, TERMINATOR);

        // The following block has been commented out because of issue #24. This check serves no real purpose -- it is against the idea of
        // being fault tolerant. Sometimes the router may give back a bad response code or a different HTTP version number or may respond to
        // an SSDP probe with an SSDP notify response (NOTIFY * HTTP/1.1) rather than a standard response (HTTP/1.1 200 OK).
        //
        // Instead, we're going to assume that no response header came in and move directly to parsing headers. The header parsing will skip
        // over the response string (assuming it doesn't contain a colon), or it'll incorrectly include it in the list of header (which
        // should be more or less benign).
//        String respStr = tokenizer.nextToken();
//        respStr = TextUtils.collapseWhitespace(respStr).trim(); // get resp string, collapse whitespace for fault tolerance
//
//        String[] splitResp = StringUtils.split(respStr, ' ');
//        Validate.isTrue(splitResp.length >= 2); // ignore stuff afterwards if any (reason text)? -- trying to be fault tolerant
//        Validate.isTrue(HTTP_VERSION.equalsIgnoreCase(splitResp[0])); // case insensitive for fault tolerance
//        responseCode = Integer.parseInt(splitResp[1]); // throws nfe, but nfe extends illegalargexc so this is okay
//        
//        Validate.isTrue(responseCode >= 0);

        Map<String, String> headers = new HashMap<>();
        while (tokenizer.hasMoreTokens()) {
            String headerLine = tokenizer.nextToken().trim(); // trim to be fault tolerant, in case header has extra spaces
            if (headerLine.isEmpty()) {
                break;
            }

            String[] splitLine = StringUtils.split(headerLine, ":", 2);
            if (splitLine.length != 2) {
                continue; // skip line if no : found
            }

            String key = splitLine[0].trim();
            String value = splitLine[1].trim();

            headers.put(key, value);
        }

        this.headers = Collections.unmodifiableMap(headers);
        this.content = contentStr;
    }
    
    final String getHeaderIgnoreCase(String key) {
        for (Entry<String, String> header : headers.entrySet()) {
            if (header.getKey().equalsIgnoreCase(key)) {
                return header.getValue();
            }
        }
        return null;
    }

    final String getContent() {
        return content;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public String toString() {
        return "UpnpIgdHttpResponse{" + "headers=" + headers + ", content=" + content + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final UpnpIgdHttpResponse other = (UpnpIgdHttpResponse) obj;
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        if (!Objects.equals(this.headers, other.headers)) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
