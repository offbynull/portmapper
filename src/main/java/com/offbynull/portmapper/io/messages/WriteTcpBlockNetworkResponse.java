package com.offbynull.portmapper.io.messages;

import org.apache.commons.lang3.Validate;

public final class WriteTcpBlockNetworkResponse implements NetworkResponse {
    private int id;
    private int amountWritten;

    public WriteTcpBlockNetworkResponse(int id, int amountWritten) {
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.id = id;
        this.amountWritten = amountWritten;
    }

    public int getId() {
        return id;
    }

    public int getAmountWritten() {
        return amountWritten;
    }
    
}
