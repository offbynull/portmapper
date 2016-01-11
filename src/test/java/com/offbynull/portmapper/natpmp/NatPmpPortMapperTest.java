package com.offbynull.portmapper.natpmp;

public final class NatPmpPortMapperTest {
    
//    @Test
//    @Ignore // REQUIRES AN APPLE ROUTER TO TEST PROPERLY (OR POSSIBLE NAT-PMP ENABLED MINIUPNPD)
//    public void testPortMapping() throws Throwable {
//        Set<DiscoveredNatPmpDevice> devices = NatPmpDiscovery.discover();
//        DiscoveredNatPmpDevice device = devices.iterator().next();
//        
//        PortMapperEventListener listener = Mockito.mock(PortMapperEventListener.class);
//        try (NatPmpPortMapper mapper = new NatPmpPortMapper(device.getGatewayAddress(), listener)) {
//            mapper.mapPort(PortType.TCP, 12345, 60);
//            
//            Thread.sleep(10000L);
//        }
//        
//        Mockito.verifyZeroInteractions(listener);
//    }
}
