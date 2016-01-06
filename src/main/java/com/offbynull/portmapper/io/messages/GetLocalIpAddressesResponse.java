package com.offbynull.portmapper.io.messages;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

public final class GetLocalIpAddressesResponse implements NetworkResponse {
    private Set<InetAddress> localAddresses;

    public GetLocalIpAddressesResponse(Set<InetAddress> localAddresses) {
        Validate.notNull(localAddresses);
        this.localAddresses = new HashSet<>(localAddresses);
    }

    public Set<InetAddress> getLocalAddresses() {
        return localAddresses;
    }
    
}
