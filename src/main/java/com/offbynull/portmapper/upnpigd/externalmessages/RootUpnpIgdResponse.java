/*
 * Copyright (c) 2013-2016, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.upnpigd.externalmessages;

import com.offbynull.portmapper.common.TextUtils;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP device query response.
 *
 * @author Kasra Faghihi
 */
public final class RootUpnpIgdResponse extends UpnpIgdHttpResponse {

    private List<ServiceReference> services;

    /**
     * Constructs a {@link DeviceQueryResponse} object by parsing a buffer.
     *
     * @param baseUrl device URI
     * @param buffer buffer containing response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public RootUpnpIgdResponse(URL baseUrl, byte[] buffer) {
        super(buffer);

        Validate.isTrue(isResponseSuccessful());
        Validate.notNull(baseUrl);

        String content = getContent();

        String baseUrlOverrideStr = TextUtils.findFirstBlock(content, "<URLBase>", "</URLBase>", true);
        if (baseUrlOverrideStr != null) {
            try {
                baseUrl = new URL(baseUrlOverrideStr);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        List<String> serviceBlocks = TextUtils.findAllBlocks(content, "<service>", "</service>", true);
        List<ServiceReference> servicesList = new ArrayList<>(serviceBlocks.size());
        for (String serviceBlock : serviceBlocks) {
            String serviceType = TextUtils.findFirstBlock(serviceBlock, "<serviceType>", "</serviceType>", true);
//            String serviceId = TextUtils.findFirstBlock(serviceBlock, "<serviceId>", "</serviceId>", true);
            String controlUrl = TextUtils.findFirstBlock(serviceBlock, "<controlURL>", "</controlURL>", true);
            //String eventSubUrl = TextUtils.findFirstBlock(serviceBlock, "<eventSubURL>", "</eventSubURL>");
            String scpdUrl = TextUtils.findFirstBlock(serviceBlock, "<SCPDURL>", "</SCPDURL>", true);

            ServiceReference service;
            try {
                service = new ServiceReference(
                        baseUrl,
                        StringUtils.trim(StringEscapeUtils.unescapeXml(serviceType)),
                        StringUtils.trim(StringEscapeUtils.unescapeXml(controlUrl)),
                        StringUtils.trim(StringEscapeUtils.unescapeXml(scpdUrl)));
                servicesList.add(service);
            } catch (IllegalArgumentException | NullPointerException e) {
                // unable to handle -- something was malformed or missing, so skip to next one
            }
        }

        this.services = Collections.unmodifiableList(servicesList);
    }

    /**
     * Get services.
     *
     * @return list of services (unmodifiable)
     */
    public List<ServiceReference> getServices() {
        return services;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 41 * hash + Objects.hashCode(this.services);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RootUpnpIgdResponse other = (RootUpnpIgdResponse) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (!Objects.equals(this.services, other.services)) {
            return false;
        }
        return true;
    }

    /**
     * Bean that represents a UPNP-IGD root XML service tag.
     */
    public final class ServiceReference {

        private final String serviceType;
        private final URL controlUrl;
        private final URL scpdUrl;

        /**
         * Constructs a {@link ServiceReference} object.
         *
         * @param baseUrl base URL
         * @param serviceType service type
         * @param controlUrl control URL
         * @param scpdUrl SCPD URL
         * @throws IllegalArgumentException if any of the URL arguments are malformed
         * @throws NullPointerException if any argument other than {@code serviceType} and {@code serviceId} is {@code null}
         */
        public ServiceReference(URL baseUrl, String serviceType, String controlUrl, String scpdUrl) {
            Validate.notNull(baseUrl);
            Validate.notNull(serviceType);
            Validate.notNull(controlUrl); // need this for controlling
            Validate.notNull(scpdUrl); // need this to see if port mapping actions are present with service

            this.serviceType = serviceType;
            try {
                this.controlUrl = baseUrl.toURI().resolve(controlUrl).toURL();
                this.scpdUrl = baseUrl.toURI().resolve(scpdUrl).toURL();
            } catch (URISyntaxException | MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        /**
         * Get service type.
         *
         * @return service type
         */
        public String getServiceType() {
            return serviceType;
        }

        /**
         * Get control URL.
         *
         * @return control URL
         */
        public URL getControlUrl() {
            return controlUrl;
        }

        /**
         * Get SCPD URL.
         *
         * @return SCPD URL
         */
        public URL getScpdUrl() {
            return scpdUrl;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.serviceType);
            hash = 19 * hash + Objects.hashCode(this.controlUrl);
            hash = 19 * hash + Objects.hashCode(this.scpdUrl);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServiceReference other = (ServiceReference) obj;
            if (!Objects.equals(this.serviceType, other.serviceType)) {
                return false;
            }
            if (!Objects.equals(this.controlUrl, other.controlUrl)) {
                return false;
            }
            if (!Objects.equals(this.scpdUrl, other.scpdUrl)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ServiceReference{" + ", serviceType=" + serviceType + ", controlUrl=" + controlUrl + ", scpdUrl=" + scpdUrl + '}';
        }
    }
}
