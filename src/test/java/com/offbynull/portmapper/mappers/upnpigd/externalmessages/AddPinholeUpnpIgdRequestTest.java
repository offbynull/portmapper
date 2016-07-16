package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import com.offbynull.portmapper.mapper.PortType;
import java.net.InetAddress;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AddPinholeUpnpIgdRequestTest {

    @Test
    public void mustGenerateTcpRequest() throws Exception {
        // NOTE: technically port mapping services should not be dealing with IPv4 (firewall requires IPv6) -- but allow it anyways because
        // some routers may not follow the spec
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4}), 15, InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 12345, PortType.TCP,
                1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#AddPinhole\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 438\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">"
                + "<soap:Body>"
                + "<u:AddPinhole xmlns:u=\"service:type\">"
                + "<RemoteHost>::ffff:102:304</RemoteHost>"
                + "<RemotePort>15</RemotePort>"
                + "<InternalClient>::ffff:506:708</InternalClient>"
                + "<InternalPort>12345</InternalPort>"
                + "<Protocol>6</Protocol>"
                + "<LeaseTime>1000</LeaseTime>"
                + "</u:AddPinhole>"
                + "</soap:Body>"
                + "</soap:Envelope>",
                bufferText);
    }

    @Test
    public void mustGenerateUdpRequest() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type",
                InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 15,
                InetAddress.getByAddress(new byte[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16}), 12345,
                PortType.UDP, 1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#AddPinhole\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 481\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">"
                + "<soap:Body>"
                + "<u:AddPinhole xmlns:u=\"service:type\">"
                + "<RemoteHost>102:304:506:708:90a:b0c:d0e:f10</RemoteHost>"
                + "<RemotePort>15</RemotePort>"
                + "<InternalClient>fffe:fdfc:fbfa:f9f8:f7f6:f5f4:f3f2:f1f0</InternalClient>"
                + "<InternalPort>12345</InternalPort>"
                + "<Protocol>17</Protocol>"
                + "<LeaseTime>1000</LeaseTime>"
                + "</u:AddPinhole>"
                + "</soap:Body>"
                + "</soap:Envelope>",
                bufferText
        );
    }

    @Test
    public void mustGenerateRequestWithWildcardAddressesAndPorts() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type",
                null, 0, null, 0, PortType.UDP, 1000);
        String bufferText = new String(req.dump(), "US-ASCII");

        assertEquals("POST /controllink HTTP/1.1\r\n"
                + "Host: fake\r\n"
                + "Content-Type: text/xml\r\n"
                + "SOAPAction: service:type#AddPinhole\r\n"
                + "Connection: Close\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Pragma: no-cache\r\n"
                + "Content-Length: 406\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\">"
                + "<soap:Body>"
                + "<u:AddPinhole xmlns:u=\"service:type\">"
                + "<RemoteHost></RemoteHost>"
                + "<RemotePort>0</RemotePort>"
                + "<InternalClient></InternalClient>"
                + "<InternalPort>0</InternalPort>"
                + "<Protocol>17</Protocol>"
                + "<LeaseTime>1000</LeaseTime>"
                + "</u:AddPinhole>"
                + "</soap:Body>"
                + "</soap:Envelope>",
                bufferText);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenInternalPortIsOutOfRange() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type", null, 0,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 100000, PortType.TCP, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenExtenralPortIsOutOfRange() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type", null, 100000,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 0, PortType.TCP, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToGenerateWhenLeaseTimeIsOutOfRange() throws Exception {
        AddPinholeUpnpIgdRequest req = new AddPinholeUpnpIgdRequest("fake", "/controllink", "service:type", null, 0,
                InetAddress.getByAddress(new byte[]{5, 6, 7, 8}), 1000, PortType.TCP, -1);
    }

}
