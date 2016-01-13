package com.offbynull.portmapper.upnpigd.externalmessages;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AddPinholeUpnpIgdResponseTest {

    @Test
    public void mustParseResponse() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:AddPinholeResponse xmlns:u=\"urn:schemas-upnp-org:service:WANIPv6FirewallControl:1\">"
                + "<UniqueID>12345</UniqueID>"
                + "</u:AddPinholeResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        AddPinholeUpnpIgdResponse resp = new AddPinholeUpnpIgdResponse(buffer);

        assertEquals("12345", resp.getUniqueId());
    }

    @Test(expected=IllegalStateException.class)
    public void mustFailToAccessExternalIpIfMissing() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:AddPinholeResponse xmlns:u=\"urn:schemas-upnp-org:service:WANIPv6FirewallControl:1\">"
                + "</u:AddPinholeResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        AddPinholeUpnpIgdResponse resp = new AddPinholeUpnpIgdResponse(buffer);

        resp.getUniqueId();
    }

    @Test
    public void mustParseResponseWithBadXml() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xmasdsdl sdfsdfsdfion=\"1.0\"?><<<<<<<<<<<"
                + "<s:Envelopsdfsdfe xmlns:s=\"hafasdasdtp://schemas.xmlsoap.org/soadp/enastyle=\"http://sdaschemas.xmlsoap.dorg/soap/encoding/\">"
                + "<s:Basdody>"
                + "<u:AddPinholeResponse xmdasdasdusd=\"sasdurnasscdhemas-asdasdasdupnp-org:service:WANIPv6FirewallControl:1\">"
                + "<UniqueID>12345</UniqueID>"
                + "</u:AddPinholeResponse>"
                + "</s:Bodyasdasdasdasd>"
                + "</s:Enveloasdasdasdsd>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>pe>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        AddPinholeUpnpIgdResponse resp = new AddPinholeUpnpIgdResponse(buffer);

        assertEquals("12345", resp.getUniqueId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailOnError() throws Exception {
        String bufferStr
                = "HTTP/1.1 500 error\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:AddPinholeResponse xmlns:u=\"urn:schemas-upnp-org:service:WANIPv6FirewallControl:1\">"
                + "<UniqueID>12345</UniqueID>"
                + "</u:AddPinholeResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        AddPinholeUpnpIgdResponse resp = new AddPinholeUpnpIgdResponse(buffer);
    }

}
