package com.offbynull.portmapper.upnpigd.messages;

import com.offbynull.portmapper.common.NetworkUtils;
import com.offbynull.portmapper.common.TextUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * * Represents a UPnP probe request.
 * @author Kasra Faghihi
 */
public final class ProbeRequest implements UpnpIgdMessage {

    private static final String QUERY_STRING = "M-SEARCH * HTTP/1.1";
    private static final String HOST_KEY = "HOST";
    private static final String MAN_KEY = "MAN";
    private static final String MAN_VALUE = "ssdp:discover";
    private static final String MM_KEY = "MM";
    private static final String MX_KEY = "MX";
    private static final String ST_KEY = "ST";
    private static final String IPV4_HOST_STR = "239.255.255.250";
    private static final String IPV6_HOST_STR = "[FF02::C]";
    private static final InetAddress IPV4_HOST =
            NetworkUtils.convertBytesToAddress(new byte[] { -17, -1, -1, -6 }); // 239.255.255.250
    private static final InetAddress IPV6_HOST =
            NetworkUtils.convertBytesToAddress(new byte[] { -1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12 }); // FF02::C
    private static final int PORT = 1900;
    private static final String TERMINATOR = "\r\n";
    

    
    private Integer mm; // https://tools.ietf.org/html/draft-goland-http-udp-00
    private Integer mx; // https://tools.ietf.org/html/draft-goland-http-udp-00
    private String serviceType; // http://quimby.gnus.org/internet-drafts/draft-cai-ssdp-v1-03.txt
    private ProbeDeviceType probeDeviceType;

    /**
     * Constructs a {@link ProbeRequest} object.
     * @param probeDeviceType type of device to probe for (IPv4 or IPv6)
     * @param mm minimum amount of time the UPnP server will wait before responding (can be {@code null} -- set to {@code null} if you don't
     * know or care)
     * @param mx maximum amount of time the UPnP server will wait before responding (can be {@code null} -- set to 3 if you don't know or
     * care)
     * @param serviceType service type to probe for (set to {@code "ssdp:all"} if you don't know or care)
     * @throws NullPointerException if {@code probeDeviceType} or {@code serviceType} is {@code null}
     * @throws IllegalArgumentException if {@code mm < 0 || mx < 0 || mm > mx}
     */
    public ProbeRequest(ProbeDeviceType probeDeviceType, Integer mm, Integer mx, String serviceType) {
        Validate.notNull(probeDeviceType);
        if (mm != null) {
            Validate.isTrue(mm >= 0);
        }
        if (mx != null) {
            Validate.isTrue(mx >= 0);
        }
        if (mm != null && mx != null) {
            Validate.isTrue(mx >= mm);
        }
        Validate.notNull(serviceType);

        this.mm = mm;
        this.mx = mx;
        this.serviceType = serviceType;
        
        switch (probeDeviceType) {
            case IPV4:
            case IPV6:
                this.probeDeviceType = probeDeviceType;
                break;
            default:
                throw new IllegalStateException(); // should never happen
        }
    }

    /**
     * Constructs a {@link ProbeRequest} object by parsing a buffer.
     * @param buffer buffer containing probe request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if buffer is malformed (M-SEARCH query is missing, or ST header is missing, or HOST header is
     * incorrect/missing, or MAN header is missing or isn't {@code "ssdp:discover"})
     */
    public ProbeRequest(byte[] buffer) {
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
        
        Validate.isTrue(QUERY_STRING.equalsIgnoreCase(queryStr)); // must be query string -- case insensitive for fault tolerance
        
        
        // Convert lines to key-value pairs
        String mmValue = null;
        String mxValue = null;
        String stValue = null;
        String manValue = null;
        String hostValue = null;
        for (String line : lines) {
            String[] splitLine = StringUtils.split(line, ":", 1);
            if (splitLine.length != 2) {
                continue; // skip line if no : found
            }
            
            String key = splitLine[0].trim();
            String value = splitLine[1].trim();
            
            // ignore case when checking to be fault tolerant -- this is why we can't use a map
            if (key.equalsIgnoreCase(HOST_KEY)) {
                if (hostValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                hostValue = value;                
            } else if (key.equalsIgnoreCase(ST_KEY)) {
                if (stValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                stValue = value;
            } else if (key.equalsIgnoreCase(MM_KEY)) {
                if (mmValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                mmValue = value;
            } else if (key.equalsIgnoreCase(MX_KEY)) {
                if (mxValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                mxValue = value;
            } else if (key.equalsIgnoreCase(MAN_KEY)) {
                if (manValue != null) {
                    continue; // skip duplicate header -- trying to be fault tolerant
                }
                manValue = value;
            }
        }

        
        // Set key-value pairs
        Validate.isTrue(stValue != null);
        Validate.isTrue(hostValue != null);
        Validate.isTrue(MAN_VALUE.equalsIgnoreCase(manValue)); // ignore case -- trying to be fault tolerant

        try {
            int mmAsInt = Integer.parseInt(mmValue);
            Validate.isTrue(mmAsInt >= 0);
            mm = mmAsInt;
        } catch (IllegalArgumentException e) { // NumberFormatException is derived from IllegalArgException
            // ignore if value is incorrect -- trying to be fault tolerant
        }

        try {
            int mxAsInt = Integer.parseInt(mxValue);
            Validate.isTrue(mxAsInt >= 0);
            mx = mxAsInt;
        } catch (IllegalArgumentException e) { // NumberFormatException is derived from IllegalArgException
            // ignore if value is incorrect -- trying to be fault tolerant
        }
        
        if (mm != null && mx != null && mm > mx) {
            // min is greater than max, not allowed so blank it out. don't crash here because we want to be fault tolerant
            mm = null;
            mx = null;
        }
        
        serviceType = stValue;
        
        String addrSuffix = ":" + PORT;
        Validate.isTrue(hostValue.endsWith(addrSuffix)); // ignore warning: host value already checked for nullness
        
        hostValue = hostValue.substring(0, addrSuffix.length());
        InetAddress hostAddr;
        try {
            hostAddr = InetAddress.getByName(hostValue); // ipv6 surrounded by square brackets properly parsed by this method
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException(uhe);
        }
        
        if (hostAddr.equals(IPV4_HOST)) {
            probeDeviceType = ProbeDeviceType.IPV4;
        } else if (hostAddr.equals(IPV6_HOST)) {
            probeDeviceType = ProbeDeviceType.IPV6;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get the MM value -- minimum amount of time the UPnP server will wait before responding ({@code null} if not set).
     * @return minimum wait time
     */
    public Integer getMm() {
        return mm;
    }

    /**
     * Get the MX value -- minimum amount of time the UPnP server will wait before responding ({@code null} if not set).
     * @return maximum wait time
     */
    public Integer getMx() {
        return mx;
    }

    /**
     * Get the ST value -- UPnP service type being probed for.
     * @return service type being probed for
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Get type of device being probe for (IPv4 or IPv6).
     * @return type of device being probed for
     */
    public ProbeDeviceType getProbeDeviceType() {
        return probeDeviceType;
    }

    @Override
    public byte[] dump() {
        StringBuilder sb = new StringBuilder();
        
        // "M-SEARCH * HTTP/1.1\r\n"
        sb.append(QUERY_STRING).append(TERMINATOR);
        
        // "HOST: 239.255.255.250:1900\r\n"
        //   ... or ...
        // "HOST: [FF02::C]:1900\r\n"
        sb.append(HOST_KEY).append(": ");
        switch (probeDeviceType) {
            case IPV4:
                sb.append(IPV4_HOST_STR);
                break;
            case IPV6:
                sb.append(IPV6_HOST_STR);
                break;
            default:
                throw new IllegalStateException();
        }
        sb.append(TERMINATOR);
        
        // "MAN: ssdp:discover\r\n"
        sb.append(MAN_KEY).append(": ").append(MAN_VALUE).append(TERMINATOR);
        
        // "MM: <some positive int>\r\n"  -- discarded if null
        if (mm != null) {
            sb.append(MM_KEY).append(": ").append(mm).append(TERMINATOR);
        }
        
        // "MX: <some positive int>\r\n"   -- discarded if null
        if (mx != null) {
            sb.append(MX_KEY).append(": ").append(mx).append(TERMINATOR);
        }
        
        // "ST: <service type>\r\n"
        sb.append(ST_KEY).append(": ").append(serviceType).append(TERMINATOR);
        
        // "\r\n"
        sb.append(TERMINATOR);
        
        return sb.toString().getBytes(Charsets.toCharset("US-ASCII"));
    }

    /**
     * Type of device to probe.
     */
    public enum ProbeDeviceType {
        /**
         * Probe for IPv4 device.
         */
        IPV4,
        /**
         * Probe fro IPv6 device.
         */
        IPV6
    }
}
