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

import java.util.Objects;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;

/**
 * Bean that represents a UPNP-IGD serviceReference XML. Specifically represents portions of a port mapping serviceReference (e.g.
 * WANIPConnection).
 * @author Kasra Faghihi
 */
public final class UpnpIgdService {

    private UpnpIgdServiceReference serviceReference;

    private Range<Long> leaseDurationRange;
    private Range<Long> externalPortRange;

    /**
     * Constructs a {@link UpnpIgdService} object.
     * @param serviceReference serviceReference reference
     * @param leaseDurationRange lease duration range, if it was encountered
     * @param externalPortRange external port duration range, if it was encountered
     * @throws NullPointerException if {@code serviceReference} is {@code null}
     */
    public UpnpIgdService(UpnpIgdServiceReference serviceReference, Range<Long> leaseDurationRange, Range<Long> externalPortRange) {
        Validate.notNull(serviceReference);

        this.serviceReference = serviceReference;
        this.leaseDurationRange = leaseDurationRange;
        this.externalPortRange = externalPortRange;
    }

    /**
     * Get service reference.
     * @return service reference
     */
    public UpnpIgdServiceReference getServiceReference() {
        return serviceReference;
    }

    /**
     * Get lease duration range.
     * @return lease duration range (can be {@code null)}
     */
    public Range<Long> getLeaseDurationRange() {
        return leaseDurationRange;
    }

    /**
     * Get external port range.
     * @return external port range (can be {@code null)}
     */
    public Range<Long> getExternalPortRange() {
        return externalPortRange;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.serviceReference);
        hash = 79 * hash + Objects.hashCode(this.leaseDurationRange);
        hash = 79 * hash + Objects.hashCode(this.externalPortRange);
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
        final UpnpIgdService other = (UpnpIgdService) obj;
        if (!Objects.equals(this.serviceReference, other.serviceReference)) {
            return false;
        }
        if (!Objects.equals(this.leaseDurationRange, other.leaseDurationRange)) {
            return false;
        }
        if (!Objects.equals(this.externalPortRange, other.externalPortRange)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UpnpIgdService{" + "serviceReference=" + serviceReference + ", leaseDurationRange=" + leaseDurationRange
                + ", externalPortRange=" + externalPortRange + '}';
    }

}
