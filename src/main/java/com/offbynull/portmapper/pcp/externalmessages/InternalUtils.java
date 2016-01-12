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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;

final class InternalUtils {
    // RFC drafts 26 to 29 use version number 2. Prior drafts use version 1. The version number change was introduced to avoid communication
    // issues between clients and servers that implemented draft-versions of the spec. The RFC draft changelog notes this change as ...
    // "Bump version number from 1 to 2, to accommodate pre-RFC PCP client implementations without needing a heuristic."
    //
    // Prior to version 2, the RFC drafts are all over the place. The communication protocol (packet structures / constants / etc...) change
    // non-trivially between drafts. For example, changes between draft 24 and 25 include the addition of nonces for MAP/PEER and a maximum
    // UDP packet size of 1024 (as opposed to 1100 in the final RFC).
    //
    // As such, it would be impossible to support all variations of version 1. Chances are that home-grade routers that supported some draft
    // version of PCP version 1 also supported other protocols capable of IPv6 support (such as UPNP-IGDv2) or have subsequently been
    // updated to support PCP version 2 via a firmware update.
    //
    // Given all this, we're only going to support PCP_VERSION 2.
    static final int PCP_VERSION = 2;

    static final int MAX_UDP_PAYLOAD = 1100;
    
    private InternalUtils() {
        // do nothing
    }
    
    // http://stackoverflow.com/a/5616073/1196226
    static long bytesToLong(byte[] data, int offset) {
        return ((data[offset + 0] & 0xFFL) << 56)
                | ((data[offset + 1] & 0xFFL) << 48)
                | ((data[offset + 2] & 0xFFL) << 40)
                | ((data[offset + 3] & 0xFFL) << 32)
                | ((data[offset + 4] & 0xFFL) << 24)
                | ((data[offset + 5] & 0xFFL) << 16)
                | ((data[offset + 6] & 0xFFL) << 8)
                | (data[offset + 7] & 0xFFL);
    }

    static void longToBytes(byte[] data, int offset, long value) {
        data[offset] = (byte) ((value >> 56) & 0xFF);
        data[offset + 1] = (byte) ((value >> 48) & 0xFF);
        data[offset + 2] = (byte) ((value >> 40) & 0xFF);
        data[offset + 3] = (byte) ((value >> 32) & 0xFF);
        data[offset + 4] = (byte) ((value >> 24) & 0xFF);
        data[offset + 5] = (byte) ((value >> 16) & 0xFF);
        data[offset + 6] = (byte) ((value >> 8) & 0xFF);
        data[offset + 7] = (byte) (value & 0xFF);
    }

    static int bytesToInt(byte[] data, int offset) {
        return ((data[offset + 0] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    static void intToBytes(byte[] data, int offset, int value) {
        data[offset] = (byte) ((value >> 24) & 0xFF);
        data[offset + 1] = (byte) ((value >> 16) & 0xFF);
        data[offset + 2] = (byte) ((value >> 8) & 0xFF);
        data[offset + 3] = (byte) (value & 0xFF);
    }

    static short bytesToShort(byte[] data, int offset) {
        return (short)
                (((data[offset + 0] & 0xFF) << 8)
                | (data[offset + 1] & 0xFF));
    }

    static void shortToBytes(byte[] data, int offset, short value) {
        data[offset] = (byte) ((value >> 8) & 0xFF);
        data[offset + 1] = (byte) (value & 0xFF);
    }
    
    
    static List<PcpOption> parseOptions(byte[] buffer, int offset) {
        Validate.notNull(buffer);
        Validate.isTrue(offset >= 0);
        Validate.isTrue(offset <= buffer.length);
        
        List<PcpOption> pcpOptionsList = new ArrayList<>();
        while (offset < buffer.length) {
            PcpOption option;

            try {
                option = new FilterPcpOption(buffer, offset);
                offset += option.getBufferLength();
                pcpOptionsList.add(option);
                continue;
            } catch (IllegalArgumentException iae) {
                // do nothing
            }
            
            try {
                option = new PreferFailurePcpOption(buffer, offset);
                offset += option.getBufferLength();
                pcpOptionsList.add(option);
                continue;
            } catch (IllegalArgumentException iae) {
                // do nothing
            }
            
            try {
                option = new ThirdPartyPcpOption(buffer, offset);
                offset += option.getBufferLength();
                pcpOptionsList.add(option);
                continue;
            } catch (IllegalArgumentException iae) {
                // do nothing
            }
            
            option = new UnknownPcpOption(buffer, offset);
            offset += option.getBufferLength();
            pcpOptionsList.add(option);
        }
        
        return pcpOptionsList;
    }
}
