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

import com.offbynull.portmapper.natpmp.NatPmpPortMapper;
import com.offbynull.portmapper.pcp.PcpPortMapper;
import com.offbynull.portmapper.upnpigd.UpnpIgdPortMapper;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port mapper factory that attempts to find all port mappers.
 * @author Kasra Faghihi
 */
public final class PortMapperFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PortMapperFactory.class);
    
    private PortMapperFactory() {
        // do nothing
    }
    
    /**
     * Searches for all PCP, NAT-PMP, or UPNP-IGD enabled routers on all available interfaces.
     * @param networkBus network bus
     * @param processBus process bus
     * @return port mapper
     * @throws NullPointerException if any argument is {@code null}
     * @throws InterruptedException if interrupted
     */
    public static List<PortMapper> discover(Bus networkBus, Bus processBus) throws InterruptedException {
        Validate.notNull(networkBus);
        Validate.notNull(processBus);
        
        List<PortMapper> ret = new LinkedList<>();
        
        List<UpnpIgdPortMapper> upnpIgdMappers = UpnpIgdPortMapper.identify(networkBus);
        LOG.debug("Found UPnP-IGD mappers: {}", upnpIgdMappers);
        
        List<NatPmpPortMapper> natPmpMappers = NatPmpPortMapper.identify(networkBus, processBus);
        LOG.debug("Found NAT-PMP mappers: {}", natPmpMappers);
        
        List<PcpPortMapper> pcpMappers = PcpPortMapper.identify(networkBus, processBus);
        LOG.debug("Found PCP mappers: {}", natPmpMappers);
        
        ret.addAll(upnpIgdMappers);
        ret.addAll(natPmpMappers);
        ret.addAll(pcpMappers);
        
        LOG.debug("Total found mappers: {}", pcpMappers);
        
        return ret;
    }
}
