package com.offbynull.portmapper.io.network.internalmessages;

import com.offbynull.portmapper.common.Bus;
import org.apache.commons.lang3.Validate;

public final class GetLocalIpAddressesNetworkRequest implements NetworkRequest {
    private Bus responseBus;

    public GetLocalIpAddressesNetworkRequest(Bus responseBus) {
        Validate.notNull(responseBus);

        this.responseBus = responseBus;
    }

    public Bus getResponseBus() {
        return responseBus;
    }
}
