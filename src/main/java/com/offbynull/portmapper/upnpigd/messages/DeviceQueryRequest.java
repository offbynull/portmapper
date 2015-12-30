package com.offbynull.portmapper.upnpigd.messages;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP device query request.
 * @author Kasra Faghihi
 */
public final class DeviceQueryRequest extends UpnpIgdHttpRequest {

    /**
     * Constructs a {@link DeviceQueryRequest} object.
     * @param host device host
     * @param rootLocation device's root location (found during discovery)
     */
    public DeviceQueryRequest(String host, String rootLocation) {
        super("GET", rootLocation, generateHeaders(host), null);
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
