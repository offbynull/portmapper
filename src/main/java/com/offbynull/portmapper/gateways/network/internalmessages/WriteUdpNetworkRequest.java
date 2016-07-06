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
 * Send packet to a UDP socket. Possible responses are {@link WriteUdpNetworkResponse} and {@link IdentifiableErrorNetworkResponse}).
 * @author Kasra Faghihi
 */
public final class WriteUdpNetworkRequest extends IdentifiableNetworkRequest {
    private InetSocketAddress remoteAddress;
    private byte[] data;

    /**
     * Constructs a {@link WriteUdpNetworkRequest} object.
     * @param id id of socket
     * @param remoteAddress outgoing socket address
     * @param data send data
     * @throws NullPointerException if any argument is {@code null}
     */
    public WriteUdpNetworkRequest(int id, InetSocketAddress remoteAddress, byte[] data) {
        super(id);
        Validate.notNull(remoteAddress);
        Validate.notNull(data);
        this.remoteAddress = remoteAddress;
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Get remote address.
     * @return remote address
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Get send packet.
     * @return send packet
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "WriteUdpNetworkRequest{super=" + super.toString() + "remoteAddress=" + remoteAddress + ", data=" + Arrays.toString(data)
                + '}';
    }

}
