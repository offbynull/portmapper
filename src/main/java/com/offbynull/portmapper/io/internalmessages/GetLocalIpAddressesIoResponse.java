package com.offbynull.portmapper.io.internalmessages;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

public final class GetLocalIpAddressesIoResponse implements IoResponse {
    private Set<InetAddress> localAddresses;

    public GetLocalIpAddressesIoResponse(Set<InetAddress> localAddresses) {
        Validate.notNull(localAddresses);
        this.localAddresses = new HashSet<>(localAddresses);
    }

    public Set<InetAddress> getLocalAddresses() {
        return localAddresses;
    }
    
}
