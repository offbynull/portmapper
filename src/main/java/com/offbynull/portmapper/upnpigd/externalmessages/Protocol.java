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
package com.offbynull.portmapper.upnpigd.externalmessages;

import org.apache.commons.lang3.Validate;

/**
 * Protocol type.
 * @author Kasra Faghihi
 */
public enum Protocol {

    /**
     * UDP protocol.
     */
    UDP(17),
    /**
     * TCP protocol.
     */
    TCP(6);
    
    private int iana;

    private Protocol(int iana) {
        Validate.inclusiveBetween(0, 255, iana);
        this.iana = iana;
    }

    int getIana() {
        return iana;
    }
    
}
