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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP probe request.
 * <p>
 * Specifications are documented at https://tools.ietf.org/html/draft-goland-http-udp-00 and
 * http://quimby.gnus.org/internet-drafts/draft-cai-ssdp-v1-03.txt
 * @author Kasra Faghihi
 */
public final class ServiceDiscoveryUpnpIgdRequest extends UpnpIgdHttpRequest {

    private static final String METHOD_NAME = "M-SEARCH";
    private static final String LOCATION = "*";
    private static final String HOST_KEY = "HOST";
    private static final String MAN_KEY = "MAN";
    private static final String MAN_VALUE = "ssdp:discover";
    private static final String MM_KEY = "MM";
    private static final String MX_KEY = "MX";
    private static final String ST_KEY = "ST";

    // https://tools.ietf.org/html/draft-goland-http-udp-00
    // http://quimby.gnus.org/internet-drafts/draft-cai-ssdp-v1-03.txt

    /**
     * Constructs a {@link ServiceDiscoveryUpnpIgdRequest} object.
     * @param probeDeviceType type of device to probe for (IPv4 or IPv6)
     * @param mm minimum amount of time the UPnP server will wait before responding (can be {@code null} -- set to {@code null} if you don't
     * know or care)
     * @param mx maximum amount of time the UPnP server will wait before responding (can be {@code null} -- set to 3 if you don't know or
     * care)
     * @param serviceType service type to probe for (set to {@code "ssdp:all"} if you don't know or care)
     * @throws NullPointerException if {@code probeDeviceType} or {@code serviceType} is {@code null}
     * @throws IllegalArgumentException if {@code mm < 0 || mx < 0 || mm > mx}
     */
    public ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType probeDeviceType, Integer mm, Integer mx, String serviceType) {
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

        Map<String, String> ret = new LinkedHashMap<>();
        
        if (mm != null) {
            ret.put(MM_KEY, mm.toString());
        }
        if (mx != null) {
            ret.put(MX_KEY, mx.toString());
        }
        ret.put(ST_KEY, serviceType);
        ret.put(MAN_KEY, MAN_VALUE);
        
        ret.put(HOST_KEY, probeDeviceType.getMulticastSocketAddressAsHeader());
        
        return ret;
    }

    @Override
    public String toString() {
        return "ServiceDiscoveryUpnpIgdRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
    
    /**
     * Type of device to probe.
     */
    public enum ProbeDeviceType {
        /**
         * Probe for IPv4 device.
         */
        IPV4("239.255.255.250", 1900),
        /**
         * Probe for link local IPv6 device.
         */
        IPV6_LINK_LOCAL("[FF02::C]", 1900),
        /**
         * Probe for site local IPv6 device.
         */
        IPV6_SITE_LOCAL("[FF05::C]", 1900),
        // We're targetting home routers -- so should we borther checking GLOBAL and ORGANIZATION-LOCAL???
        /**
         * Probe for organization local IPv6 device.
         */
        IPV6_ORGANIZATION_LOCAL("[FF08::C]", 1900),
        /**
         * Probe for global IPv6 device.
         */
        IPV6_GLOBAL("[FF0E::C]", 1900);
        
        private String multicastSocketAddressAsHeader;
        private InetSocketAddress multicastSocketAddress;
        
        ProbeDeviceType(String multicastHost, int port) {
            Validate.notNull(multicastHost);
            Validate.inclusiveBetween(1, 65535, port);
            this.multicastSocketAddressAsHeader = multicastHost + ':' + port;
            InetAddress multicastAddress;
            try {
                multicastAddress = InetAddress.getByName(multicastHost);
            } catch (UnknownHostException uhe) {
                throw new IllegalArgumentException(uhe);
            }
            this.multicastSocketAddress = new InetSocketAddress(multicastAddress, port);
        }

        private String getMulticastSocketAddressAsHeader() {
            return multicastSocketAddressAsHeader;
        }

        /**
         * Get multicast socket address to probe for UPnP-IGD devices.
         * @return multicast socket address
         */
        public InetSocketAddress getMulticastSocketAddress() {
            return multicastSocketAddress;
        }

    }
}
