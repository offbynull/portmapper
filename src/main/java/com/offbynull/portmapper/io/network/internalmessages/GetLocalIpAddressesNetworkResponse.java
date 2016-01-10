package com.offbynull.portmapper.io.network.internalmessages;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

public final class GetLocalIpAddressesNetworkResponse implements NetworkResponse {
    private Set<InetAddress> localAddresses;

    public GetLocalIpAddressesNetworkResponse(Set<InetAddress> localAddresses) {
        Validate.notNull(localAddresses);
        this.localAddresses = new HashSet<>(localAddresses);
    }

    public Set<InetAddress> getLocalAddresses() {
        return localAddresses;
    }
    
}
