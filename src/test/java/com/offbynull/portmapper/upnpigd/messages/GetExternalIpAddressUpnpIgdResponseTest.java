package com.offbynull.portmapper.upnpigd.messages;

import java.net.InetAddress;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GetExternalIpAddressUpnpIgdResponseTest {

    @Test
    public void mustParseResponse() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:GetExternalIPAddressResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "<NewExternalIPAddress>10.0.0.1</NewExternalIPAddress>"
                + "</u:GetExternalIPAddressResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetExternalIpAddressUpnpIgdResponse resp = new GetExternalIpAddressUpnpIgdResponse(buffer);

        assertEquals(InetAddress.getByAddress(new byte[]{10, 0, 0, 1}), resp.getIpAddress());
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
                + "<u:GetExternalIPAddressResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "</u:GetExternalIPAddressResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetExternalIpAddressUpnpIgdResponse resp = new GetExternalIpAddressUpnpIgdResponse(buffer);

        resp.getIpAddress();
    }

    @Test(expected=IllegalStateException.class)
    public void mustFailToAccessExternalIpIfBad() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:GetExternalIPAddressResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "<NewExternalIPAddress>gggggggggggggggg</NewExternalIPAddress>"
                + "</u:GetExternalIPAddressResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetExternalIpAddressUpnpIgdResponse resp = new GetExternalIpAddressUpnpIgdResponse(buffer);

        resp.getIpAddress();
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
                + "<u:GetExternalIPAddressResponse xmdasdasdusd=\"sasdurnasscdhemas-asdasdasdupnp-org:service:WANPPPConnection:1\">"
                + "<NewExternalIPAddress>10.0.0.1</NewExternalIPAddress>"
                + "</u:GetExternalIPAddressResponse>"
                + "</s:Bodyasdasdasdasd>"
                + "</s:Enveloasdasdasdsd>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>pe>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetExternalIpAddressUpnpIgdResponse resp = new GetExternalIpAddressUpnpIgdResponse(buffer);

        assertEquals(InetAddress.getByAddress(new byte[]{10, 0, 0, 1}), resp.getIpAddress());
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
                + "<u:GetExternalIPAddressResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "<NewExternalIPAddress>10.0.0.1</NewExternalIPAddress>"
                + "</u:GetExternalIPAddressResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        GetExternalIpAddressUpnpIgdResponse resp = new GetExternalIpAddressUpnpIgdResponse(buffer);
    }

}
