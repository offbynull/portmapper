package com.offbynull.portmapper.upnpigd.messages;

import com.offbynull.portmapper.common.TextUtils;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP probe response.
 * @author Kasra Faghihi
 */
public final class ProbeResponse implements UpnpIgdMessage {

    private static final String QUERY_STRING = "HTTP/1.1 200 OK";
    private static final String LOCATION_KEY = "HOST";
    private static final String SERVER_KEY = "SERVER";
    private static final String USN_KEY = "USN";
    private static final String ST_KEY = "ST";
    private static final String TERMINATOR = "\r\n";

    // http://quimby.gnus.org/internet-drafts/draft-cai-ssdp-v1-03.txt
    private String location;
    private String server;
    private String usn;
    private String serviceType;

    // examples for Javadoc taken from http://www.upnp-hacks.org/upnp.html
    /**
     * Constructs a {@link ProbeResponse} object.
     * @param location type of device to probe for (IPv4 or IPv6)
     * @param server name of the device replying to the probe (can be {@code null} -- should be there but not required for identifying
     * UPnP-IGD devices -- e.g. {@code "SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)"})
     * @param usn unique service name of the device replying to the probe (can be {@code null} -- should be there but not required for
     * identifying UPnP-IGD devices -- e.g. {@code "uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1"})
     * @param serviceType service type of the device replying to the probe (can be {@code null} -- should be there but not required for
     * identifying UPnP-IGD devices (action names are used instead) -- e.g. {@code "urn:schemas-upnp-org:service:WANPPPConnection:1"})
     * @throws NullPointerException if {@code location} is {@code null}
     */
    public ProbeResponse(String location, String server, String usn, String serviceType) {
        Validate.notNull(location);
        // server can be null
        // usn can be null
        // serviceType can be null

        this.location = location;
        this.server = server;
        this.usn = usn;
        this.serviceType = serviceType;
    }

    /**
     * Constructs a {@link ProbeResponse} object by parsing a buffer.
     * @param buffer buffer containing probe request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if buffer is malformed (response is not 200 OK, or LOCATION header is missing)
     */
    public ProbeResponse(byte[] buffer) {
        Validate.notNull(buffer);

        // Convert buffer to string
        String query = new String(buffer, Charset.forName("US-ASCII"));

        // Split buffer to lines
        List<String> lines = Arrays.asList(StringUtils.splitByWholeSeparator(query, "\r\n"));

        // Search until empty line -- stuff after empty line is content, which technically can be valid, but we ignore it.
        // If we didn't find an empty line, just use the whole list -- trying to be fault tolerant here
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(0).isEmpty()) {
                lines = lines.subList(0, i);
                break;
            }
        }

        // Validate that the query string is what it should be
        Validate.isTrue(lines.size() >= 1); // must contain at least 1 line, which is the query string'
        String queryStr = TextUtils.collapseWhitespace(lines.get(0)).trim(); // get query string, collapse whitespace for fault tolerance

        Validate.isTrue(QUERY_STRING.equalsIgnoreCase(queryStr)); // must start with resp OK string -- case insensitive for fault tolerance

        // Convert lines to key-value pairs
        String locationValue = null;
        String serverValue = null;
        String usnValue = null;
        String stValue = null;
        for (String line : lines) {
            String[] splitLine = StringUtils.split(line, ":", 1);
            if (splitLine.length != 2) {
                continue; // skip line if no : found
            }

            String key = splitLine[0].trim();
            String value = splitLine[1].trim();

            // ignore case when checking to be fault tolerant -- this is why we can't use a map
            if (key.equalsIgnoreCase(LOCATION_KEY)) {
                if (locationValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                locationValue = value;
            } else if (key.equalsIgnoreCase(SERVER_KEY)) {
                if (serverValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                serverValue = value;
            } else if (key.equalsIgnoreCase(USN_KEY)) {
                if (usnValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                usnValue = value;
            } else if (key.equalsIgnoreCase(ST_KEY)) {
                if (stValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                stValue = value;
            }
        }

        // Set key-value pairs
        Validate.isTrue(locationValue != null);
        // server usn and st can be null -- not really but we only care about location

        location = locationValue;
        server = serverValue;
        serviceType = stValue;
        usn = usnValue;
    }

    public String getLocation() {
        return location;
    }

    public String getServer() {
        return server;
    }

    public String getUsn() {
        return usn;
    }

    public String getServiceType() {
        return serviceType;
    }

    @Override
    public byte[] dump() {
        StringBuilder sb = new StringBuilder();

        // "HTTP/1.1 200 OK\r\n"
        sb.append(QUERY_STRING).append(TERMINATOR);

        // "LOCATION: <some http url>\r\n"
        sb.append(LOCATION_KEY).append(": ").append(location).append(TERMINATOR);

        // "SERVER: <some string>\r\n"
        if (server != null) {
            sb.append(SERVER_KEY).append(": ").append(server).append(TERMINATOR);
        }

        // "ST: <some service type>\r\n"
        if (serviceType != null) {
            sb.append(ST_KEY).append(": ").append(serviceType).append(TERMINATOR);
        }

        // "USN: <some usn>\r\n"
        if (usn != null) {
            sb.append(USN_KEY).append(": ").append(usn).append(TERMINATOR);
        }
        // "\r\n"
        sb.append(TERMINATOR);

        return sb.toString().getBytes(Charsets.toCharset("US-ASCII"));
    }
}
