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

import com.offbynull.portmapper.helpers.NetworkUtils;
import static com.offbynull.portmapper.pcp.externalmessages.InternalUtils.PCP_VERSION;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Represents a PCP request. Provides PCP request header construction functionality. From the RFC:
 * <pre>
 * 7.1.  Request Header
 * 
 *    All requests have the following format:
 * 
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |  Version = 2  |R|   Opcode    |         Reserved              |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                 Requested Lifetime (32 bits)                  |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |            PCP Client's IP Address (128 bits)                 |
 *      |                                                               |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      :                                                               :
 *      :             (optional) Opcode-specific information            :
 *      :                                                               :
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      :                                                               :
 *      :             (optional) PCP Options                            :
 *      :                                                               :
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                   Figure 2: Common Request Packet Format
 * 
 *    These fields are described below:
 * 
 *    Version:  This document specifies protocol version 2.  PCP clients
 *       and servers compliant with this document use the value 2.  This
 *       field is used for version negotiation as described in Section 9.
 * 
 *    R: Indicates Request (0) or Response (1).
 * 
 *    Opcode:  A 7-bit value specifying the operation to be performed.  MAP
 *       and PEER Opcodes are defined in Sections 11 and 12.
 * 
 *    Reserved:  16 reserved bits.  MUST be zero on transmission and MUST
 *       be ignored on reception.
 * 
 *    Requested Lifetime:  An unsigned 32-bit integer, in seconds, ranging
 *       from 0 to 2^32-1 seconds.  This is used by the MAP and PEER
 *       Opcodes defined in this document for their requested lifetime.
 * 
 *    PCP Client's IP Address:  The source IPv4 or IPv6 address in the IP
 *       header used by the PCP client when sending this PCP request.  An
 *       IPv4 address is represented using an IPv4-mapped IPv6 address.
 *       The PCP Client IP Address in the PCP message header is used to
 *       detect an unexpected NAT on the path between the PCP client and
 *       the PCP-controlled NAT or firewall device.  See Section 8.1.
 * 
 *    Opcode-specific information:  Payload data for this Opcode.  The
 *       length of this data is determined by the Opcode definition.
 * 
 *    PCP Options:  Zero, one, or more options that are legal for both a
 *       PCP request and for this Opcode.  See Section 7.3.
 * </pre>
 * @author Kasra Faghihi
 */
public abstract class PcpRequest implements PcpMessage {
    /**
     * PCP request header length.
     */
    protected static final int HEADER_LENGTH = 24;
    
    private int op;
    private long lifetime;
    private InetAddress internalIp;
    private List<PcpOption> options;
    
    private int dataLength;
    private int optionsLength;

    /**
     * Constructs a {@link PcpRequest} object.
     * @param op PCP opcode
     * @param lifetime requested lifetime in seconds
     * @param internalIp IP address on the interface used to access the PCP server
     * @param opcodeSpecificDataLength length of the opcode specific data
     * @param options PCP options
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if {@code 0 > op > 127 || 0L > lifetime > 0xFFFFFFFFL || 0 > opcodeSpecificDataLength}
     */
    public PcpRequest(int op, long lifetime, InetAddress internalIp, int opcodeSpecificDataLength, PcpOption ... options) {
        Validate.notNull(internalIp);
        Validate.noNullElements(options);

        this.op = op;
        this.lifetime = lifetime;
        this.internalIp = internalIp;
        this.options = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(options)));

        this.dataLength = opcodeSpecificDataLength;
        
        for (PcpOption option : options) {
            optionsLength += option.getDataLength();
        }
        
        validateState();
    }

    /**
     * Constructs a {@link PcpRequest} object by parsing a buffer.
     * @param buffer buffer containing PCP request data
     * @param opcodeSpecificDataLength length of the opcode specific data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 0 || 0 > op > 127 || 0L > lifetime > 0xFFFFFFFFL || || 0 > opcodeSpecificDataLength}) or contains
     * an unparseable options region.
     */
    public PcpRequest(byte[] buffer, int opcodeSpecificDataLength) {
        Validate.notNull(buffer);
        Validate.isTrue(opcodeSpecificDataLength >= 0);
        Validate.isTrue(buffer.length >= HEADER_LENGTH);
        
        int offset = 0;

        int version = buffer[offset] & 0xFF;
        Validate.isTrue(version == PCP_VERSION); // check pcp version
        offset++;
        
        int temp = buffer[offset] & 0xFF;
        Validate.isTrue((temp & 128) == 0); // check top bit (r-flag) is 0
        op = temp;
        offset++;
        
        // int reserved = InternalUtils.bytesToShort(buffer, offset); // RFC says to ignore on reception
        offset += 2;
        
        lifetime = InternalUtils.bytesToInt(buffer, offset) & 0xFFFFFFFFL;
        offset += 4;

        // at offset 8, write ipv6 address
        internalIp = NetworkUtils.convertBytesToAddress(buffer, offset, 16);
        offset += 16;
        
        // skip over data block -- data block should be parsed by child class
        this.dataLength = opcodeSpecificDataLength;
        offset += opcodeSpecificDataLength;
        
        options = InternalUtils.parseOptions(buffer, offset);
        for (PcpOption option : options) {
            optionsLength += option.getDataLength();
        }
        
        validateState();
    }

    private void validateState() {
        Validate.notNull(internalIp);
        Validate.inclusiveBetween(0, 127, op);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, lifetime);
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
     * Get requested lifetime.
     * @return requested lifetime in seconds
     */
    public final long getLifetime() {
        return lifetime;
    }

    /**
     * Get internal IP address.
     * @return internal IP address
     */
    public final InetAddress getInternalIp() {
        return internalIp;
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
        data[1] = (byte) op; // topmost bit should be 0, because op is between 0 to 127, which means r-flag = 0
        InternalUtils.shortToBytes(data, 2, (short) 0);
        InternalUtils.intToBytes(data, 4, (int) lifetime);

        // at offset 8, write ipv6 address
        byte[] selfAddressArr = NetworkUtils.convertAddressToIpv6Bytes(internalIp);
        System.arraycopy(selfAddressArr, 0, data, 8, selfAddressArr.length);

        int offset = 24;
        
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
}
