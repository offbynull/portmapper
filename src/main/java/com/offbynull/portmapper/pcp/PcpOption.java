/*
 * Copyright (c) 2013-2014, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.pcp;

import java.nio.BufferOverflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import org.apache.commons.lang3.Validate;

/**
 * Represents a PCP option. Provides PCP option header parsing/construction functionality. From the RFC:
 * <pre>
 * 
 *    Options use the following Type-Length-Value format:
 * 
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |  Option Code  |  Reserved     |       Option Length           |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      :                       (optional) Data                         :
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                          Figure 4: Options Header
 * 
 *    The description of the fields is as follows:
 * 
 *    Option Code:  8 bits.  Its most significant bit indicates if this
 *       option is mandatory (0) or optional (1) to process.
 * 
 *    Reserved:  8 bits.  MUST be set to 0 on transmission and MUST be
 *       ignored on reception.
 * 
 *    Option Length:  16 bits.  Indicates the length of the enclosed data,
 *       in octets.  Options with length of 0 are allowed.  Options that
 *       are not a multiple of 4 octets long are followed by one, two, or
 *       three 0 octets to pad their effective length in the packet to be a
 *       multiple of 4 octets.  The Option Length reflects the semantic
 *       length of the option, not including any padding octets.
 * 
 *    Data:  Option data.
 * </pre>
 * @author Kasra Faghihi
 */
public abstract class PcpOption {
    private int code;
    private int length;
    private ByteBuffer data;

    /**
     * Constructs a {@link PcpOption} object by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     */
    public PcpOption(ByteBuffer buffer) {
        Validate.notNull(buffer);
        
        code = buffer.get() & 0xFF;
        
        buffer.get(); // skip over reserved
        
        length = buffer.getShort() & 0xFFFF;
        
        byte[] dataArr = new byte[length];
        buffer.get(dataArr);
        
        data = ByteBuffer.wrap(dataArr).asReadOnlyBuffer();
        
        // skip over padding
        int remainder = length % 4;
        for (int i = 0; i < remainder; i++) {
            buffer.get();
        }
    }

    /**
     * Constructs a {@link PcpOption} object.
     * @param code option code
     * @param data option data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code code} is {@code < 0 || > 255}, or if the amount of data remaining in {@code data} is
     * {@code > 65535}
     */
    public PcpOption(int code, ByteBuffer data) {
        Validate.inclusiveBetween(0, 255, code);
        Validate.inclusiveBetween(0, 65535, data.remaining());
        Validate.notNull(data);
        
        this.code = code;
        this.length = data.remaining();
        this.data = ByteBuffer.allocate(length + (length % 4)).put(data).asReadOnlyBuffer(); // NOPMD
    }

    /**
     * Get PCP option code.
     * @return PCP option code
     */
    public final int getCode() {
        return code;
    }

    /**
     * Get PCP data length.
     * @return PCP data length
     */
    public final int getLength() {
        return length;
    }

    /**
     * Get PCP data.
     * @return PCP data (read-only)
     */
    public final ByteBuffer getData() {
        return data.asReadOnlyBuffer();
    }

    /**
     * Dump this PCP option in to a byte buffer.
     * @param dst byte buffer to dump to
     * @throws NullPointerException if {@code dst} is {@code null}
     * @throws BufferOverflowException if {@code dst} doesn't have enough space to write this option
     * @throws ReadOnlyBufferException if {@code dst} is read-only
     */
    final void dump(ByteBuffer dst) {
        Validate.notNull(data);
        
        dst.put((byte) code);
        dst.put((byte) 0); // reserved
        dst.putShort((short) length);
        dst.put(data.asReadOnlyBuffer());
    }
    
}
