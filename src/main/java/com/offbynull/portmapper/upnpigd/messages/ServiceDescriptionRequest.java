package com.offbynull.portmapper.upnpigd.messages;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP service description request.
 * @author Kasra Faghihi
 */
public final class ServiceDescriptionRequest extends UpnpIgdHttpRequest {

    /**
     * Constructs a {@link ServiceDescriptionRequest} object.
     * @param host device host
     * @param scpdLocation SCPD location (found during discovery)
     */
    public ServiceDescriptionRequest(String host, String scpdLocation) {
        super("GET", scpdLocation, generateHeaders(host), null);
    }
    
    private static Map<String, String> generateHeaders(String host) {
        Validate.notNull(host);
        
        Map<String, String> ret = new HashMap<>();
        
        // content-length is added by parent class
        ret.put("Host", host);
        ret.put("Connection", "Close");
        
        return ret;
    }
}
