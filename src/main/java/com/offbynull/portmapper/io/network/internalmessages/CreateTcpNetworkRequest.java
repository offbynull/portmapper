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

/**
 * Create a TCP socket. Possible responses are {@link CreateTcpNetworkResponse} and {@link IdentifiableErrorNetworkResponse}). Shortly after
 * creation, the socket will connect and a {@link ConnectedTcpNetworkNotification} will be sent out to the creator.
 * @author Kasra Faghihi
 */
public final class CreateTcpNetworkRequest implements NetworkRequest {

    private Bus responseBus;
    private InetAddress sourceAddress;
    private InetAddress destinationAddress;
    private int destinationPort;

    /**
     * Constructs a {@link CreateTcpNetworkRequest} object.
     * @param responseBus bus to send responses/notifications to for the created socket 
     * @param sourceAddress source address of the socket to be created
     * @param destinationAddress destination address of the socket to be created
     * @param destinationPort destination port of the socket to be created
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code 1 > destinationPort > 65535}
     */
    public CreateTcpNetworkRequest(Bus responseBus, InetAddress sourceAddress, InetAddress destinationAddress, int destinationPort) {
        Validate.notNull(responseBus);
        Validate.notNull(sourceAddress);
        Validate.notNull(destinationAddress);
        Validate.inclusiveBetween(1, 65535, destinationPort);

        this.responseBus = responseBus;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    /**
     * Bus to send responses/notifications to for the created socket.
     * @return response bus
     */
    public Bus getResponseBus() {
        return responseBus;
    }

    /**
     * Source address of the socket to be created.
     * @return source address
     */
    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    /**
     * Destination address of the socket to be created.
     * @return destination address
     */
    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * Destination port of the socket to be created.
     * @return destination port
     */
    public int getDestinationPort() {
        return destinationPort;
    }
}
