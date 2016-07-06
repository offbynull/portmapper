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

import java.util.Arrays;
import org.apache.commons.lang3.Validate;

/**
 * Send data to a TCP socket. Possible responses are {@link WriteTcpNetworkResponse} and {@link IdentifiableErrorNetworkResponse}).
 * @author Kasra Faghihi
 */
public final class WriteTcpNetworkRequest extends IdentifiableNetworkRequest {
    private byte[] data;

    /**
     * Constructs a {@link WriteTcpNetworkRequest} object.
     * @param id id of socket
     * @param data send data
     * @throws NullPointerException if any argument is {@code null}
     */
    public WriteTcpNetworkRequest(int id, byte[] data) {
        super(id);
        Validate.notNull(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Get send data.
     * @return send data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "WriteTcpNetworkRequest{super=" + super.toString() + "data=" + Arrays.toString(data) + '}';
    }

}
