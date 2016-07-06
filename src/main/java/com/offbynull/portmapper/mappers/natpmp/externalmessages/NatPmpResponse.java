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
package com.offbynull.portmapper.mappers.natpmp.externalmessages;

import static com.offbynull.portmapper.mappers.natpmp.externalmessages.InternalUtils.NAT_PMP_VERSION;
import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP response.
 * @author Kasra Faghihi
 */
public abstract class NatPmpResponse implements NatPmpMessage {
    private static final int HEADER_LENGTH = 8;
    
    private int op;
    private int resultCode;
    private long secondsSinceStartOfEpoch;

    NatPmpResponse(int op, int resultCode, long secondsSinceStartOfEpoch) {
        this.op = op;
        this.resultCode = resultCode;
        this.secondsSinceStartOfEpoch = secondsSinceStartOfEpoch;
        
        validateState();
    }

    NatPmpResponse(byte[] buffer) {
        Validate.notNull(buffer);
        Validate.isTrue(buffer.length >= HEADER_LENGTH);
        
        int offset = 0;

        int version = buffer[offset] & 0xFF;
        Validate.isTrue(version == NAT_PMP_VERSION); // check pcp version
        offset++;
        
        op = buffer[offset] & 0xFF;
        offset++;

        resultCode = InternalUtils.bytesToShort(buffer, offset);
        offset += 2;
        
        secondsSinceStartOfEpoch = InternalUtils.bytesToInt(buffer, offset) & 0xFFFFFFFFL;
        offset += 4;
        
        validateState();
    }

    private void validateState() {
        Validate.inclusiveBetween(128, 255, op);
        Validate.inclusiveBetween(0, 65535, resultCode);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, secondsSinceStartOfEpoch);
    }

    @Override
    public final int getOp() {
        return op;
    }
        
    /**
     * Get result code. 0 means success.
     * @return result code
     */
    public final int getResultCode() {
        return resultCode;
    }

    /**
     * Get the number of seconds since the device's port mapping table was initialized/reset.
     * @return number of seconds the device's port mapping table has been up (up to {@code 0xFFFFFFFFL})
     */
    public final long getSecondsSinceStartOfEpoch() {
        return secondsSinceStartOfEpoch;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public String toString() {
        return "NatPmpResponse{" + "op=" + op + ", resultCode=" + resultCode + ", secondsSinceStartOfEpoch=" + secondsSinceStartOfEpoch
                + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.op;
        hash = 83 * hash + this.resultCode;
        hash = 83 * hash + (int) (this.secondsSinceStartOfEpoch ^ (this.secondsSinceStartOfEpoch >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NatPmpResponse other = (NatPmpResponse) obj;
        if (this.op != other.op) {
            return false;
        }
        if (this.resultCode != other.resultCode) {
            return false;
        }
        if (this.secondsSinceStartOfEpoch != other.secondsSinceStartOfEpoch) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
