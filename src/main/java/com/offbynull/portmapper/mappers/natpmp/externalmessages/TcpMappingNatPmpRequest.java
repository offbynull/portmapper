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

/**
 * Represents a NAT-PMP TCP mapping request.
 * @author Kasra Faghihi
 */
public final class TcpMappingNatPmpRequest extends MappingNatPmpRequest {
    private static final int OP = 2;
    
    /**
     * Construct a {@link TcpMappingNatPmpRequest} object.
     * @param data buffer containing NAT-PMP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} isn't the right size or is malformed ({@code op != 2 || version != 0 ||
     * 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || 0 > lifetime > 0xFFFFFFFFL})
     */
    public TcpMappingNatPmpRequest(byte[] data) {
        super(OP, data);
    }

    /**
     * Construct a {@link TcpMappingNatPmpRequest} object.
     * @param internalPort internal port
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param lifetime desired lifetime of mapping ({@code 0} to destroy mapping)
     * @throws IllegalArgumentException if {@code 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535
     * || 0 > lifetime > 0xFFFFFFFFL}
     */
    public TcpMappingNatPmpRequest(int internalPort, int suggestedExternalPort, long lifetime) {
        super(OP, internalPort, suggestedExternalPort, lifetime);
    }

    @Override
    public String toString() {
        return "TcpMappingNatPmpRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
