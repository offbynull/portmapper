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
package com.offbynull.portmapper.upnpigd;

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortMapper;
import com.offbynull.portmapper.PortMapperEventListener;
import com.offbynull.portmapper.PortType;
import java.util.Set;

/**
 * UPnP-IGD test.
 * @author Kasra Faghihi
 */
public final class App {
    private App() {
        // do nothing
    }
    
    /**
     * Main method.
     * @param args unused
     * @throws Throwable on error
     */
    public static void main(String []args) throws Throwable {
//        Set<UpnpIgdService> services = UpnpIgdDiscovery.discover();
//        UpnpIgdService service = services.iterator().next();
//        
//        PortMapper mapper = new UpnpIgdPortMapper(service, new PortMapperEventListener() { // NOPMD
//
//            @Override
//            public void resetRequired(String details) {
//                System.out.println(details);
//                System.exit(0);
//            }
//        });
//        
//        
//        //System.out.println(controller.getExternalIp());
//        
//        MappedPort mappedPort = mapper.mapPort(PortType.TCP, 12345, 10L);
//        for (int i = 0; i < 3; i++) {
//            Thread.sleep(5000L);
//            System.out.println("Refreshing...");
//            mapper.refreshPort(mappedPort, 10L);
//        }
//        
//        Thread.sleep(20000L);
//        
//        mapper.close();
    }
}
