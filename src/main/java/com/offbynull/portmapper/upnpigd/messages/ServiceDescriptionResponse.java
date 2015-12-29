package com.offbynull.portmapper.upnpigd.messages;

import com.offbynull.portmapper.common.TextUtils;
import com.offbynull.portmapper.upnpigd.UpnpIgdService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP service description response.
 *
 * @author Kasra Faghihi
 */
public final class ServiceDescriptionResponse extends UpnpIgdHttpResponse {

    private static final Set<String> EXPECTED_ACTION_NAMES; // unmodifiable
    static {
        Set<String> set = new HashSet<>();
        
        set.add("GetExternalIPAddress");
        set.add("GetSpecificPortMappingEntry");
        set.add("DeletePortMapping");
        set.add("AddPortMapping");
        
        EXPECTED_ACTION_NAMES = Collections.unmodifiableSet(set);
    }
    
    private List<ServiceDescription> descriptions;

    /**
     * Constructs a {@link ServiceDescriptionResponse} object by parsing a buffer.
     *
     * @param buffer buffer containing response data
     */
    public ServiceDescriptionResponse(byte[] buffer) {
        super(buffer);

        String content = getContent();

        List<String> actions = TextUtils.findAllBlocks(content, "<actions>", "</actions>", true);
        Set<String> missingActionNames = new HashSet<>(EXPECTED_ACTION_NAMES);
        for (String action : actions) {
            String name = TextUtils.findFirstBlock(action, "<name>", "</name>", true);
            if (name == null) {
                continue;
            }

            Iterator<String> it = missingActionNames.iterator();
            while (it.hasNext()) {
                String missingActionName = it.next();
                if (missingActionName.equalsIgnoreCase(name)) {
                    it.remove();
                    break;
                }
            }
        }

        // Make sure no action names are missing
        Validate.isTrue(missingActionNames.isEmpty());

        // Get state variables
        Range<Long> leaseDurationRange = Range.between(0x0L, 0xFFFFFFFFL);
        Range<Long> externalPortRange = Range.between(0x0L, 0xFFFFL);
        List<String> stateVars = TextUtils.findAllBlocks(content, "<stateVariable>", "</stateVariable>", true);
        for (String stateVar : stateVars) {
            String name = TextUtils.findFirstBlock(stateVar, "<name>", "</name>", true);
            if (name == null) {
                continue;
            }
            name = name.trim();

            if (name.equalsIgnoreCase("PortMappingLeaseDuration")) {
                leaseDurationRange = extractRange(stateVar, 0L, 0xFFFFFFFFL);
            } else if (name.equalsIgnoreCase("ExternalPort")) {
                externalPortRange = extractRange(stateVar, 0L, 0xFFFFL);
            }
        }

        // Generate service
        ServiceDescription desc = new ServiceDescription(leaseDurationRange, externalPortRange);
        descriptions.add(desc);

        this.descriptions = Collections.unmodifiableList(this.descriptions);
    }

    private static Range<Long> extractRange(String block, long absoluteMin, long absoluteMax) {
        Validate.validState(block != null);
        Validate.validState(absoluteMin < absoluteMax);
        
        String allowedRangeBlock = TextUtils.findFirstBlock(block, "<allowedValueRange>", "</allowedValueRange>", true);
        if (allowedRangeBlock == null) {
            return null;
        }
        allowedRangeBlock = allowedRangeBlock.trim();

        String minStr = TextUtils.findFirstBlock(allowedRangeBlock, "<minimum>", "</minimum>", true);
        long min;
        if (minStr == null) {
            min = absoluteMin;
        } else {
            try {
                min = Long.valueOf(minStr.trim());
                min = Math.max(absoluteMin, min);
            } catch (NumberFormatException nfe) {
                min = absoluteMin;
            }
        }

        String maxStr = TextUtils.findFirstBlock(allowedRangeBlock, "<maximum>", "</maximum>", true);
        long max;
        if (maxStr == null) {
            max = absoluteMax;
        } else {
            try {
                max = Long.valueOf(maxStr.trim());
                max = Math.max(absoluteMax, max);
            } catch (NumberFormatException nfe) {
                max = absoluteMax;
            }
        }

        Range<Long> ret = Range.between(min, max);
        
        return ret;
    }

    /**
     * Get service descriptions.
     *
     * @return list of services (unmodifiable)
     */
    public List<ServiceDescription> getServiceDescriptions() {
        return descriptions;
    }

    /**
     * Bean that identifies which type of UPNP-IGD service is being described. Two types of services are identified: port mapping and
     * pinhole firewall.
     * <p>
     * Port mapping services (e.g. InternetGatewayDevice:1, InternetGatewayDevice:2, WANIPConnection:1, WANIPConnection:2, etc..) are said
     * to support IPv4. While pinhole services (e.g. WANIPv6FirewallControl:1) are said to support IPv6.
     * <p>
     * Action names are used to identify which type of service is being referenced. This is done as a future-proofing mechanism in case
     * new/upgraded service types are announced that offer the same functionality.
     */
    public final class ServiceDescription {

        private Range<Long> leaseDurationRange;
        private Range<Long> externalPortRange;

        /**
         * Constructs a {@link UpnpIgdService} object.
         *
         * @param leaseDurationRange lease duration range, if it was encountered
         * @param externalPortRange external port duration range, if it was encountered
         */
        public ServiceDescription(Range<Long> leaseDurationRange, Range<Long> externalPortRange) {
            this.leaseDurationRange = leaseDurationRange;
            this.externalPortRange = externalPortRange;
        }

        /**
         * Get lease duration range.
         *
         * @return lease duration range (can be {@code null)}
         */
        public Range<Long> getLeaseDurationRange() {
            return leaseDurationRange;
        }

        /**
         * Get external port range.
         *
         * @return external port range (can be {@code null)}
         */
        public Range<Long> getExternalPortRange() {
            return externalPortRange;
        }

        @Override
        public int hashCode() {
            int hash = 5;
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
            final ServiceDescription other = (ServiceDescription) obj;
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
            return "ServiceDescription{" + ", leaseDurationRange=" + leaseDurationRange + ", externalPortRange=" + externalPortRange + '}';
        }
    }
}
