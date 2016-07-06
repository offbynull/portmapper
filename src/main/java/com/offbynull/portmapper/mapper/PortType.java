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
package com.offbynull.portmapper.mapper;

/**
 * Describes the port type.
 *
 * @author Kasra Faghihi
 */
public enum PortType {

    /**
     * UDP port.
     */
    UDP(17),
    /**
     * TCP port.
     */
    TCP(6);

    private final int protocolNumber;

    PortType(int protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    /**
     * Get the IANA protocol number.
     *
     * @return IANA protocol number
     */
    public int getProtocolNumber() {
        return protocolNumber;
    }

    /**
     * Convert IANA protocol number to {@link PortType}.
     * @param number IANA protocol number
     * @return corresponding {@link PortType}
     * @throws IllegalArgumentException if {@code number} didn't match any {@link PortType} value
     */
    public static PortType fromIanaNumber(int number) {
        switch (number) {
            case 6:
                return PortType.TCP;
            case 17:
                return PortType.UDP;
            default:
                throw new IllegalArgumentException();
        }
    }

}
