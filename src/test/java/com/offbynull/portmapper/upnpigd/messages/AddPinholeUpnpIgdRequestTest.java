package com.offbynull.portmapper.upnpigd.messages;

import java.net.InetAddress;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AddPinholeUpnpIgdRequestTest {

    @Test
    public void mustGenerateTcpRequest() throws Exception {
        // NOTE: technically port mapping services should not be dealing with IPv4 (firewall requires IPv6) -- but allow it anyways because
        // some routers may not follow the spec
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 15, InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 12345, Protocol.TCP,
                1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "SOAPAction: service:type#AddPinhole\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Connection: Close\r\n"
                + "Host: fake\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Type: text/xml\r\n"
                + "Content-Length: 458\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>\r\n"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n"
                + "<soap:Body>\r\n"
                + "<u:AddPinhole xmlns:u=\"service:type\">\r\n"
                + "<RemoteHost>1.2.3.4</RemoteHost>\r\n"
                + "<RemotePort>15</RemotePort>\r\n"
                + "<InternalClient>5.6.7.8</InternalClient>\r\n"
                + "<InternalPort>12345</InternalPort>\r\n"
                + "<Protocol>6</Protocol>\r\n"
                + "<LeaseDuration>1000</LeaseDuration>\r\n"
                + "</u:AddPinhole>\r\n"
                + "</soap:Body>\r\n"
                + "</soap:Envelope>\r\n",
                bufferText);
    }

    @Test
    public void mustGenerateUdpRequest() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 15,
                InetAddress.getByAddress(new byte[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16}), 12345,
                Protocol.UDP, 1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "SOAPAction: service:type#AddPinhole\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Connection: Close\r\n"
                + "Host: fake\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Type: text/xml\r\n"
                + "Content-Length: 515\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>\r\n"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n"
                + "<soap:Body>\r\n"
                + "<u:AddPinhole xmlns:u=\"service:type\">\r\n"
                + "<RemoteHost>102:304:506:708:90a:b0c:d0e:f10</RemoteHost>\r\n"
                + "<RemotePort>15</RemotePort>\r\n"
                + "<InternalClient>fffe:fdfc:fbfa:f9f8:f7f6:f5f4:f3f2:f1f0</InternalClient>\r\n"
                + "<InternalPort>12345</InternalPort>\r\n"
                + "<Protocol>17</Protocol>\r\n"
                + "<LeaseDuration>1000</LeaseDuration>\r\n"
                + "</u:AddPinhole>\r\n"
                + "</soap:Body>\r\n"
                + "</soap:Envelope>\r\n",
                bufferText);
    }

    @Test
    public void mustGenerateRequestWithWildcardAddressesAndPorts() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type",
                null, 0, null, 0, Protocol.UDP, 1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "SOAPAction: service:type#AddPinhole\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Connection: Close\r\n"
                + "Host: fake\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Type: text/xml\r\n"
                + "Content-Length: 440\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>\r\n"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">\r\n"
                + "<soap:Body>\r\n"
                + "<u:AddPinhole xmlns:u=\"service:type\">\r\n"
                + "<RemoteHost></RemoteHost>\r\n"
                + "<RemotePort>0</RemotePort>\r\n"
                + "<InternalClient></InternalClient>\r\n"
                + "<InternalPort>0</InternalPort>\r\n"
                + "<Protocol>17</Protocol>\r\n"
                + "<LeaseDuration>1000</LeaseDuration>\r\n"
                + "</u:AddPinhole>\r\n"
                + "</soap:Body>\r\n"
                + "</soap:Envelope>\r\n",
                bufferText);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenInternalPortIsOutOfRange() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type", null, 0,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 100000, Protocol.TCP, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenExtenralPortIsOutOfRange() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type", null, 100000,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 0, Protocol.TCP, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenLeaseTimeIsOutOfRange() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type", null, 0,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 1000, Protocol.TCP, -1);
    }

}
