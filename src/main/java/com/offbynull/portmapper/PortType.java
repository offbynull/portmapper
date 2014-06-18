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
package com.offbynull.portmapper;

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
