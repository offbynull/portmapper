/*
 * Copyright (c) 2013-2015, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.natpmp.messages;

import org.apache.commons.lang3.Validate;

final class InternalUtils {
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

    static ResponseHeader parseNatPmpResponseHeader(byte[] data) {
        Validate.notNull(data);
        Validate.isTrue(data.length >= 2, "Bad packet size: %d", data.length);
        
        int version = data[0] & 0xFF;
        Validate.isTrue(version == 0, "Unknown version: %d", version);
        
        int op = data[1] & 0xFF;
        Validate.isTrue(op >= 128, "Op must be >= 128: %d", op);

        // 3.5.  Result Codes
        //
        //   Currently defined result codes:
        //
        // 0 - Success
        // 1 - Unsupported Version
        // 2 - Not Authorized/Refused
        //     (e.g., box supports mapping, but user has turned feature off)
        // 3 - Network Failure
        //     (e.g., NAT box itself has not obtained a DHCP lease)
        // 4 - Out of resources
        //     (NAT box cannot create any more mappings at this time)
        // 5 - Unsupported opcode
        int resultCode = bytesToShort(data, 2) & 0xFFFF;
        // Why comment out this check? The result codes listed in the RFC are "currently defined", meaning that they're subject to change.
        // Validate.isTrue(op <= 5, "Unrecognized result code: %d", 5);
        
        long secondsSinceStartOfEpoch = bytesToInt(data, 4) & 0xFFFFFFFFL;

        return new ResponseHeader(op, resultCode, secondsSinceStartOfEpoch);
    }


    static RequestHeader parseNatPmpRequestHeader(byte[] data) {
        Validate.notNull(data);
        Validate.isTrue(data.length >= 2, "Bad packet size: %d", data.length);
        
        int version = data[0] & 0xFF;
        Validate.isTrue(version == 0, "Unknown version: %d", version);

        int op = data[1] & 0xFF;
        Validate.inclusiveBetween(0, 127, op);

        return new RequestHeader(op);
    }

}
