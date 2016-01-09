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
package com.offbynull.portmapper.upnpigd;

import com.offbynull.portmapper.io.NetworkGateway;
import com.offbynull.portmapper.io.internalmessages.KillNetworkRequest;
import java.net.InetAddress;
import java.util.Set;

/**
 * UPnP-IGD test.
 * @author Kasra Faghihi
 */
final class App {
    private App() {
        // do nothing
    }
    
    /**
     * Main method.
     * @param args unused
     * @throws Throwable on error
     */
    public static void main(String []args) throws Throwable {
        NetworkGateway networkGateway = NetworkGateway.create();
        try {
            Set<UpnpIgdPortMapper> mappers = UpnpIgdPortMapper.identify(networkGateway.getBus());
            System.out.println(mappers);
        } finally {
            networkGateway.getBus().send(new KillNetworkRequest());
            networkGateway.join();
        }
    }
}
