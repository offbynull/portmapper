package com.offbynull.portmapper.pcp;

import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortType;
import com.offbynull.portmapper.io.network.NetworkGateway;
import com.offbynull.portmapper.io.network.internalmessages.KillNetworkRequest;
import com.offbynull.portmapper.io.process.ProcessGateway;
import com.offbynull.portmapper.io.process.internalmessages.KillProcessRequest;
import java.net.InetAddress;
import java.util.Set;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

//@Ignore("REQUIRES MINIUPNPD TO BE PROPERLY SET UP IN A VM ALONG WITH AN APPLE AIRPORT ROUTER")
public final class PcpPortMapperTest {

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
    public void mustFindAndControlPortMappers() throws Throwable {
        Set<PcpPortMapper> mappers = PcpPortMapper.identify(networkBus, processBus);
        PcpPortMapper miniupnpdMapper = new PcpPortMapper(
                networkBus,
                InetAddress.getByName("192.168.75.1"),
                InetAddress.getByName("192.168.75.128"));
        mappers.add(miniupnpdMapper);

        assertEquals(2, mappers.size()); // 1 discovered apple airport router and 1 miniupnpd router forcefully added in
                                         // miniupnpd router is not picked up by discovery because its running in a vm
        
        for (PcpPortMapper mapper : mappers) {
            MappedPort tcpPort = mapper.mapPort(PortType.TCP, 12345, 12345, 4294967295L);
            assertEquals(PortType.TCP, tcpPort.getPortType());
            assertEquals(12345, tcpPort.getInternalPort());
            // assertNotEquals(4294967295L, tcpPort.getLifetime()); // may be equal
            // external port/external ip/etc.. may all be different

            MappedPort udpPort = mapper.mapPort(PortType.UDP, 12345, 12345, 4294967295L);
            assertEquals(PortType.UDP, udpPort.getPortType());
            // assertNotEquals(4294967295L, udpPort.getLifetime()); // may be equal
            // external port/external ip/etc.. may all be different




            tcpPort = mapper.refreshPort(tcpPort, 10000L);
            assertEquals(PortType.TCP, tcpPort.getPortType());
            assertEquals(12345, tcpPort.getInternalPort());
            assertEquals(10000L, tcpPort.getLifetime());
            // external port/external ip/etc.. may all be different

            udpPort = mapper.refreshPort(udpPort, 10000L);
            assertEquals(PortType.UDP, udpPort.getPortType());
            assertEquals(12345, udpPort.getInternalPort());
            assertEquals(10000L, udpPort.getLifetime());
            // external port/external ip/etc.. may all be different




            mapper.unmapPort(tcpPort);
            mapper.unmapPort(udpPort);
        }
    }
}
