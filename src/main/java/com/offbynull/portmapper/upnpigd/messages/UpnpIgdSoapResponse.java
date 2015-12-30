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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP device query request.
 *
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdSoapResponse extends UpnpIgdHttpResponse {

    private Map<String, String> arguments;
    
    UpnpIgdSoapResponse(Set<String> expectedArguments, byte[] buffer) {
        super(buffer);
        
        Validate.notNull(expectedArguments);
        Validate.noNullElements(expectedArguments);

        String content = getContent();
        
        // A really hacky way of finding the body block
        String bodyBlock = TextUtils.findFirstBlock(content, /*<soapprefix*/":Body>", /*</soapprefix:*/":Body>", true);

        // A really hacky way of finding the fault block
        String faultBlock = TextUtils.findFirstBlock(bodyBlock, /*<soapprefix*/":Fault>", /*</soapprefix:*/":Fault>", true);
        if (faultBlock != null) {
            String faultCode = TextUtils.findFirstBlock(content, "faultcode", "faultcode", true);
            String faultString = TextUtils.findFirstBlock(content, "faultstring", "faultstring", true);
            
            throw new IllegalArgumentException("Response contains fault (code: " + faultCode + " / message: " + faultString);
        }
        
        
        Map<String, String> args = new HashMap<>();
        for (String key : expectedArguments) {
            String value = TextUtils.findFirstBlock(content, key + ">", key + ">", true);
            if (value != null) {
                value = StringEscapeUtils.unescapeXml(value).trim();
                args.put(key, value);
            }
        }

        arguments = Collections.unmodifiableMap(args);
        
        //<?xml version="1.0"?>
        //
        //<soap:Envelope
        //xmlns:soap="http://www.w3.org/2003/05/soap-envelope/"
        //soap:encodingStyle="http://www.w3.org/2003/05/soap-encoding">
        //<soap:Body>
        //  <soap:Fault>
        //  ...
        //  </soap:Fault>
        //</soap:Body>
        //</soap:Envelope>
    }
    
    final String getArgumentIgnoreCase(String key) {
        for (Map.Entry<String, String> header : arguments.entrySet()) {
            if (header.getKey().equalsIgnoreCase(key)) {
                return header.getValue();
            }
        }
        return null;
    }
}
