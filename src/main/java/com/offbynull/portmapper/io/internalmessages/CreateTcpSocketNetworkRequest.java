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
package com.offbynull.portmapper.io.internalmessages;

import com.offbynull.portmapper.common.Bus;
import java.net.InetAddress;
import org.apache.commons.lang3.Validate;

public final class CreateTcpSocketNetworkRequest implements NetworkRequest {

    private Bus responseBus;
    private InetAddress sourceAddress;
    private InetAddress destinationAddress;
    private int destinationPort;

    public CreateTcpSocketNetworkRequest(Bus responseBus, InetAddress sourceAddress, InetAddress destinationAddress, int destinationPort) {
        Validate.notNull(responseBus);
        Validate.notNull(sourceAddress);
        Validate.notNull(destinationAddress);
        Validate.inclusiveBetween(1, 65535, destinationPort);

        this.responseBus = responseBus;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    public Bus getResponseBus() {
        return responseBus;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    public int getDestinationPort() {
        return destinationPort;
    }
}
