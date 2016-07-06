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
import org.apache.commons.lang3.Validate;

/**
 * Process has received data.
 * @author Kasra Faghihi
 */
public final class ReadProcessNotification extends IdentifiableProcessNotification {
    private byte[] data;
    private ReadType readType;

    /**
     * Constructs a {@link ReadProcessNotification} object.
     * @param id id of process
     * @param data received data
     * @param readType stream which received the data (STDOUT or STDERR)
     * @throws NullPointerException if any argument is {@code null}
     */
    public ReadProcessNotification(int id, byte[] data, ReadType readType) {
        super(id);
        Validate.notNull(data);
        Validate.notNull(readType);
        this.data = Arrays.copyOf(data, data.length);
        this.readType = readType;
    }

    /**
     * Get data.
     * @return data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Get stream which received data.
     * @return stream which received data
     */
    public ReadType getReadType() {
        return readType;
    }

    @Override
    public String toString() {
        return "ReadProcessNotification{super=" + super.toString() + "data=" + Arrays.toString(data) + ", readType=" + readType + '}';
    }

}
