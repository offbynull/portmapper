/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.gateways.network.internalmessages;

import com.offbynull.portmapper.gateway.Bus;
import java.net.InetAddress;
import org.apache.commons.lang3.Validate;

/**
 * Create a TCP socket. Possible responses are {@link CreateTcpNetworkResponse} and {@link IdentifiableErrorNetworkResponse}). Shortly after
 * creation, the socket will connect and a {@link ConnectedTcpNetworkNotification} will be sent out to the creator.
 * @author Kasra Faghihi
 */
public final class CreateTcpNetworkRequest extends IdentifiableNetworkRequest {

    private Bus responseBus;
    private InetAddress sourceAddress;
    private InetAddress destinationAddress;
    private int destinationPort;

    /**
     * Constructs a {@link CreateTcpNetworkRequest} object.
     * @param id id of socket
     * @param responseBus bus to send responses/notifications to for the created socket 
     * @param sourceAddress source address of the socket to be created
     * @param destinationAddress destination address of the socket to be created
     * @param destinationPort destination port of the socket to be created
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code 1 > destinationPort > 65535}
     */
    public CreateTcpNetworkRequest(int id, Bus responseBus, InetAddress sourceAddress, InetAddress destinationAddress,
            int destinationPort) {
        super(id);
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

    @Override
    public String toString() {
        return "CreateTcpNetworkRequest{" + "responseBus=" + responseBus + ", sourceAddress=" + sourceAddress + ", destinationAddress="
                + destinationAddress + ", destinationPort=" + destinationPort + '}';
    }
}
