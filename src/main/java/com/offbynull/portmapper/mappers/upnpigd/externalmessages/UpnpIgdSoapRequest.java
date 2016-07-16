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
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<soap:Envelope ")
                .append("xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" ")
                .append("soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">");
        sb.append("<soap:Body>");
        sb.append("<u:").append(actionName).append(" xmlns:u=\"").append(serviceType).append("\">");

        for (Entry<String, String> argument : arguments.entrySet()) {
            String key = StringEscapeUtils.escapeXml10(argument.getKey());
            String val = StringEscapeUtils.escapeXml10(argument.getValue());
            sb.append("<").append(key).append(">").append(val).append("</").append(key).append(">");
        }

        sb.append("</u:").append(actionName).append(">");
        sb.append("</soap:Body>");
        sb.append("</soap:Envelope>");

        return sb.toString();
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public String toString() {
        return "UpnpIgdSoapRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
    // CHECKSTYLE:ON:DesignForExtension
}
