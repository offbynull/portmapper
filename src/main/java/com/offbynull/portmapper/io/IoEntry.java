package com.offbynull.portmapper.io;

import com.offbynull.portmapper.common.Bus;
import org.apache.commons.lang3.Validate;

abstract class IoEntry {
    private int id;
    private Bus responseBus;
    
    public IoEntry(int id, Bus responseBus) {
        Validate.notNull(responseBus);
        
        this.id = id;
        this.responseBus = responseBus;
    }

    public int getId() {
        return id;
    }

    public Bus getResponseBus() {
        return responseBus;
    }
}
