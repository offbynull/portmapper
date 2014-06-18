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
package com.offbynull.portmapper.common;

/**
 * Communication type.
 * @author Kasra Faghihi
 */
public enum CommunicationType {
    /**
     * A unicast packet. Packets on unicast could be a response to a PCP request, or could be an unsolicited update to a PCP request, or
     * could be a ANNOUNCE notification.
     */
    UNICAST,
    /**
     * A multicast packet. Packets on multicast are usually ANNOUNCE notifications. See RFC section on "Rapid Recovery".
     */
    MULTICAST
    
}
