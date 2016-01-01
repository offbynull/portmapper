package com.offbynull.portmapper.upnpigd.messages;

import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ServiceDiscoveryUpnpIgdResponseTest {

    @Test
    public void mustProperlyParseBuffer() throws Exception {
        // taken from http://www.upnp-hacks.org/upnp.html
        byte[] buffer = ("HTTP/1.1 200 OK\r\n"
                + "CACHE-CONTROL:max-age=1800\r\n"
                + "EXT:\r\n"
                + "LOCATION: http://10.0.0.138:80/IGD.xml\r\n"
                + "SERVER:SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)\r\n"
                + "ST: urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "USN:uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "\r\n")
                .getBytes("US-ASCII");
        ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(buffer);
        
        assertEquals(URI.create("http://10.0.0.138:80/IGD.xml"), resp.getLocation());
        assertEquals("SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)", resp.getServer());
        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getServiceType());
        assertEquals("uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getUsn());
    }

    @Test(expected = IllegalStateException.class)
    public void mustProperlyParseBufferWithoutStHeaderButFailOnAccess() throws Exception {
        byte[] buffer = ("HTTP/1.1 200 OK\r\n"
                + "CACHE-CONTROL:max-age=1800\r\n"
                + "EXT:\r\n"
                + "LOCATION: http://10.0.0.138:80/IGD.xml\r\n"
                + "SERVER:SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)\r\n"
                + "USN:uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "\r\n")
                .getBytes("US-ASCII");
        ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(buffer);
        
        assertEquals(URI.create("http://10.0.0.138:80/IGD.xml"), resp.getLocation());
        assertEquals("SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)", resp.getServer());
        assertEquals("uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getUsn());
        resp.getServiceType();
    }

    @Test(expected = IllegalStateException.class)
    public void mustProperlyParseBufferWithoutLocationHeaderButFailOnAccess() throws Exception {
        byte[] buffer = ("HTTP/1.1 200 OK\r\n"
                + "CACHE-CONTROL:max-age=1800\r\n"
                + "EXT:\r\n"
                + "SERVER:SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)\r\n"
                + "ST: urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "USN:uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "\r\n")
                .getBytes("US-ASCII");
        ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(buffer);
        
        assertEquals("SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)", resp.getServer());
        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getServiceType());
        assertEquals("uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getUsn());
        resp.getLocation();
    }

    @Test(expected = IllegalStateException.class)
    public void mustProperlyParseBufferWithFaultyLocationHeaderButFailOnAccess() throws Exception {
        byte[] buffer = ("HTTP/1.1 200 OK\r\n"
                + "CACHE-CONTROL:max-age=1800\r\n"
                + "EXT:\r\n"
                + "LOCATION: example.com/file[/].html\r\n"
                + "SERVER:SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)\r\n"
                + "ST: urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "USN:uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "\r\n")
                .getBytes("US-ASCII");
        ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(buffer);
        
        assertEquals("SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)", resp.getServer());
        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getServiceType());
        assertEquals("uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getUsn());
        resp.getLocation();
    }

    @Test
    public void mustProperlyParseBufferWithoutServerHeader() throws Exception {
        byte[] buffer = ("HTTP/1.1 200 OK\r\n"
                + "CACHE-CONTROL:max-age=1800\r\n"
                + "EXT:\r\n"
                + "LOCATION: http://10.0.0.138:80/IGD.xml\r\n"
                + "ST: urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "USN:uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "\r\n")
                .getBytes("US-ASCII");
        ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(buffer);
        
        assertEquals(URI.create("http://10.0.0.138:80/IGD.xml"), resp.getLocation());
        assertNull(resp.getServer());
        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getServiceType());
        assertEquals("uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getUsn());
    }

    @Test
    public void mustProperlyParseBufferAndIgnoreContent() throws Exception {
        byte[] buffer = ("HTTP/1.1 200 OK\r\n"
                + "CACHE-CONTROL:max-age=1800\r\n"
                + "EXT:\r\n"
                + "LOCATION: http://10.0.0.138:80/IGD.xml\r\n"
                + "SERVER:SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)\r\n"
                + "ST: urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "USN:uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1\r\n"
                + "Content-Length: 6\r\n"
                + "\r\n"
                + "abcdef\r\n")
                .getBytes("US-ASCII");
        ServiceDiscoveryUpnpIgdResponse resp = new ServiceDiscoveryUpnpIgdResponse(buffer);
        
        assertEquals(URI.create("http://10.0.0.138:80/IGD.xml"), resp.getLocation());
        assertEquals("SpeedTouch 510 4.0.0.9.0 UPnP/1.0 (DG233B00011961)", resp.getServer());
        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getServiceType());
        assertEquals("uuid:UPnP-SpeedTouch510::urn:schemas-upnp-org:service:WANPPPConnection:1", resp.getUsn());
    }

}
