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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP device query request.
 *
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdSoapRequest extends UpnpIgdHttpRequest {

    UpnpIgdSoapRequest(String host, String location, String serviceType, String actionName, Map<String, String> arguments) {
        super("POST", location,
                generateHeaders(host, serviceType, actionName),
                generateContent(serviceType, actionName, arguments));

        //<?xml version="1.0"?>
        //<soap:Envelope
        //xmlns:soap="http://www.w3.org/2003/05/soap-envelope/"
        //soap:encodingStyle="http://www.w3.org/2003/05/soap-encoding">
        //<soap:Body>
        // <u:actionname xmlns:u="servicetype">
        //   <key>value</key>
        //   <key>value</key>
        //   <key>value</key>
        //   <key>value</key>
        // </u:%s>
        //</soap:Body>
        //</soap:Envelope>
        //<?xml version="1.0"?>
    }

    private static Map<String, String> generateHeaders(String host, String serviceType, String actionName) {
        Validate.notNull(serviceType);
        Validate.notNull(actionName);

        Map<String, String> ret = new LinkedHashMap<>();

        // content-length is added by parent class
        ret.put("Host", host);
        ret.put("Content-Type", "text/xml");
        ret.put("SOAPAction", serviceType + "#" + actionName);
        ret.put("Connection", "Close");
        ret.put("Cache-Control", "no-cache");
        ret.put("Pragma", "no-cache");

        return ret;
    }

    private static String generateContent(String serviceType, String actionName, Map<String, String> arguments) {
        Validate.notNull(serviceType);
        Validate.notNull(actionName);
        Validate.notNull(arguments);
        Validate.noNullElements(arguments.keySet());
        Validate.noNullElements(arguments.values());

        serviceType = StringEscapeUtils.escapeXml10(serviceType);
        actionName = StringEscapeUtils.escapeXml10(actionName);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\r\n");
        sb.append("<soap:Envelope ")
                .append("xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" ")
                .append("soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n");
        sb.append("<soap:Body>\r\n");
        sb.append("<u:").append(actionName).append(" xmlns:u=\"").append(serviceType).append("\">\r\n");

        for (Entry<String, String> argument : arguments.entrySet()) {
            String key = StringEscapeUtils.escapeXml10(argument.getKey());
            String val = StringEscapeUtils.escapeXml10(argument.getValue());
            sb.append("<").append(key).append(">").append(val).append("</").append(key).append(">\r\n");
        }

        sb.append("</u:").append(actionName).append(">\r\n");
        sb.append("</soap:Body>\r\n");
        sb.append("</soap:Envelope>\r\n");

        return sb.toString();
    }
    
    // CHECKSTYLE:OFF:DesignForExtension
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
        final UpnpIgdHttpRequest other = (UpnpIgdHttpRequest) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
