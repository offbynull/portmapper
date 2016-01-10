package com.offbynull.portmapper.io.internalmessages;

import org.apache.commons.lang3.Validate;

public final class WriteTcpIoResponse extends IdentifiableIoResponse {
    private int amountWritten;

    public WriteTcpIoResponse(int id, int amountWritten) {
        super(id);
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    public int getAmountWritten() {
        return amountWritten;
    }
    
}
