package com.offbynull.portmapper.upnpigd.messages;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ServiceDescriptionUpnpIgdRequestTest {

    @Test
    public void mustGenerateRequest() throws Exception {
        ServiceDescriptionUpnpIgdRequest req = new ServiceDescriptionUpnpIgdRequest("192.168.0.1", "/");
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("GET / HTTP/1.1\r\n"
                + "Connection: Close\r\n"
                + "Host: 192.168.0.1\r\n"
                + "\r\n",
                bufferText);
    }

}
