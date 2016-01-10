package com.offbynull.portmapper.upnpigd;

public class UpnpIgdTest {

//    @Test
//    @Ignore // REQUIRES MINIUPNPD TO BE PROPERLY SET UP IN A VM ALONG WITH NO OTHER UPNP-ENABLED ROUTERS
//    public void testPortMapping() throws Throwable {
//        Set<UpnpIgdService> services = UpnpIgdDiscovery.discover();
//        
//        UpnpIgdService service = services.iterator().next();
//        
//        PortMapperEventListener listener = Mockito.mock(PortMapperEventListener.class);
//        try (PortMapper mapper = new UpnpIgdPortMapper(service, listener)) {
//            MappedPort mappedPort = mapper.mapPort(PortType.TCP, 12345, 10L);
//            
//            for (int i = 0; i < 3; i++) {
//                Thread.sleep(5000L);
//                System.out.println("Refreshing...");
//                mapper.refreshPort(mappedPort, 10L);
//            }
//            
//            Thread.sleep(20000L);
//        }
//        
//        Mockito.verify(listener).resetRequired(Mockito.anyString());
//    }
}
