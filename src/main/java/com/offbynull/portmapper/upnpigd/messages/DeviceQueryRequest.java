package com.offbynull.portmapper.upnpigd.messages;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP device query request.
 * @author Kasra Faghihi
 */
public final class DeviceQueryRequest extends UpnpIgdHttpRequest {
    private static final String HOST_KEY = "HOST";
    
    /**
     * Constructs a {@link DeviceQueryRequest} object.
     * @param host device host
     * @param rootLocation device's root URL location (found during discovery)
     */
    public DeviceQueryRequest(String host, String rootLocation) {
        super("GET", rootLocation, generateHeaders(host), null);
    }
    
    private static Map<String, String> generateHeaders(String host) {
        Validate.notNull(host);
        
        Map<String, String> ret = new HashMap<>();
        
        ret.put(HOST_KEY, host);
        
        return ret;
    }
}
