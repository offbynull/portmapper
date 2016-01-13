package com.offbynull.portmapper.upnpigd.externalmessages;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GetExternalIpAddressUpnpIgdRequestTest {

    @Test
    public void mustGenerateRequest() throws Exception {
        GetExternalIpAddressUpnpIgdRequest req = new GetExternalIpAddressUpnpIgdRequest("fake", "/controllink", "service:type");
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#GetExternalIPAddress\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 276\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>\r\n"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n"
                + "<soap:Body>\r\n"
                + "<u:GetExternalIPAddress xmlns:u=\"service:type\">\r\n"
                + "</u:GetExternalIPAddress>\r\n"
                + "</soap:Body>\r\n"
                + "</soap:Envelope>\r\n",
                bufferText);
    }

}
