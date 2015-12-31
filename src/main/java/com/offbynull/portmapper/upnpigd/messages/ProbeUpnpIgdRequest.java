/*
 * Copyright (c) 2013-2015, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.upnpigd.messages;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP probe request.
 * <p>
 * Specifications are documented at https://tools.ietf.org/html/draft-goland-http-udp-00 and
 * http://quimby.gnus.org/internet-drafts/draft-cai-ssdp-v1-03.txt
 * @author Kasra Faghihi
 */
public final class ProbeUpnpIgdRequest extends UpnpIgdHttpRequest {

    private static final String METHOD_NAME = "M-SEARCH";
    private static final String LOCATION = "*";
    private static final String HOST_KEY = "HOST";
    private static final String MAN_KEY = "MAN";
    private static final String MAN_VALUE = "ssdp:discover";
    private static final String MM_KEY = "MM";
    private static final String MX_KEY = "MX";
    private static final String ST_KEY = "ST";
    private static final String IPV4_HOST_STR = "239.255.255.250";
    private static final String IPV6_HOST_STR = "[FF02::C]"; // SEE TICKET FOR OTHER HOSTS TO PROBE, MINIUPNPC DEFINES ATLEAST 3
    private static final int PORT = 1900;

    // https://tools.ietf.org/html/draft-goland-http-udp-00
    // http://quimby.gnus.org/internet-drafts/draft-cai-ssdp-v1-03.txt

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
    public ProbeUpnpIgdRequest(ProbeDeviceType probeDeviceType, Integer mm, Integer mx, String serviceType) {
        super(METHOD_NAME, LOCATION, generateHeaders(probeDeviceType, mm, mx, serviceType), null);
    }
    
    private static Map<String, String> generateHeaders(ProbeDeviceType probeDeviceType, Integer mm, Integer mx, String serviceType) {
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

        Map<String, String> ret = new HashMap<>();
        
        if (mm != null) {
            ret.put(MM_KEY, mm.toString());
        }
        if (mx != null) {
            ret.put(MX_KEY, mx.toString());
        }
        ret.put(ST_KEY, serviceType);
        ret.put(MAN_KEY, MAN_VALUE);
        
        switch (probeDeviceType) {
            case IPV4:
                ret.put(HOST_KEY, IPV4_HOST_STR + ':' + PORT);
                break;
            case IPV6:
                ret.put(HOST_KEY, IPV6_HOST_STR + ':' + PORT);
                break;
            default:
                throw new IllegalStateException(); // should never happen
        }
        
        return ret;
    }

//    /**
//     * Constructs a {@link ProbeRequest} object by parsing a buffer.
//     * @param buffer buffer containing probe request data
//     * @throws NullPointerException if any argument is {@code null}
//     * @throws IllegalArgumentException if buffer is malformed (M-SEARCH query is missing, or ST header is missing, or HOST header is
//     * incorrect/missing, or MAN header is missing or isn't {@code "ssdp:discover"})
//     */
//    public ProbeRequest(byte[] buffer) {
//        super(buffer);
//        
//        Validate.isTrue(getMethod().equalsIgnoreCase(METHOD_NAME));
//        Validate.isTrue(getLocation().equalsIgnoreCase(LOCATION));
//        
//        
//        // Get header values
//        String mmValue = getHeaderIgnoreCase(MM_KEY);
//        String mxValue = getHeaderIgnoreCase(MX_KEY);
//        String stValue = getHeaderIgnoreCase(ST_KEY);
//        String manValue = getHeaderIgnoreCase(MAN_KEY);
//        String hostValue = getHeaderIgnoreCase(HOST_KEY);
//
//        
//        // Check for required values
//        Validate.isTrue(stValue != null);
//        Validate.isTrue(hostValue != null);
//        Validate.isTrue(MAN_VALUE.equalsIgnoreCase(manValue)); // ignore case -- trying to be fault tolerant
//
//        
//        // Validate MM
//        Integer mmAsInt = null;
//        try {
//            mmAsInt = Integer.parseInt(mmValue);
//            Validate.isTrue(mmAsInt >= 0);
//        } catch (IllegalArgumentException e) { // NumberFormatException is derived from IllegalArgException
//            // ignore if value is incorrect -- trying to be fault tolerant
//        }
//
//        
//        // Validate MX
//        Integer mxAsInt = null;
//        try {
//            mxAsInt = Integer.parseInt(mxValue);
//            Validate.isTrue(mxAsInt >= 0);
//        } catch (IllegalArgumentException e) { // NumberFormatException is derived from IllegalArgException
//            // ignore if value is incorrect -- trying to be fault tolerant
//        }
//        
//        if (mmAsInt != null && mxAsInt != null && mmAsInt > mxAsInt) {
//            // min is greater than max, not allowed so blank it out MM -- trying to be fault tolerant
//            removeHeaderIgnoreCase(MM_KEY);
//        }
//        
//        
//        // Validate HOST
//        String addrSuffix = ":" + PORT;
//        Validate.isTrue(hostValue.endsWith(addrSuffix)); // ignore warning: host value already checked for nullness
//        
//        hostValue = hostValue.substring(0, addrSuffix.length());
//        InetAddress hostAddr;
//        try {
//            hostAddr = InetAddress.getByName(hostValue); // ipv6 surrounded by square brackets properly parsed by this method
//        } catch (UnknownHostException uhe) {
//            throw new IllegalArgumentException(uhe);
//        }
//        
//        if (!(hostAddr.equals(IPV4_HOST) || hostAddr.equals(IPV6_HOST))) {
//            throw new IllegalArgumentException();
//        }
//    }
//
//    /**
//     * Get the MM value -- minimum amount of time the UPnP server will wait before responding ({@code null} if not set).
//     * @return minimum wait time
//     */
//    public Integer getMm() {
//        String mmValue = getHeaderIgnoreCase(MM_KEY);
//        try {
//            return Integer.parseInt(mmValue);
//        } catch (IllegalArgumentException e) {
//            return null;
//        }
//    }
//
//    /**
//     * Get the MX value -- minimum amount of time the UPnP server will wait before responding ({@code null} if not set).
//     * @return maximum wait time
//     */
//    public Integer getMx() {
//        String mxValue = getHeaderIgnoreCase(MX_KEY);
//        try {
//            return Integer.parseInt(mxValue);
//        } catch (IllegalArgumentException e) {
//            return null;
//        }
//    }
//
//    /**
//     * Get the ST value -- UPnP service type being probed for.
//     * @return service type being probed for
//     */
//    public String getServiceType() {
//        return getHeaderIgnoreCase(ST_KEY);
//    }
//
//    /**
//     * Get type of device being probe for (IPv4 or IPv6).
//     * @return type of device being probed for
//     */
//    public ProbeDeviceType getProbeDeviceType() {
//        String hostValue = getHeaderIgnoreCase(HOST_KEY);
//        
//        String addrSuffix = ":" + PORT;
//        Validate.validState(hostValue.endsWith(addrSuffix)); // should never happen -- validation checks in constructor
//        
//        hostValue = hostValue.substring(0, addrSuffix.length());
//        InetAddress hostAddr;
//        try {
//            hostAddr = InetAddress.getByName(hostValue); // ipv6 surrounded by square brackets properly parsed by this method
//        } catch (UnknownHostException uhe) {
//            throw new IllegalArgumentException(uhe);
//        }
//        
//        if (hostAddr.equals(IPV4_HOST)) {
//            return ProbeDeviceType.IPV4;
//        } else if (hostAddr.equals(IPV6_HOST)) {
//            return ProbeDeviceType.IPV6;
//        } else {
//            throw new IllegalStateException(); // should never happen -- validation checks in constructor
//        }
//    }

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
