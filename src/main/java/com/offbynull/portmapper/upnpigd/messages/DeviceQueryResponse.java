package com.offbynull.portmapper.upnpigd.messages;

import com.offbynull.portmapper.common.TextUtils;
import com.offbynull.portmapper.upnpigd.UpnpIgdServiceReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP device query response.
 * @author Kasra Faghihi
 */
public final class DeviceQueryResponse extends UpnpIgdHttpResponse {

    private List<ServiceReference> services;

    /**
     * Constructs a {@link DeviceQueryResponse} object by parsing a buffer.
     * @param deviceUri device URI
     * @param buffer buffer containing response data
     */
    public DeviceQueryResponse(URI deviceUri, byte[] buffer) {
        super(buffer);

        Validate.isTrue(isResponseSuccessful());
        Validate.notNull(deviceUri);

        String content = getContent();
        
        List<String> serviceBlocks = TextUtils.findAllBlocks(content, "<service>", "</service>", true);
        List<ServiceReference> servicesList = new ArrayList<>(serviceBlocks.size());
        for (String serviceBlock : serviceBlocks) {
            String serviceType = TextUtils.findFirstBlock(serviceBlock, "<serviceType>", "</serviceType>", true);
            String serviceId = TextUtils.findFirstBlock(serviceBlock, "<serviceId>", "</serviceId>", true);
            String controlUrl = TextUtils.findFirstBlock(serviceBlock, "<controlURL>", "</controlURL>", true);
            //String eventSubUrl = TextUtils.findFirstBlock(serviceBlock, "<eventSubURL>", "</eventSubURL>");
            String scpdUrl = TextUtils.findFirstBlock(serviceBlock, "<SCPDURL>", "</SCPDURL>", true);

            ServiceReference service;
            try {
                service = new ServiceReference(
                        deviceUri,
                        StringUtils.trim(serviceType),
                        StringUtils.trim(serviceId),
                        StringUtils.trim(controlUrl),
                        StringUtils.trim(scpdUrl));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }

            servicesList.add(service);
        }
        
        this.services = Collections.unmodifiableList(services);
    }

    /**
     * Get services.
     * @return list of services (unmodifiable)
     */
    public List<ServiceReference> getServices() {
        return services;
    }

    /**
     * Bean that represents a UPNP-IGD root XML service tag.
     */
    public final class ServiceReference {

        private final String serviceType;
        private final String serviceId;
        private final URI controlUrl;
        private final URI scpdUrl;

        /**
         * Constructs a {@link UpnpIgdServiceReference} object.
         *
         * @param deviceUri URL of device -- grabbed via probing
         * @param serviceType service type
         * @param serviceId service ID
         * @param controlUrl control URL
         * @param scpdUrl SCPD URL
         * @throws MalformedURLException if any of the URL arguments are malformed
         * @throws NullPointerException if any argument other than {@code serviceType} and {@code serviceId} is {@code null}
         */
        public ServiceReference(URI deviceUri, String serviceType, String serviceId, String controlUrl, String scpdUrl)
                throws MalformedURLException {
            Validate.notNull(deviceUri);
//            Validate.notNull(serviceType); // don't care about this
//            Validate.notNull(serviceId);  // don't care about this
            Validate.notNull(controlUrl); // need this for controlling
            Validate.notNull(scpdUrl); // need this to see if port mapping actions are present with service

            this.serviceType = serviceType;
            this.serviceId = serviceId;
            this.controlUrl = deviceUri.resolve(controlUrl);
            this.scpdUrl = deviceUri.resolve(scpdUrl);
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
         * Get service ID.
         *
         * @return service ID
         */
        public String getServiceId() {
            return serviceId;
        }

        /**
         * Get control URL.
         *
         * @return control URL
         */
        public URI getControlUrl() {
            return controlUrl;
        }

        /**
         * Get SCPD URL.
         *
         * @return SCPD URL
         */
        public URI getScpdUrl() {
            return scpdUrl;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.serviceType);
            hash = 19 * hash + Objects.hashCode(this.serviceId);
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
            if (!Objects.equals(this.serviceId, other.serviceId)) {
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
            return "ServiceReference{" + ", serviceType=" + serviceType + ", serviceId=" + serviceId + ", controlUrl="
                    + controlUrl + ", scpdUrl=" + scpdUrl + '}';
        }
    }
}
