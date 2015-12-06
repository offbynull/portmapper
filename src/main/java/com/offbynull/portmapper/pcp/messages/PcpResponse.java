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
package com.offbynull.portmapper.pcp.messages;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public abstract class PcpResponse {
    private int op;
    private long lifetime;
    private long epochTime;
    private List<PcpOption> options;
    
    /**
     * Constructs a {@link PcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if the version doesn't match the expected version (must always be {@code 2}), or if the r-flag isn't
     * set, or if there's an unsuccessful/unrecognized result code
     */
    PcpResponse(ByteBuffer buffer) {
        Validate.notNull(buffer);
        
        if (buffer.remaining() < 4 || buffer.remaining() > 1100 || buffer.remaining() % 4 != 0) {
            throw new IllegalArgumentException("Bad packet size: " + buffer.remaining());
        }
        
        int version = buffer.get() & 0xFF;
        Validate.isTrue(version == 2, "Unknown version: %d", version);
        
        int temp = buffer.get() & 0xFF;
        Validate.isTrue((temp & 128) == 128, "Bad R-flag: %d", temp);
        op = temp & 0x7F; // discard first bit, it was used for rflag
        
        buffer.get(); // skip reserved field
        
        int resultCodeNum = buffer.get() & 0xFF;
        PcpResultCode[] resultCodes = PcpResultCode.values();
        
        Validate.isTrue(resultCodeNum < resultCodes.length, "Unknown result code encountered: %d", resultCodeNum);
        Validate.isTrue(resultCodeNum == PcpResultCode.SUCCESS.ordinal(), "Unsuccessful result code: %s [%s]",
                resultCodes[resultCodeNum].toString(), resultCodes[resultCodeNum].getMessage());
        
        lifetime = buffer.getInt() & 0xFFFFFFFFL;
        
        epochTime = buffer.getInt() & 0xFFFFFFFFL;
        
        for (int i = 0; i < 12; i++) {
            buffer.get();
            //Validate.isTrue(buffer.get() == 0, "Reserved space indicates unsuccessful response");
        }
        
        options = Collections.emptyList();
    }

    /**
     * Get opcode.
     * @return opcode
     */
    public final int getOp() {
        return op;
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
     * MUST be called by child class constructor so that PCP options can be parsed.
     * @param buffer buffer containing PCP response data, with the pointer at the point which PCP options begin
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     */
    protected final void parseOptions(ByteBuffer buffer) {
        Validate.notNull(buffer);
        
        List<PcpOption> pcpOptionsList = new ArrayList<>();
        while (buffer.hasRemaining()) {
            PcpOption option;

            try {
                buffer.mark();
                option = new FilterPcpOption(buffer);
                pcpOptionsList.add(option);
                continue;
            } catch (BufferUnderflowException | IllegalArgumentException e) {
                buffer.reset();
            }
            
            try {
                buffer.mark();
                option = new PreferFailurePcpOption(buffer);
                pcpOptionsList.add(option);
                continue;
            } catch (BufferUnderflowException | IllegalArgumentException e) {
                buffer.reset();
            }
            
            try {
                buffer.mark();
                option = new ThirdPartyPcpOption(buffer);
                pcpOptionsList.add(option);
                continue;
            } catch (BufferUnderflowException | IllegalArgumentException e) {
                buffer.reset();
            }
            
            option = new UnknownPcpOption(buffer);
            pcpOptionsList.add(option);
        }
        
        options = Collections.unmodifiableList(pcpOptionsList);
    } 
}
