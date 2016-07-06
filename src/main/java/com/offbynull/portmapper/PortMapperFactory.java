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
package com.offbynull.portmapper;

import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.mappers.natpmp.NatPmpPortMapper;
import com.offbynull.portmapper.mappers.pcp.PcpPortMapper;
import com.offbynull.portmapper.mappers.upnpigd.UpnpIgdPortMapper;
import java.net.InetAddress;
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
     * @param additionalIps additional IPs to check (only relevant for PCP and NAT-PMP)
     * @return port mapper
     * @throws NullPointerException if any argument is {@code null}
     * @throws InterruptedException if interrupted
     */
    public static List<PortMapper> discover(Bus networkBus, Bus processBus, InetAddress ... additionalIps) throws InterruptedException {
        Validate.notNull(networkBus);
        Validate.notNull(processBus);
        Validate.notNull(additionalIps);
        Validate.noNullElements(additionalIps);
        
        List<PortMapper> ret = new LinkedList<>();
        
        List<UpnpIgdPortMapper> upnpIgdMappers = UpnpIgdPortMapper.identify(networkBus);
        LOG.debug("Found UPnP-IGD mappers: {}", upnpIgdMappers);
        
        List<NatPmpPortMapper> natPmpMappers = NatPmpPortMapper.identify(networkBus, processBus, additionalIps);
        LOG.debug("Found NAT-PMP mappers: {}", natPmpMappers);
        
        List<PcpPortMapper> pcpMappers = PcpPortMapper.identify(networkBus, processBus, additionalIps);
        LOG.debug("Found PCP mappers: {}", pcpMappers);
        
        ret.addAll(upnpIgdMappers);
        ret.addAll(natPmpMappers);
        ret.addAll(pcpMappers);
        
        LOG.debug("Total found mappers: {}", ret);
        
        return ret;
    }
}
