package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DeletePinholeUpnpIgdRequestTest {

    @Test
    public void mustGenerateRequest() throws Exception {
        DeletePinholeUpnpIgdRequest req = new DeletePinholeUpnpIgdRequest("fake", "/controllink", "service:type", "12345");
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#DeletePinhole\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 277\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" soap:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<soap:Body>"
                + "<u:DeletePinhole xmlns:u=\"service:type\">"
                + "<UniqueID>12345</UniqueID>"
                + "</u:DeletePinhole>"
                + "</soap:Body>"
                + "</soap:Envelope>",
                bufferText);
    }

}
