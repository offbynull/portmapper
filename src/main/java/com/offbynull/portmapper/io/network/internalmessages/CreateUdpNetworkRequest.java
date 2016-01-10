/*
 * Copyright (c) 2013-2016, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.portmapper.io.network.internalmessages;

import com.offbynull.portmapper.Bus;
import java.net.InetAddress;
import org.apache.commons.lang3.Validate;

public final class CreateUdpNetworkRequest implements NetworkRequest {
    private Bus responseBus;
    private InetAddress sourceAddress;

    public CreateUdpNetworkRequest(Bus responseBus, InetAddress sourceAddress) {
        Validate.notNull(responseBus);
        Validate.notNull(sourceAddress);
        this.responseBus = responseBus;
        this.sourceAddress = sourceAddress;
    }

    public Bus getResponseBus() {
        return responseBus;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }
}
