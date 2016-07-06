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
 * Represents a NAT-PMP request.
 * @author Kasra Faghihi
 */
public abstract class NatPmpRequest implements NatPmpMessage {
    private static final int HEADER_LENGTH = 2;
    
    private final int op;

    NatPmpRequest(int op) {
        // Checks are redundant, but keep anyways to be safe.
        this.op = op;
        
        validateState();
    }

    NatPmpRequest(byte[] buffer) {
        Validate.notNull(buffer);
        Validate.isTrue(buffer.length >= HEADER_LENGTH);
        
        int offset = 0;

        int version = buffer[offset] & 0xFF;
        Validate.isTrue(version == NAT_PMP_VERSION); // check pcp version
        offset++;
        
        op = buffer[offset] & 0xFF;
        offset++;
        
        validateState();
    }
    
    private void validateState() {
        Validate.inclusiveBetween(0, 127, op);
    }

    @Override
    public final int getOp() {
        return op;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public String toString() {
        return "NatPmpRequest{" + "op=" + op + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.op;
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
        final NatPmpRequest other = (NatPmpRequest) obj;
        if (this.op != other.op) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
