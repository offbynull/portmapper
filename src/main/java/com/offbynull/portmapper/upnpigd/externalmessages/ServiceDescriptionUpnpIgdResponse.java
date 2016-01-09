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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP service description response.
 * <p>
 * Two types of services are identified: port mapping and pinhole firewall. Action names are used to identify which type of service is being
 * offered. This is done as a future-proofing mechanism in case new/upgraded service types are announced that offer the same functionality.
 * <p>
 * Port mapping services (e.g. InternetGatewayDevice:1, InternetGatewayDevice:2, WANIPConnection:1, WANIPConnection:2, etc..) are said
 * to support IPv4 only. While pinhole services (e.g. WANIPv6FirewallControl:1) are said to support IPv6 only.
 * <p>
 * Specifications are documented at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class ServiceDescriptionUpnpIgdResponse extends UpnpIgdHttpResponse {
    
    private Map<ServiceType, IdentifiedService> identifiedServices;

    /**
     * Constructs a {@link ServiceDescriptionResponse} object by parsing a buffer.
     * @param buffer buffer containing response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} was malformed
     */
    public ServiceDescriptionUpnpIgdResponse(byte[] buffer) {
        super(buffer);
        
        Validate.isTrue(isResponseSuccessful());

        String content = getContent();

        
        Map<ServiceType, IdentifiedService> descs = new HashMap<>();

        // check for new portmapper version first -- if we checked for the old one first it would pass even if its the new one because the
        // new version contains all the old version methods
        IdentifiedService portMappingDescription;
        if ((portMappingDescription = getAsNewPortMappingService(content)) != null) {
            descs.put(ServiceType.NEW_PORT_MAPPER, portMappingDescription);
        } else if ((portMappingDescription = getAsOldPortMappingService(content)) != null) {
            descs.put(ServiceType.OLD_PORT_MAPPER, portMappingDescription);
        }
        
        IdentifiedService firewallDescription = getAsFirewallService(content);
        if (firewallDescription != null) {
            descs.put(ServiceType.FIREWALL, firewallDescription);
        }
        
        identifiedServices = Collections.unmodifiableMap(descs);
    }

    /**
     * Return services that were identified.
     * <p>
     * Identification happens by matching action names against those in the UPnP-IGD specifications. If the returned list has more
     * than 1 element, it means this UPnP-IGD service includes action names for both IPv4 portmapping and IPv6 firewall. This could happen
     * if the service being accessed wasn't implemented properly (or possibly a newer standard that wasn't around when this library was
     * written). Either way, the service may still be usable, but it's up to the user to decide which actions to use (IPv4 portmapping
     * actions or IPv6 firewall actions).
     * @return service descriptions (almost always have only 1 key in it / will never have more than 2 keys)
     */
    public Map<ServiceType, IdentifiedService> getIdentifiedServices() {
        return identifiedServices;
    }

    private static IdentifiedService getAsOldPortMappingService(String content) {
        List<String> actionBlocks = TextUtils.findAllBlocks(content, "<action>", "</action>", true);
        List<String> stateVarBlocks = TextUtils.findAllBlocks(content, "<stateVariable>", "</stateVariable>", true);

        String getExtIpActionBlock = searchBlocksForTag(actionBlocks, "name", "GetExternalIPAddress");
        String getMappingActionBlock = searchBlocksForTag(actionBlocks, "name", "GetSpecificPortMappingEntry");
        String deleteMappingActionBlock = searchBlocksForTag(actionBlocks, "name", "DeletePortMapping");
        String addMappingActionBlock = searchBlocksForTag(actionBlocks, "name", "AddPortMapping");
        
        if (getExtIpActionBlock == null || getMappingActionBlock == null || deleteMappingActionBlock == null
                || addMappingActionBlock == null) {
            return null;
        }
        
        Range<Long> externalPortRange = getAllowedValueRange(
                addMappingActionBlock,
                stateVarBlocks,
                "NewExternalPort",
                Range.between(0L, 65535L)); // based on docs
        Range<Long> leaseTimeRange = getAllowedValueRange(
                addMappingActionBlock,
                stateVarBlocks,
                "NewLeaseDuration",
                Range.between(1L, 604800L)); // based on docs
        
        return new IdentifiedService(leaseTimeRange, externalPortRange);
    }

    private static IdentifiedService getAsNewPortMappingService(String content) {
        List<String> actionBlocks = TextUtils.findAllBlocks(content, "<action>", "</action>", true);
        List<String> stateVarBlocks = TextUtils.findAllBlocks(content, "<stateVariable>", "</stateVariable>", true);

        String getExtIpActionBlock = searchBlocksForTag(actionBlocks, "name", "GetExternalIPAddress");
        String getMappingActionBlock = searchBlocksForTag(actionBlocks, "name", "GetSpecificPortMappingEntry");
        String deleteMappingActionBlock = searchBlocksForTag(actionBlocks, "name", "DeletePortMapping");
        String addAnyMappingActionBlock = searchBlocksForTag(actionBlocks, "name", "AddAnyPortMapping");
        
        if (getExtIpActionBlock == null || getMappingActionBlock == null || deleteMappingActionBlock == null
                || addAnyMappingActionBlock == null) {
            return null;
        }
        
        Range<Long> externalPortRange = getAllowedValueRange(
                addAnyMappingActionBlock,
                stateVarBlocks,
                "NewExternalPort",
                Range.between(0L, 65535L)); // based on docs
        Range<Long> leaseTimeRange = getAllowedValueRange(
                addAnyMappingActionBlock,
                stateVarBlocks,
                "NewLeaseDuration",
                Range.between(1L, 604800L)); // based on docs
        
        return new IdentifiedService(leaseTimeRange, externalPortRange);
    }
    
    private static IdentifiedService getAsFirewallService(String content) {
        List<String> actionBlocks = TextUtils.findAllBlocks(content, "<action>", "</action>", true);
        List<String> stateVarBlocks = TextUtils.findAllBlocks(content, "<stateVariable>", "</stateVariable>", true);
        

//       "GetFirewallStatus" required -- but don't bother checking because we don't care about it... technically this action is used to see
//                                       if the firewall is active and accepting inbound
        String addPinholeActionBlock = searchBlocksForTag(actionBlocks, "name", "AddPinhole");
        String updatePinholeActionBlock = searchBlocksForTag(actionBlocks, "name", "UpdatePinhole");
        String deletePinholeActionBlock = searchBlocksForTag(actionBlocks, "name", "DeletePinhole");
        
        if (addPinholeActionBlock == null || updatePinholeActionBlock == null || deletePinholeActionBlock == null) {
            return null;
        }
        
        Range<Long> remotePortRange = getAllowedValueRange(
                addPinholeActionBlock,
                stateVarBlocks,
                "RemotePort",
                Range.between(0L, 65535L)); // based on docs
        Range<Long> addPinholeLeaseTimeRange = getAllowedValueRange(
                addPinholeActionBlock,
                stateVarBlocks,
                "LeaseTime",
                Range.between(1L, 86400L)); // based on docs
        Range<Long> updatePinholeLeaseTimeRange = getAllowedValueRange(
                updatePinholeActionBlock,
                stateVarBlocks,
                "LeaseTime",
                Range.between(1L, 86400L)); // based on docs
        
        // in the case leasetime range for add and update are different, get the intersection (just to be safe)
        Range<Long> leaseTimeRange = addPinholeLeaseTimeRange.intersectionWith(updatePinholeLeaseTimeRange);
        
        return new IdentifiedService(leaseTimeRange, remotePortRange);
    }

    private static Range<Long> getAllowedValueRange(String actionBlock, List<String> stateVars, String expectedArgName,
            Range<Long> maxRange) {
        // get arguments
        List<String> argBlocks = TextUtils.findAllBlocks(actionBlock, "<argument>", "</argument>", true);
        
        
        // find argument
        String foundArgBlock = searchBlocksForTag(argBlocks, "name", expectedArgName);
        if (foundArgBlock == null) { // didn't find arg that shuold be there -- try to be fault tolerant and return max range
            return maxRange;
        }
        
        
        // find state variable referenced by argument
        String expectedStateVar = TextUtils.findFirstBlock(foundArgBlock, "<relatedStateVariable>", "</relatedStateVariable>", true);
        if (expectedStateVar == null) { // didn't find related state var for arg -- try to be fault tolerant and return max range
            return maxRange;
        }
        expectedStateVar = expectedStateVar.trim();
        
        
        // find state variable
        String foundStateVarBlock = searchBlocksForTag(stateVars, "name", expectedStateVar);
        if (foundStateVarBlock == null) { // didn't find statevar referenced by arg -- try to be fault tolerant and return max range
            return maxRange;
        }
        
        
        // parse allowed value range from that state variable
        String allowedRangeBlock = TextUtils.findFirstBlock(foundStateVarBlock, "<allowedValueRange>", "</allowedValueRange>", true);
        if (allowedRangeBlock == null) {
            return maxRange; // no allowed range value found, return max range
        }
        // parse minimum -- default to maxRange's min if couldn't be understood or not found
        String minStr = TextUtils.findFirstBlock(allowedRangeBlock, "<minimum>", "</minimum>", true);
        long min;
        if (minStr == null) {
            min = maxRange.getMinimum();
        } else {
            try {
                min = Long.valueOf(minStr.trim());
            } catch (NumberFormatException nfe) {
                min = maxRange.getMinimum();
            }
        }
        // parse maximum -- default to maxRange's max if couldn't be understood or not found
        String maxStr = TextUtils.findFirstBlock(allowedRangeBlock, "<maximum>", "</maximum>", true);
        long max;
        if (maxStr == null) {
            max = maxRange.getMaximum();
        } else {
            try {
                max = Long.valueOf(maxStr.trim());
            } catch (NumberFormatException nfe) {
                max = maxRange.getMaximum();
            }
        }
        // if minimum is greater than maximum OR ranges are out of bounds -- something is seriously wrong with the device if this happens,
        // return default -- trying to be fault tolerant
        if (min > max || min < maxRange.getMinimum() || max > maxRange.getMaximum()) {
            return maxRange;
        }

        return Range.between(min, max);
    }

    private static String searchBlocksForTag(List<String> blocks, String tagToSearch, String expectedValue) {
        for (String block : blocks) {
            String name = TextUtils.findFirstBlock(block, "<" + tagToSearch + ">", "</" + tagToSearch + ">", true);
            if (name == null) {
                continue;
            }

            name = name.trim();
            if (expectedValue.equalsIgnoreCase(name)) { // ignore case to be fault tolerant
                return block;
            }
        }
        
        return null;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + Objects.hashCode(this.identifiedServices);
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
        final ServiceDescriptionUpnpIgdResponse other = (ServiceDescriptionUpnpIgdResponse) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (!Objects.equals(this.identifiedServices, other.identifiedServices)) {
            return false;
        }
        return true;
    }

    /**
     * Bean that identifies which type of UPNP-IGD service is being described.
     */
    public static final class IdentifiedService {

        private Range<Long> leaseDurationRange;
        private Range<Long> externalPortRange;

        private IdentifiedService(Range<Long> leaseDurationRange, Range<Long> externalPortRange) {
            Validate.notNull(leaseDurationRange);
            Validate.notNull(externalPortRange);
            Validate.isTrue(leaseDurationRange.getMinimum() >= 0L);
            Validate.isTrue(leaseDurationRange.getMaximum() <= 0xFFFFFFFFL);
            Validate.isTrue(externalPortRange.getMinimum() >= 0L);
            Validate.isTrue(externalPortRange.getMaximum() <= 0xFFFFL);
            this.leaseDurationRange = leaseDurationRange;
            this.externalPortRange = externalPortRange;
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
            int hash = 3;
            hash = 73 * hash + Objects.hashCode(this.leaseDurationRange);
            hash = 73 * hash + Objects.hashCode(this.externalPortRange);
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
            final IdentifiedService other = (IdentifiedService) obj;
            if (!Objects.equals(this.leaseDurationRange, other.leaseDurationRange)) {
                return false;
            }
            if (!Objects.equals(this.externalPortRange, other.externalPortRange)) {
                return false;
            }
            return true;
        }

    }
    
    /**
     * Service type.
     */
    public enum ServiceType {
        /**
         * Old port mapper service (does not feature AddAnyPortMapping method).
         */
        OLD_PORT_MAPPER, // ipv4 only according to docs
        /**
         * Old port mapper service (features AddAnyPortMapping method).
         */
        NEW_PORT_MAPPER, // ipv4 only according to docs
        /**
         * Firewall service.
         */
        FIREWALL  // ipv6 only according to docs
    }
}
