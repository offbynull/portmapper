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

final class InternalUtils {
    static final int NAT_PMP_VERSION = 0;
    
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
}
