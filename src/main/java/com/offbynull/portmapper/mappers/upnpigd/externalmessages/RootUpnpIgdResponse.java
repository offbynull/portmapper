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

import com.offbynull.portmapper.helpers.TextUtils;
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
     * Constructs a {@link RootUpnpIgdResponse} object by parsing a buffer.
     *
     * @param baseUrl device URI
     * @param buffer buffer containing response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public RootUpnpIgdResponse(URL baseUrl, byte[] buffer) {
        super(buffer);

//        Validate.isTrue(isResponseSuccessful());
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
    public String toString() {
        return "RootUpnpIgdResponse{super=" + super.toString() +  "services=" + services + '}';
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 67 * hash + Objects.hashCode(this.services);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RootUpnpIgdResponse other = (RootUpnpIgdResponse) obj;
        if (!Objects.equals(this.services, other.services)) {
            return false;
        }
        return true;
    }

    /**
     * Bean that represents a UPNP-IGD root XML service tag.
     */
    public static final class ServiceReference {

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
        public String toString() {
            return "ServiceReference{" + "serviceType=" + serviceType + ", controlUrl=" + controlUrl + ", scpdUrl=" + scpdUrl + '}';
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + Objects.hashCode(this.serviceType);
            hash = 53 * hash + Objects.hashCode(this.controlUrl);
            hash = 53 * hash + Objects.hashCode(this.scpdUrl);
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
    }

}
