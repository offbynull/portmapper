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
package com.offbynull.portmapper.pcp.messages;

/**
 * Represents a PCP message.
 * @author Kasra Faghihi
 */
public interface PcpMessage {
    /**
     * Dump out the PCP message as a packet.
     * @return PCP packet
     * @throws IndexOutOfBoundsException if the generated packet is greater than 1100 bytes (section 7 of the RFC states: All PCP messages
     * are sent over UDP, with a maximum UDP payload length of 1100 octets)
     */
    byte[] dump();
}
