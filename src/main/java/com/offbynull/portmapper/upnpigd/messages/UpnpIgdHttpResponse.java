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

import com.offbynull.portmapper.common.TextUtils;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP-IGD response. Note that these messages aren't bound to any specific protocol. Some will be sent over UDP broadcast and
 * others will be sent via TCP (HTTP).
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdHttpResponse {

    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String TERMINATOR = "\r\n";
    private static final String HEADER_SPLIT_POINT = TERMINATOR + TERMINATOR;

    private final Map<String, String> headers;
    private final int responseCode;
    private final String content;

    UpnpIgdHttpResponse(int responseCode, Map<String, String> headers, String content) {
        Validate.notNull(headers);
        Validate.noNullElements(headers.keySet());
        Validate.noNullElements(headers.values());
        Validate.isTrue(responseCode >= 0);
//        Validate.notNull(content); // content may be null

        // content len calculated on dump
        for (String header : headers.keySet()) {
            if (header.equalsIgnoreCase("Content-Length")) {
                throw new IllegalArgumentException();
            }
        }

        this.responseCode = responseCode;
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

        String respStr = tokenizer.nextToken();
        respStr = TextUtils.collapseWhitespace(respStr).trim(); // get resp string, collapse whitespace for fault tolerance

        String[] splitResp = StringUtils.split(respStr, ' ');
        Validate.isTrue(splitResp.length >= 2); // ignore stuff afterwards if any (reason text)? -- trying to be fault tolerant
        Validate.isTrue(HTTP_VERSION.equalsIgnoreCase(splitResp[0])); // case insensitive for fault tolerance
        responseCode = Integer.parseInt(splitResp[1]); // throws nfe, but nfe extends illegalargexc so this is okay
        
        Validate.isTrue(responseCode >= 0);


        Map<String, String> headers = new HashMap<>();
        while (tokenizer.hasMoreTokens()) {
            String headerLine = tokenizer.nextToken().trim(); // trim to be fault tolerant, in case header has extra spaces
            if (headerLine.isEmpty()) {
                break;
            }

            String[] splitLine = StringUtils.split(headerLine, ":", 1);
            if (splitLine.length != 2) {
                continue; // skip line if no : found
            }

            String key = splitLine[0].trim();
            String value = splitLine[1].trim();
            
            if (key.equalsIgnoreCase("Content-Length")) { // ignore, calculated on dump
                continue;
            }

            headers.put(key, value);
        }

        this.headers = Collections.unmodifiableMap(headers);
        this.content = contentStr;
    }
    
    final boolean isResponseSuccessful() {
        return responseCode / 100 == 2; // is 2xx code?
    }

    final void validateResponseCode() {
        Validate.isTrue(isResponseSuccessful(), "Bad response code: %d", responseCode);
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
}
