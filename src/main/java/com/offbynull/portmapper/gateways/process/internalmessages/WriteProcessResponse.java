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

import org.apache.commons.lang3.Validate;

/**
 * Sent data through a process. Successful response to {@link WriteProcessResponse}.
 * @author Kasra Faghihi
 */
public final class WriteProcessResponse extends IdentifiableProcessResponse {
    private int amountWritten;

    /**
     * Constructs a {@link WriteProcessResponse} object.
     * @param id id of process
     * @param amountWritten amount of data written out (in bytes)
     */
    public WriteProcessResponse(int id, int amountWritten) {
        super(id);
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amountWritten);
        this.amountWritten = amountWritten;
    }

    /**
     * Amount of data written out.
     * @return bytes written out
     */
    public int getAmountWritten() {
        return amountWritten;
    }

    @Override
    public String toString() {
        return "WriteProcessResponse{super=" + super.toString() + "amountWritten=" + amountWritten + '}';
    }
    
}
