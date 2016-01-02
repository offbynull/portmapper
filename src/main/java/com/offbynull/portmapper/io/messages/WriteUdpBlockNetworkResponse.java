package com.offbynull.portmapper.io.messages;

import org.apache.commons.lang3.Validate;

public final class WriteUdpBlockNetworkResponse implements NetworkResponse {
    private int amountWritten;

    public WriteUdpBlockNetworkResponse(int amountWritten) {
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    public int getAmountWritten() {
        return amountWritten;
    }
    
}
