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

import com.offbynull.portmapper.natpmp.DiscoveredNatPmpDevice;
import com.offbynull.portmapper.natpmp.NatPmpDiscovery;
import com.offbynull.portmapper.natpmp.NatPmpPortMapper;
import com.offbynull.portmapper.pcp.DiscoveredPcpDevice;
import com.offbynull.portmapper.pcp.PcpDiscovery;
import com.offbynull.portmapper.pcp.PcpPortMapper;
import com.offbynull.portmapper.upnpigd.UpnpIgdDiscovery;
import com.offbynull.portmapper.upnpigd.UpnpIgdPortMapper;
import com.offbynull.portmapper.upnpigd.UpnpIgdService;
import java.io.IOException;
import java.util.Set;

/**
 * Port mapper factory that attempts to find a router that has PCP, NAT-PMP, or UPNP-IGD enabled.
 * @author Kasra Faghihi
 */
public final class PortMapperFactory {
    private PortMapperFactory() {
        // do nothing
    }
    
    /**
     * Searches for a PCP, NAT-PMP, or UPNP-IGD enabled router. Returns the first router that's found.
     * @param listener port mapping event listener
     * @return port mapper
     * @throws IllegalStateException if no router is found
     * @throws InterruptedException if interrupted
     * @throws IOException if IO error occurs
     */
    public static PortMapper create(PortMapperEventListener listener) throws InterruptedException, IOException {
        Set<DiscoveredPcpDevice> pcpDevices = PcpDiscovery.discover();
        if (!pcpDevices.isEmpty()) {
            DiscoveredPcpDevice device = pcpDevices.iterator().next();
            return new PcpPortMapper(device.getGatewayAddress(), device.getLocalAddress(), true, listener);
        }
        
        Set<DiscoveredNatPmpDevice> natPmpDevices = NatPmpDiscovery.discover();
        if (!natPmpDevices.isEmpty()) {
            DiscoveredNatPmpDevice device = natPmpDevices.iterator().next();
            return new NatPmpPortMapper(device.getGatewayAddress(), listener);
        }
        
        Set<UpnpIgdService> upnpIgdService = UpnpIgdDiscovery.discover();
        if (!upnpIgdService.isEmpty()) {
            UpnpIgdService service = upnpIgdService.iterator().next();
            return new UpnpIgdPortMapper(service, listener);
        }
        
        throw new IllegalStateException();
    }
}
