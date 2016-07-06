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

import java.util.Arrays;

/**
 * A {@link PcpOption} that doesn't map to any of the known PCP option values presented in the RFC. From the RFC:
 * <pre>
 *    Because the PCP client cannot reject a response containing an Option,
 *    PCP clients MUST ignore Options that they do not understand that
 *    appear in responses, including Options in the mandatory-to-process
 *    range.  Naturally, if a client explicitly requests an Option where
 *    correct execution of that Option requires processing the Option data
 *    in the response, that client SHOULD implement code to do that.
 * </pre>
 * @author Kasra Faghihi
 */
public final class UnknownPcpOption extends PcpOption {

    private byte[] data;

    /**
     * Constructs a {@link UnknownPcpOption} object by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @param offset offset in {@code buffer} where the PCP option starts
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} is malformed (doesn't contain enough bytes
     * / length is not a multiple of 4 (not enough padding) / length exceeds 65535 / data doesn't contain enough bytes)
     */
    public UnknownPcpOption(byte[] buffer, int offset) {
        super(buffer, offset);
        int dataStartOffset = offset + HEADER_LENGTH;
        int dataStopOffset = dataStartOffset + getDataLength();
        this.data = Arrays.copyOfRange(buffer, dataStartOffset, dataStopOffset);
    }

    /**
     * Constructs a {@link PcpOption} object.
     * @param code option code
     * @param data option data (do not include padding)
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code code < 0 || code > 255}, or if {@code data.length > 65535}
     */
    public UnknownPcpOption(int code, byte[] data) {
        super(code, data.length);
        this.data = Arrays.copyOf(data, data.length);
    }

    @Override
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "UnknownPcpOption{super=" + super.toString() + "data=" + Arrays.toString(data) + '}';
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 37 * hash + Arrays.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnknownPcpOption other = (UnknownPcpOption) obj;
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }
}
