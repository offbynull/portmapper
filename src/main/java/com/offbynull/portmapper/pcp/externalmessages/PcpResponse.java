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

import static com.offbynull.portmapper.pcp.externalmessages.InternalUtils.PCP_VERSION;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Represents a PCP response. Provides PCP request header construction functionality. From the RFC:
 * <pre>
 * 7.2.  Response Header
 * 
 *    All responses have the following format:
 * 
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |  Version = 2  |R|   Opcode    |   Reserved    |  Result Code  |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                      Lifetime (32 bits)                       |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                     Epoch Time (32 bits)                      |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |                      Reserved (96 bits)                       |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      :                                                               :
 *      :             (optional) Opcode-specific response data          :
 *      :                                                               :
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      :             (optional) Options                                :
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                   Figure 3: Common Response Packet Format
 * 
 *    These fields are described below:
 * 
 *    Version:  Responses from servers compliant with this specification
 *       MUST use version 2.  This is set by the server.
 * 
 *    R: Indicates Request (0) or Response (1).  All Responses MUST use 1.
 *       This is set by the server.
 * 
 *    Opcode:  The 7-bit Opcode value.  The server copies this value from
 *       the request.
 * 
 *    Reserved:  8 reserved bits, MUST be sent as 0, MUST be ignored when
 *       received.  This is set by the server.
 * 
 *    Result Code:  The result code for this response.  See Section 7.4 for
 *       values.  This is set by the server.
 * 
 *    Lifetime:  An unsigned 32-bit integer, in seconds, ranging from 0 to
 *       2^32-1 seconds.  On an error response, this indicates how long
 *       clients should assume they'll get the same error response from
 *       that PCP server if they repeat the same request.  On a success
 *       response for the PCP Opcodes that create a mapping (MAP and PEER),
 *       the Lifetime field indicates the lifetime for this mapping.  This
 *       is set by the server.
 * 
 *    Epoch Time:  The server's Epoch Time value.  See Section 8.5 for
 *       discussion.  This value is set by the server, in both success and
 *       error responses.
 * 
 *    Reserved:  96 reserved bits.  For requests that were successfully
 *       parsed, this MUST be sent as 0, MUST be ignored when received.
 *       This is set by the server.  For requests that were not
 *       successfully parsed, the server copies the last 96 bits of the PCP
 *       Client's IP Address field from the request message into this
 *       corresponding 96-bit field of the response.
 * 
 *    Opcode-specific information:  Payload data for this Opcode.  The
 *       length of this data is determined by the Opcode definition.
 * 
 *    PCP Options:  Zero, one, or more options that are legal for both a
 *       PCP response and for this Opcode.  See Section 7.3.
 * </pre>
 * @author Kasra Faghihi
 */
public abstract class PcpResponse implements PcpMessage {
    /**
     * PCP response header length.
     */
    protected static final int HEADER_LENGTH = 24;
    
    private int op;
    private int resultCode;
    private long lifetime;
    private long epochTime;
    private List<PcpOption> options;
    
    private int dataLength;
    private int optionsLength;
    
    /**
     * Constructs a {@link PcpResponse} object.
     * @param op PCP opcode
     * @param lifetime lifetime in seconds
     * @param epochTime server's epoch time in seconds
     * @param resultCode result code
     * @param opcodeSpecificDataLength length of the opcode specific data
     * @param options PCP options
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if {@code 0 > op > 127 || 0L > lifetime > 0xFFFFFFFFL || 0L > epochTime > 0xFFFFFFFFL}
     */
    public PcpResponse(int op, int resultCode, long lifetime, long epochTime, int opcodeSpecificDataLength, PcpOption ... options) {
        Validate.noNullElements(options);

        this.op = op;
        this.resultCode = resultCode;
        this.lifetime = lifetime;
        this.epochTime = epochTime;
        this.options = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(options)));
        
        this.dataLength = opcodeSpecificDataLength;

        this.dataLength = opcodeSpecificDataLength;
        
        for (PcpOption option : options) {
            optionsLength += option.getDataLength();
        }
        
        validateState();
    }

    /**
     * Constructs a {@link PcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP request data
     * @param opcodeSpecificDataLength length of the opcode specific data
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 1 || 0 > op > 127 || 0L > lifetime > 0xFFFFFFFFL || 0L > epochTime > 0xFFFFFFFFL}) or contains an
     * unparseable options region.
     */
    public PcpResponse(byte[] buffer, int opcodeSpecificDataLength) {
        Validate.notNull(buffer);
        Validate.isTrue(opcodeSpecificDataLength >= 0);
        Validate.isTrue(buffer.length >= HEADER_LENGTH);
        
        int offset = 0;
        
        int version = buffer[offset] & 0xFF;
        Validate.isTrue(version == PCP_VERSION); // check pcp version
        offset++;
        
        int temp = buffer[offset] & 0xFF;
        Validate.isTrue((temp & 128) == 128); // check top bit (r-flag) is 1
        op = temp & 0x7F; // discard first bit, it was used for rflag
        offset++;
        
        offset++; // skip reserved field
        
        resultCode = buffer[offset] & 0xFF; // don't bother checking if resultcode is a success
        offset++;
        
        lifetime = InternalUtils.bytesToInt(buffer, offset) & 0xFFFFFFFFL;
        offset += 4;
        
        epochTime = InternalUtils.bytesToInt(buffer, offset) & 0xFFFFFFFFL;
        offset += 4;
        
        offset += 12; // skip over reserved space
        
        // skip over data block -- data block should be parsed by child class
        this.dataLength = opcodeSpecificDataLength;
        offset += opcodeSpecificDataLength;
        
        options = InternalUtils.parseOptions(buffer, offset);
        for (PcpOption option : options) {
            optionsLength += option.getDataLength();
        }
    }

    private void validateState() {
        Validate.inclusiveBetween(0, 127, op);
        Validate.inclusiveBetween(0, 255, resultCode);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, lifetime);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, epochTime);
        Validate.isTrue(dataLength >= 0);
        Validate.isTrue(optionsLength >= 0);
        Validate.noNullElements(options);
    }

    /**
     * Get opcode.
     * @return opcode
     */
    public final int getOp() {
        return op;
    }

    /**
     * Get the result code. 0 means success.
     * @return result code
     */
    public final int getResultCode() {
        return resultCode;
    }

    /**
     * Get lifetime.
     * @return lifetime in seconds
     */
    public final long getLifetime() {
        return lifetime;
    }

    /**
     * Get epoch time.
     * @return epoch time in seconds
     */
    public final long getEpochTime() {
        return epochTime;
    }

    /**
     * Get PCP options.
     * @return PCP options (unmodifiable)
     */
    public final List<PcpOption> getOptions() {
        return options;
    }

    /**
     * Get PCP opcode-specific data length. Equivalent to {@code getData().length}.
     * @return PCP opcode-specific data length
     */
    public final int getDataLength() {
        return dataLength;
    }

    /**
     * Get PCP opcode-specific data.
     * @return PCP opcode-specific data
     */
    public abstract byte[] getData();
    
    /**
     * Get the number of bytes this PCP option takes up when dumped out (length of buffer returned by {@link #dump() }).
     * @return length of buffer containing PCP option
     */
    public final int getBufferLength() {
        int length = HEADER_LENGTH + dataLength + optionsLength;
        return length;
    }

    @Override
    public final byte[] dump() {
        // first pass calculates required size + gets dumps
        int payloadLength = HEADER_LENGTH;
        
        byte[] opcodeSpecificData = getData();
        payloadLength += opcodeSpecificData.length;
        
        List<byte[]> optionsData = new ArrayList<>(options.size());
        for (PcpOption option : options) {
            byte[] optionData = option.dump();
            payloadLength += optionData.length;
            optionsData.add(optionData);
        }
        
        
        // combine dumps and return
        Validate.isTrue(payloadLength <= InternalUtils.MAX_UDP_PAYLOAD);
        byte[] data = new byte[payloadLength];

        data[0] = 2;
        data[1] = (byte) (op | 0x80); // topmost bit should be 1
        data[2] = 0;
        data[3] = (byte) resultCode;
        InternalUtils.intToBytes(data, 4, (int) lifetime);
        InternalUtils.intToBytes(data, 8, (int) epochTime);

        int offset = 24; // skip over reserved
        
        // write opcode-specific data
        System.arraycopy(opcodeSpecificData, 0, data, offset, opcodeSpecificData.length);
        offset += opcodeSpecificData.length;

        // write options data
        for (byte[] optionData : optionsData) {
            System.arraycopy(optionData, 0, data, offset, optionData.length);
            offset += optionData.length;
        }
        
        
        // return data
        return data;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.op;
        hash = 89 * hash + this.resultCode;
        hash = 89 * hash + (int) (this.lifetime ^ (this.lifetime >>> 32));
        hash = 89 * hash + (int) (this.epochTime ^ (this.epochTime >>> 32));
        hash = 89 * hash + Objects.hashCode(this.options);
        hash = 89 * hash + this.dataLength;
        hash = 89 * hash + this.optionsLength;
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
        final PcpResponse other = (PcpResponse) obj;
        if (this.op != other.op) {
            return false;
        }
        if (this.resultCode != other.resultCode) {
            return false;
        }
        if (this.lifetime != other.lifetime) {
            return false;
        }
        if (this.epochTime != other.epochTime) {
            return false;
        }
        if (this.dataLength != other.dataLength) {
            return false;
        }
        if (this.optionsLength != other.optionsLength) {
            return false;
        }
        if (!Objects.equals(this.options, other.options)) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
