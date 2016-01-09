package com.offbynull.portmapper.io.internalmessages;

import org.apache.commons.lang3.Validate;

public final class WriteUdpNetworkResponse extends IdentifiableNetworkResponse {
    private int amountWritten;

    public WriteUdpNetworkResponse(int id, int amountWritten) {
        super(id);
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    public int getAmountWritten() {
        return amountWritten;
    }
    
}
