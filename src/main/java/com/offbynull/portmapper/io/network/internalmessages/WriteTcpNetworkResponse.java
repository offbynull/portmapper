package com.offbynull.portmapper.io.network.internalmessages;

import org.apache.commons.lang3.Validate;

public final class WriteTcpNetworkResponse extends IdentifiableNetworkResponse {
    private int amountWritten;

    public WriteTcpNetworkResponse(int id, int amountWritten) {
        super(id);
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    public int getAmountWritten() {
        return amountWritten;
    }
    
}
