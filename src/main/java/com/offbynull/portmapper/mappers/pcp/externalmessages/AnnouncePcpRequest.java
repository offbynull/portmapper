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
package com.offbynull.portmapper.mappers.pcp.externalmessages;

import java.net.InetAddress;

/**
 * Represents an ANNOUNCE PCP request. This request has no op-code specific payload.
 * @author Kasra Faghihi
 */
public final class AnnouncePcpRequest extends PcpRequest {
    // FROM THE RFC:
    // The PCP ANNOUNCE Opcode requests and responses have no Opcode-specific payload (that is, the length of the Opcode-specific data is
    // zero).  The Requested Lifetime field of requests and Lifetime field of responses are both set to 0 on transmission and ignored on
    // reception.
    private static final int LIFETIME = 0;
    private static final int OPCODE = 0;
    private static final int OPCODE_SPECIFIC_DATA_LENGTH = 0;

    /**
     * Constructs a {@link AnnouncePcpRequest} object.
     * @param internalIp IP address on the interface used to access the PCP server
     * @param options PCP options
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     */
    public AnnouncePcpRequest(InetAddress internalIp, PcpOption... options) {
        super(OPCODE, LIFETIME, internalIp, OPCODE_SPECIFIC_DATA_LENGTH, options);
    }

    /**
     * Constructs a {@link AnnouncePcpRequest} object by parsing a buffer.
     * @param buffer buffer containing PCP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 0 || op != 0}) or contains an unparseable options region.
     */
    public AnnouncePcpRequest(byte[] buffer) {
        super(buffer, OPCODE_SPECIFIC_DATA_LENGTH);
        // Validate.isTrue(getLifetime() == LIFETIME); // should be 0, but RFC says to ignore on reception -- this provides no extra
                                                       // information not does it doesn't corrupt/invalidate anything, so don't bother
                                                       // verifying
    }

    @Override
    public byte[] getData() {
        return new byte[OPCODE_SPECIFIC_DATA_LENGTH];
    }

    @Override
    public String toString() {
        return "AnnouncePcpRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
