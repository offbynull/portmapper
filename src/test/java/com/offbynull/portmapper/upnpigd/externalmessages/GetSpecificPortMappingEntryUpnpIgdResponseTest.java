package com.offbynull.portmapper.upnpigd.externalmessages;

import com.offbynull.portmapper.upnpigd.externalmessages.GetSpecificPortMappingEntryUpnpIgdResponse;
import java.net.InetAddress;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GetSpecificPortMappingEntryUpnpIgdResponseTest {

    @Test
    public void mustParseResponse() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:GetSpecificPortMappingEntryResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "<NewInternalPort>200</NewInternalPort>"
                + "<NewInternalClient>10.0.0.1</NewInternalClient>"
                + "<NewEnabled>1</NewEnabled>"
                + "<NewPortMappingDescription>fffff</NewPortMappingDescription>"
                + "<NewLeaseDuration>2000</NewLeaseDuration>"
                + "</u:GetSpecificPortMappingEntryResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetSpecificPortMappingEntryUpnpIgdResponse resp = new GetSpecificPortMappingEntryUpnpIgdResponse(buffer);

        assertEquals(true, resp.getEnabled());
        assertEquals("fffff", resp.getDescription());
        assertEquals(InetAddress.getByAddress(new byte[]{10, 0, 0, 1}), resp.getInternalClient());
        assertEquals(200, resp.getInternalPort());
        assertEquals(2000L, (long) resp.getLeaseDuration());
    }

    @Test
    public void mustParseResponseWithBadXml() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Bodydfsdfsdfsf>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                + "<u:GetSpecificPortMappingEntryResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "<NewInternalPort>200</NewInternalPort>"
                + "<NewInternalClient>10.0.0.1</NewInternalClient>"
                + "<NewEnabled>1</NewEnabled>"
                + "<NewPortMappingDescription>fffff</NewPortMappingDescription>"
                + "<NewLeaseDuration>2000</NewLeaseDuration>"
                + "</u:GetSpecificPortMappingEntryResponse>"
                + "</";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetSpecificPortMappingEntryUpnpIgdResponse resp = new GetSpecificPortMappingEntryUpnpIgdResponse(buffer);

        assertEquals(true, resp.getEnabled());
        assertEquals("fffff", resp.getDescription());
        assertEquals(InetAddress.getByAddress(new byte[]{10, 0, 0, 1}), resp.getInternalClient());
        assertEquals(200, resp.getInternalPort());
        assertEquals(2000L, (long) resp.getLeaseDuration());
    }

    @Test(expected = IllegalStateException.class)
    public void mustFailWhenAccessingMissingValue() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:GetSpecificPortMappingEntryResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "<NewInternalPort>200</NewInternalPort>"
                + "<NewInternalClient>10.0.0.1</NewInternalClient>"
                + "<NewPortMappingDescription>fffff</NewPortMappingDescription>"
                + "<NewLeaseDuration>2000</NewLeaseDuration>"
                + "</u:GetSpecificPortMappingEntryResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetSpecificPortMappingEntryUpnpIgdResponse resp = new GetSpecificPortMappingEntryUpnpIgdResponse(buffer);

        resp.getEnabled();
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailOnError() throws Exception {
        String bufferStr
                = "HTTP/1.1 500 Bad\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:GetSpecificPortMappingEntryResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "<NewInternalPort>200</NewInternalPort>"
                + "<NewInternalClient>10.0.0.1</NewInternalClient>"
                + "<NewEnabled>1</NewEnabled>"
                + "<NewPortMappingDescription>fffff</NewPortMappingDescription>"
                + "<NewLeaseDuration>2000</NewLeaseDuration>"
                + "</u:GetSpecificPortMappingEntryResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetSpecificPortMappingEntryUpnpIgdResponse resp = new GetSpecificPortMappingEntryUpnpIgdResponse(buffer);

        resp.getEnabled();
    }

}
