package com.offbynull.portmapper;

import com.offbynull.portmapper.gateways.network.NetworkGateway;
import com.offbynull.portmapper.gateways.network.internalmessages.KillNetworkRequest;
import com.offbynull.portmapper.gateways.process.ProcessGateway;
import com.offbynull.portmapper.gateways.process.internalmessages.KillProcessRequest;
import com.offbynull.portmapper.mappers.natpmp.NatPmpPortMapper;
import com.offbynull.portmapper.mappers.pcp.PcpPortMapper;
import com.offbynull.portmapper.mappers.upnpigd.FirewallUpnpIgdPortMapper;
import com.offbynull.portmapper.mappers.upnpigd.PortMapperUpnpIgdPortMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

//@Ignore("REQUIRES MINIUPNPD TO BE PROPERLY SET UP IN A VM ALONG WITH AN APPLE AIRPORT ROUTER")
public class PortMapperFactoryIntegrationTest {

    private NetworkGateway network;
    private Bus networkBus;
    private ProcessGateway process;
    private Bus processBus;
    
    @Before
    public void before() {
        network = NetworkGateway.create();
        networkBus = network.getBus();
        process = ProcessGateway.create();
        processBus = process.getBus();
    }

    @After
    public void after() {
        networkBus.send(new KillNetworkRequest());
        processBus.send(new KillProcessRequest());
    }

    @Test
    public void mustDiscoverPortMappers() throws Exception {
        List<PortMapper> mappers = PortMapperFactory.discover(networkBus, processBus);
        
        Set<Class<?>> expectedTypes = new HashSet<>();
        
        for (PortMapper mapper : mappers) {
            expectedTypes.add(mapper.getClass());
        }
        
        Set<Class<?>> actualTypes = new HashSet<>();
        actualTypes.add(FirewallUpnpIgdPortMapper.class);
        actualTypes.add(PortMapperUpnpIgdPortMapper.class);
        actualTypes.add(PcpPortMapper.class);
        actualTypes.add(NatPmpPortMapper.class);
        
        assertEquals(expectedTypes, actualTypes);
    }
    
}
