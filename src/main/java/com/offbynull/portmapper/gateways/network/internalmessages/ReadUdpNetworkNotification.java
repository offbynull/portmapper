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

import java.net.InetSocketAddress;
import java.util.Arrays;
import org.apache.commons.lang3.Validate;

/**
 * UDP socket has received data.
 * @author Kasra Faghihi
 */
public final class ReadUdpNetworkNotification extends IdentifiableNetworkNotification {
    private InetSocketAddress localAddress;
    private InetSocketAddress remoteAddress;
    private byte[] data;

    /**
     * Constructs a {@link ReadUdpNetworkNotification} object.
     * @param id id of socket
     * @param localAddress address sent to
     * @param remoteAddress address sent from
     * @param data received data
     * @throws NullPointerException if any argument is {@code null}
     */
    public ReadUdpNetworkNotification(int id, InetSocketAddress localAddress, InetSocketAddress remoteAddress, byte[] data) {
        super(id);
        Validate.notNull(localAddress);
        Validate.notNull(remoteAddress);
        Validate.notNull(data);
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Get local address.
     * @return local address
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Get remote address.
     * @return remote address
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Get data.
     * @return data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "ReadUdpNetworkNotification{super=" + super.toString() + "localAddress=" + localAddress + ", remoteAddress=" + remoteAddress
                + ", data=" + Arrays.toString(data) + '}';
    }

}
