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
package com.offbynull.portmapper;

import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.natpmp.NatPmpPortMapper;
import com.offbynull.portmapper.pcp.PcpPortMapper;
import com.offbynull.portmapper.upnpigd.UpnpIgdPortMapper;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Port mapper factory that attempts to find all port mappers.
 * @author Kasra Faghihi
 */
public final class PortMapperFactory {
    private PortMapperFactory() {
        // do nothing
    }
    
    /**
     * Searches for all PCP, NAT-PMP, or UPNP-IGD enabled routers on all available interfaces.
     * @return port mapper
     * @throws NullPointerException if any argument is {@code null}
     * @throws InterruptedException if interrupted
     */
    public static Set<PortMapper> create(Bus networkBus, Bus processBus) throws InterruptedException {
        Validate.notNull(networkBus);
        Validate.notNull(processBus);
        
        Set<PortMapper> ret = new HashSet<>();
        
        ret.addAll(UpnpIgdPortMapper.identify(networkBus));
        ret.addAll(NatPmpPortMapper.identify(networkBus, processBus));
        ret.addAll(PcpPortMapper.identify(networkBus, processBus));
        
        return ret;
    }
}
