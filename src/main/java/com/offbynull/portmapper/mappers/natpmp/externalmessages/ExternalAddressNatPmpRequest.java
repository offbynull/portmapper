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

import org.apache.commons.lang3.Validate;

/**
 * Represents a NAT-PMP external address request. From the RFC:
 * <pre>
 * 3.2.  Determining the External Address
 * 
 *    To determine the external address, the client behind the NAT sends
 *    the following UDP payload to port 5351 of the configured gateway
 *    address:
 * 
 *     0                   1
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Vers = 0      | OP = 0        |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * @author Kasra Faghihi
 */
public final class ExternalAddressNatPmpRequest extends NatPmpRequest {

    private static final int LENGTH = 2;
    private static final int OP = 0;

    /**
     * Construct a {@link ExternalAddressNatPmpRequest} object.
     * @param buffer buffer containing NAT-PMP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} isn't the right size or is malformed ({@code op != 0 || version != 0 ||
     * 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || 0L > lifetime > 0xFFFFFFFFL})
     */
    public ExternalAddressNatPmpRequest(byte[] buffer) {
        super(buffer);

        Validate.notNull(buffer);
        Validate.isTrue(buffer.length == LENGTH);

        Validate.isTrue(getOp() == OP);
    }

    /**
     * Construct a {@link ExternalAddressNatPmpRequest} object.
     */
    public ExternalAddressNatPmpRequest() {
        super(OP);
    }

    @Override
    public byte[] dump() {
        return new byte[] {0, OP};
    }

    @Override
    public String toString() {
        return "ExternalAddressNatPmpRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
