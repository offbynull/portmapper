package com.offbynull.portmapper.upnpigd.messages;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GetExternalIpAddressUpnpIgdRequestTest {

    @Test
    public void mustGenerateRequest() throws Exception {
        GetExternalIpAddressUpnpIgdRequest req = new GetExternalIpAddressUpnpIgdRequest("fake", "/controllink", "service:type");
        String bufferText = new String(req.dump(), "US-ASCII");

        System.out.println(bufferText);
        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "SOAPAction: service:type#GetExternalIPAddress\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Connection: Close\r\n"
                + "Host: fake\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Type: text/xml\r\n"
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
