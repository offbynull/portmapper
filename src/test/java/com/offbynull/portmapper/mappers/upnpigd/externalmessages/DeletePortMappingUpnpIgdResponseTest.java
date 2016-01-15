package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import org.junit.Test;

public class DeletePortMappingUpnpIgdResponseTest {

    @Test
    public void mustParseResponse() throws Exception {
        String bufferStr
                = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/xml\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>"
                + "<u:DeletePortMappingResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "</u:DeletePortMappingResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        DeletePortMappingUpnpIgdResponse resp = new DeletePortMappingUpnpIgdResponse(buffer);
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
                + "<u:DeletePortMappingResponse xmdasdasdusd=\"sasdurnasscdhemas-asdasdasdupnp-org:service:WANPPPConnection:1\">"
                + "</u:DeletePortMappingResponse>"
                + "</s:Bodyasdasdasdasd>"
                + "</s:Enveloasdasdasdsd>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>pe>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        DeletePortMappingUpnpIgdResponse resp = new DeletePortMappingUpnpIgdResponse(buffer);
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
                + "<u:DeletePortMappingResponse xmlns:u=\"urn:schemas-upnp-org:service:WANPPPConnection:1\">"
                + "</u:DeletePortMappingResponse>"
                + "</s:Body>"
                + "</s:Envelope>";
        byte[] buffer = bufferStr.getBytes("US-ASCII");
        DeletePortMappingUpnpIgdResponse resp = new DeletePortMappingUpnpIgdResponse(buffer);
    }

}
