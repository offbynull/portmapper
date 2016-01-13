package com.offbynull.portmapper.upnpigd.externalmessages;

import com.offbynull.portmapper.PortType;
import java.net.InetAddress;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AddPortMappingUpnpIgdRequestTest {

    @Test
    public void mustGenerateTcpRequest() throws Exception {
        AddPortMappingUpnpIgdRequest req = new AddPortMappingUpnpIgdRequest("fake", "/controllink",
                "service:type", InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 15, PortType.TCP, 12345,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), true, "desc", 1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#AddPortMapping\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 597\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>\r\n"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n"
                + "<soap:Body>\r\n"
                + "<u:AddPortMapping xmlns:u=\"service:type\">\r\n"
                + "<NewRemoteHost>1.2.3.4</NewRemoteHost>\r\n"
                + "<NewExternalPort>15</NewExternalPort>\r\n"
                + "<NewProtocol>TCP</NewProtocol>\r\n"
                + "<NewInternalPort>12345</NewInternalPort>\r\n"
                + "<NewInternalClient>5.6.7.8</NewInternalClient>\r\n"
                + "<NewEnabled>1</NewEnabled>\r\n"
                + "<NewPortMappingDescription>desc</NewPortMappingDescription>\r\n"
                + "<NewLeaseDuration>1000</NewLeaseDuration>\r\n"
                + "</u:AddPortMapping>\r\n"
                + "</soap:Body>\r\n"
                + "</soap:Envelope>\r\n",
                bufferText);
    }

    @Test
    public void mustGenerateUdpRequest() throws Exception {
        // NOTE: technically port mapping services should not be dealing with IPv6 (only the firewall supports IPv6) -- but allow it anyways
        // because some routers may not follow the spec
        AddPortMappingUpnpIgdRequest req = new AddPortMappingUpnpIgdRequest("fake", "/controllink",
                "service:type", InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 15,
                PortType.UDP, 12345,
                InetAddress.getByAddress(new byte[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16}), false, "desc",
                1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        System.out.println(bufferText);
        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#AddPortMapping\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 653\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>\r\n"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n"
                + "<soap:Body>\r\n"
                + "<u:AddPortMapping xmlns:u=\"service:type\">\r\n"
                + "<NewRemoteHost>102:304:506:708:90a:b0c:d0e:f10</NewRemoteHost>\r\n"
                + "<NewExternalPort>15</NewExternalPort>\r\n"
                + "<NewProtocol>UDP</NewProtocol>\r\n"
                + "<NewInternalPort>12345</NewInternalPort>\r\n"
                + "<NewInternalClient>fffe:fdfc:fbfa:f9f8:f7f6:f5f4:f3f2:f1f0</NewInternalClient>\r\n"
                + "<NewEnabled>0</NewEnabled>\r\n"
                + "<NewPortMappingDescription>desc</NewPortMappingDescription>\r\n"
                + "<NewLeaseDuration>1000</NewLeaseDuration>\r\n"
                + "</u:AddPortMapping>\r\n"
                + "</soap:Body>\r\n"
                + "</soap:Envelope>\r\n",
                bufferText);
    }

    @Test
    public void mustGenerateRequestWithWildcardRemoteAddressAndPortAndLeaseTime() throws Exception {
        AddPortMappingUpnpIgdRequest req = new AddPortMappingUpnpIgdRequest("fake", "/controllink",
                "service:type", null, 0, PortType.TCP, 12345,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), true, "desc", 0);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#AddPortMapping\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 586\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>\r\n"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n"
                + "<soap:Body>\r\n"
                + "<u:AddPortMapping xmlns:u=\"service:type\">\r\n"
                + "<NewRemoteHost></NewRemoteHost>\r\n"
                + "<NewExternalPort>0</NewExternalPort>\r\n"
                + "<NewProtocol>TCP</NewProtocol>\r\n"
                + "<NewInternalPort>12345</NewInternalPort>\r\n"
                + "<NewInternalClient>5.6.7.8</NewInternalClient>\r\n"
                + "<NewEnabled>1</NewEnabled>\r\n"
                + "<NewPortMappingDescription>desc</NewPortMappingDescription>\r\n"
                + "<NewLeaseDuration>0</NewLeaseDuration>\r\n"
                + "</u:AddPortMapping>\r\n"
                + "</soap:Body>\r\n"
                + "</soap:Envelope>\r\n",
                bufferText);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenInternalPortIsWildcard() throws Exception {
        AddPortMappingUpnpIgdRequest req = new AddPortMappingUpnpIgdRequest("fake", "/controllink",
                "service:type", null, 0, PortType.TCP, 0,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), true, "desc", 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenInternalPortIsOutOfRange() throws Exception {
        AddPortMappingUpnpIgdRequest req = new AddPortMappingUpnpIgdRequest("fake", "/controllink",
                "service:type", null, 0, PortType.TCP, 100000,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), true, "desc", 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenLeaseTimeIsOutOfRange() throws Exception {
        AddPortMappingUpnpIgdRequest req = new AddPortMappingUpnpIgdRequest("fake", "/controllink",
                "service:type", null, 0, PortType.TCP, 1000,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), true, "desc", -1);
    }

}
