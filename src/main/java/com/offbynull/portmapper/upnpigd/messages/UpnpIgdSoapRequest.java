package com.offbynull.portmapper.upnpigd.messages;

import java.util.HashMap;
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

    private static final String HOST_KEY = "Host";
    private static final String CONNECTION_KEY = "Connection";

    UpnpIgdSoapRequest(String host, String location, String serviceType, String actionName, Map<String, String> arguments) {
        super("POST", location, generateHeaders(host), generateContent(serviceType, actionName, arguments));

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

    private static Map<String, String> generateHeaders(String host) {
        Validate.notNull(host);

        Map<String, String> ret = new HashMap<>();

        ret.put(HOST_KEY, host);
        ret.put(CONNECTION_KEY, "Close");

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
            sb.append("<").append(key).append("/>").append(val).append("<").append(key).append("/>\r\n");
        }

        sb.append("</u:").append(actionName).append(">\r\n");
        sb.append("</soap:Body>\r\n");
        sb.append("</soap:Envelope>\r\n");

        return sb.toString();
    }
}
