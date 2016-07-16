package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UpdatePinholeUpnpIgdRequestTest {

    @Test
    public void mustGenerateRequest() throws Exception {
        UpdatePinholeUpnpIgdRequest req = new UpdatePinholeUpnpIgdRequest("fake", "/controllink", "service:type", "12345", 1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#UpdatePinhole\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 310\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" soap:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<soap:Body>"
                + "<u:UpdatePinhole xmlns:u=\"service:type\">"
                + "<UniqueID>12345</UniqueID>"
                + "<NewLeaseTime>1000</NewLeaseTime>"
                + "</u:UpdatePinhole>"
                + "</soap:Body>"
                + "</soap:Envelope>",
                bufferText);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenLeaseTimeIsOutOfRange() throws Exception {
        UpdatePinholeUpnpIgdRequest req = new UpdatePinholeUpnpIgdRequest("fake", "/controllink", "service:type", "12345", 0);
    }
}
