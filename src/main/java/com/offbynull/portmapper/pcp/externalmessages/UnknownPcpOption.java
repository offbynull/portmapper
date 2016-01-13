/*
 * Copyright (c) 2013-2016, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.portmapper.pcp.externalmessages;

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
