package com.offbynull.portmapper.io.messages;

import com.offbynull.portmapper.common.Bus;
import org.apache.commons.lang3.Validate;

public final class GetLocalIpAddressesRequest implements NetworkRequest {
    private Bus responseBus;

    public GetLocalIpAddressesRequest(Bus responseBus) {
        Validate.notNull(responseBus);

        this.responseBus = responseBus;
    }

    public Bus getResponseBus() {
        return responseBus;
    }
}
