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

import java.net.InetAddress;
import java.nio.BufferOverflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public abstract class PcpRequest {
    private int op;
    private long lifetime;
    private List<PcpOption> options;

    /**
     * Constructs a {@link PcpRequest} object.
     * @param op PCP opcode
     * @param lifetime requested lifetime in seconds
     * @param options PCP options
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code op > 127}, or if {@code lifetime > 0xFFFFFFFFL}
     */
    PcpRequest(int op, long lifetime, PcpOption ... options) {
        Validate.inclusiveBetween(0, 127, op);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, lifetime);
        Validate.noNullElements(options);

        this.op = op;
        this.lifetime = lifetime;
        this.options = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(options)));
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
     * Get PCP options.
     * @return PCP options (unmodifiable)
     */
    public final List<PcpOption> getOptions() {
        return options;
    }

    /**
     * Dump this PCP request in to a byte buffer.
     * @param dst byte buffer to dump to
     * @param selfAddress IP address of this machine on the interface used to access the PCP server
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferOverflowException if {@code dst} doesn't have enough space to write this option
     * @throws ReadOnlyBufferException if {@code dst} is read-only
     */
    public final void dump(ByteBuffer dst, InetAddress selfAddress) {
        Validate.notNull(dst);
        Validate.notNull(selfAddress);
        
        dst.put((byte) 2);
        dst.put((byte) op); // topmost bit should be 0, because op is between 0 to 127, which means r-flag = 0
        dst.putShort((short) 0);
        dst.putInt((int) lifetime);
        
        byte[] selfAddressArr = selfAddress.getAddress();
        switch (selfAddressArr.length) {
            case 4: {
                // convert ipv4 address to ipv4-mapped ipv6 address
                for (int i = 0; i < 10; i++) {
                    dst.put((byte) 0);
                }
                
                for (int i = 0; i < 2; i++) {
                    dst.put((byte) 0xff);
                }
                
                dst.put(selfAddressArr);
                break;
            }
            case 16: {
                dst.put(selfAddressArr);
                break;
            }
            default:
                throw new IllegalArgumentException(); // should never happen
        }
        
        dumpOpCodeSpecificInformation(dst);

        for (PcpOption option : options) {
            option.dump(dst);
        }
    }
    
    /**
     * Called by {@link #dump(java.nio.ByteBuffer, java.net.InetAddress) } to write op-code specific data to the buffer.
     * @param dst byte buffer to dump to
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferOverflowException if {@code dst} doesn't have enough space to write this option
     * @throws ReadOnlyBufferException if {@code dst} is read-only
     */
    protected abstract void dumpOpCodeSpecificInformation(ByteBuffer dst);
}
