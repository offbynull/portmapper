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
package com.offbynull.portmapper.gateways.process.internalmessages;

import java.util.Arrays;

/**
 * Send data to a process. Possible responses are {@link WriteProcessResponse} and {@link IdentifiableErrorProcessResponse}).
 * @author Kasra Faghihi
 */
public final class WriteProcessRequest extends IdentifiableProcessRequest {
    private byte[] data;

    /**
     * Constructs a {@link WriteProcessRequest} object.
     * @param id id of process
     * @param data send data
     * @throws NullPointerException if any argument is {@code null}
     */
    public WriteProcessRequest(int id, byte[] data) {
        super(id);
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
        return "WriteProcessRequest{super=" + super.toString() + "data=" + Arrays.toString(data) + '}';
    }

}
