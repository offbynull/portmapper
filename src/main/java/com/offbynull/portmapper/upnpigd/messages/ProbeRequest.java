package com.offbynull.portmapper.upnpigd.messages;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public final class ProbeRequest {

    private static final String IPV4_SEARCH_QUERY =
            "M-SEARCH * HTTP/1.1\r\n"
            + "HOST: 239.255.255.250:1900\r\n"
            + "MAN: ssdp:discover\r\n" // device discovery
            + "MX: 3\r\n" // server should send response in rand(0, MX)
            + "ST: ssdp:all\r\n" // query any service type (see example in http://www.upnp-hacks.org/upnp.html to finetune if desired)
            + "\r\n";

    private static final String IPV6_SEARCH_QUERY =
            "M-SEARCH * HTTP/1.1\r\n"
            + "HOST: [FF02::C]:1900\r\n"
            + "MAN: ssdp:discover\r\n" // device discovery
            + "MX: 3\r\n" // server should send response in rand(0, MX)
            + "ST: ssdp:all\r\n" // query any service type (see example in http://www.upnp-hacks.org/upnp.html to finetune if desired)
            + "\r\n";
    
    private Mode mode;

    public ProbeRequest(Mode mode) {
        Validate.notNull(mode);

        switch (mode) {
            case IPV4:
            case IPV6:
                this.mode = mode;
                break;
            default:
                throw new IllegalStateException(); // should never happen
        }
    }

    public ProbeRequest(byte[] buffer) {
        Validate.notNull(buffer);

        
        // Convert buffer to string
        String query = new String(buffer, Charset.forName("US-ASCII"));
        
        
        // Split buffer to lines
        String[] lines = StringUtils.splitByWholeSeparator(query, "\r\n");
        Validate.isTrue(lines.length >= 2); // must contain at least 2 lines to be valid -- 2 lines noted in checks just below this one
        Validate.isTrue(lines[0].equalsIgnoreCase("M-SEARCH * HTTP/1.1")); // must start with M-SEARCH string
        Validate.isTrue(lines[lines.length - 1].isEmpty()); // must end with empty line
        
        
        // Convert lines to key-value pairs
        Map<String, String> dataMap = new HashMap<>();
        for (String line : lines) {
            String[] splitLine = StringUtils.split(line, ":", 1);
            if (splitLine.length != 2) {
                continue; // skip line if no : found
            }
            
            String key = splitLine[0].trim().toLowerCase(Locale.ENGLISH);
            String value = splitLine[1].trim();
            
            dataMap.put(key, value);
        }

        
        // Validate key-value pairs
        Validate.isTrue(dataMap.containsKey("st")); // do we care what st is? or de we just care that it exists?
        Validate.isTrue(dataMap.containsKey("mx")); // do we care that the mx field is present?
        Validate.isTrue("ssdp:discover".equalsIgnoreCase(dataMap.get("man")));
        
        
        // Pick mode based on host value
        String host = dataMap.get("host");
        Validate.isTrue(host != null);
        switch (host.toLowerCase(Locale.ENGLISH)) {
            case "239.255.255.250:1900":
                mode = Mode.IPV4;
                break;
            case "[FF02::C]:1900":
                mode = Mode.IPV6;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Mode getMode() {
        return mode;
    }

    public byte[] dump() {
        switch (mode) {
            case IPV4:
                return IPV4_SEARCH_QUERY.getBytes(Charset.forName("US-ASCII"));
            case IPV6:
                return IPV6_SEARCH_QUERY.getBytes(Charset.forName("US-ASCII"));
            default:
                throw new IllegalStateException();
        }
    }

    public enum Mode {
        IPV4,
        IPV6
    }
}
