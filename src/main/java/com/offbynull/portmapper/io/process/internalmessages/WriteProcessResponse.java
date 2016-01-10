package com.offbynull.portmapper.io.process.internalmessages;

import com.offbynull.portmapper.io.network.internalmessages.*;
import org.apache.commons.lang3.Validate;

public final class WriteProcessResponse extends IdentifiableProcessResponse {
    private int amountWritten;

    public WriteProcessResponse(int id, int amountWritten) {
        super(id);
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    public int getAmountWritten() {
        return amountWritten;
    }
    
}
