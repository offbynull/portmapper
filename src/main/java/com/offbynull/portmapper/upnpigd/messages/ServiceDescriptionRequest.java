package com.offbynull.portmapper.upnpigd.messages;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP service description request.
 * @author Kasra Faghihi
 */
public final class ServiceDescriptionRequest extends UpnpIgdHttpRequest {
    private static final String HOST_KEY = "HOST";
    
    /**
     * Constructs a {@link ServiceDescriptionRequest} object.
     * @param host device host
     * @param scpdLocation scpd URL location (found during discovery)
     */
    public ServiceDescriptionRequest(String host, String scpdLocation) {
        super("GET", scpdLocation, generateHeaders(host), null);
    }
    
    private static Map<String, String> generateHeaders(String host) {
        Validate.notNull(host);
        
        Map<String, String> ret = new HashMap<>();
        
        ret.put(HOST_KEY, host);
        
        return ret;
    }
}
