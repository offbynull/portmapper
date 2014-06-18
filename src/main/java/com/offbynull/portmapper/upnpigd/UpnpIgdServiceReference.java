/*
 * Copyright (c) 2013-2014, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.upnpigd;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Bean that represents a UPNP-IGD root XML service tag.
 * @author Kasra Faghihi
 */
public final class UpnpIgdServiceReference {
    private final UpnpIgdDevice device;
    private final String serviceType;
    private final String serviceId;
    private final URI controlUrl;
    private final URI scpdUrl;

    /**
     * Constructs a {@link UpnpIgdServiceReference} object.
     * @param device device
     * @param serviceType service type
     * @param serviceId service ID
     * @param controlUrl control URL
     * @param scpdUrl SCPD URL
     * @throws MalformedURLException if any of the URL arguments are malformed
     * @throws NullPointerException if any argument is {@code null}
     */
    public UpnpIgdServiceReference(UpnpIgdDevice device, String serviceType, String serviceId, String controlUrl, String scpdUrl)
            throws MalformedURLException {
        Validate.notNull(device);
        Validate.notNull(serviceType);
        Validate.notNull(serviceId);
        Validate.notNull(controlUrl);
        Validate.notNull(scpdUrl);
        
        URI baseUri = device.getUrl();
        
        this.device = device;
        this.serviceType = serviceType;
        this.serviceId = serviceId;
        this.controlUrl = baseUri.resolve(controlUrl);
        this.scpdUrl = baseUri.resolve(scpdUrl);
    }

    /**
     * Get device.
     * @return device
     */
    public UpnpIgdDevice getDevice() {
        return device;
    }

    /**
     * Get service type.
     * @return service type
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Get service ID.
     * @return service ID
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Get control URL.
     * @return control URL
     */
    public URI getControlUrl() {
        return controlUrl;
    }

    /**
     * Get SCPD URL.
     * @return SCPD URL
     */
    public URI getScpdUrl() {
        return scpdUrl;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.device);
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
        final UpnpIgdServiceReference other = (UpnpIgdServiceReference) obj;
        if (!Objects.equals(this.device, other.device)) {
            return false;
        }
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
        return "UpnpIgdService{" + "device=" + device + ", serviceType=" + serviceType + ", serviceId=" + serviceId + ", controlUrl="
                + controlUrl + ", scpdUrl=" + scpdUrl + '}';
    }
}
