package com.offbynull.portmapper.io.messages;

import org.apache.commons.lang3.Validate;

public final class WriteTcpBlockNetworkResponse implements NetworkResponse {
    private int amountWritten;

    public WriteTcpBlockNetworkResponse(int amountWritten) {
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    public int getAmountWritten() {
        return amountWritten;
    }
    
}
