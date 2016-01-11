package com.offbynull.portmapper.helpers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class NetworkUtilsTest {

    @Test
    public void mustCreateInetAddress() throws UnknownHostException {
        InetAddress actual = NetworkUtils.toAddress("127.0.0.1");
        assertEquals(InetAddress.getByName("127.0.0.1"), actual);
    }

    @Test
    public void mustConvertToIpv6AddressWhenIpv4() throws UnknownHostException {
        String actual = NetworkUtils.toIpv6AddressString(InetAddress.getByName("127.0.0.1"));
        assertEquals("::ffff:7f00:1", actual);
    }

    @Test
    public void mustConvertToIpv6AddressWhenIpv6() throws UnknownHostException {
        String actual = NetworkUtils.toIpv6AddressString(InetAddress.getByName("1111::1111"));
        assertEquals("1111:0:0:0:0:0:0:1111", actual);
    }

    @Test
    public void mustConvertBytesToIpv4Address() throws UnknownHostException {
        InetAddress actual = NetworkUtils.convertBytesToAddress(new byte[] {127, 0, 0, 1});
        assertEquals(InetAddress.getByName("127.0.0.1"), actual);
    }

    @Test
    public void mustConvertBytesToIpv4AddressWhenIpv4MappedToIpv6() throws UnknownHostException {
        InetAddress actual = NetworkUtils.convertBytesToAddress(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 127, 0, 0, 1});
        assertEquals(InetAddress.getByName("127.0.0.1"), actual);
    }

    @Test
    public void mustConvertBytesToIpv6Address() throws UnknownHostException {
        InetAddress actual = NetworkUtils.convertBytesToAddress(new byte[] {17, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 17});
        assertEquals(InetAddress.getByName("1111::1111"), actual);
    }
    
    @Test
    public void mustConvertToIpv6BytesWhenIpv4() throws UnknownHostException {
        byte[] actual = NetworkUtils.convertAddressToIpv6Bytes(InetAddress.getByName("127.0.0.1"));
        assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 127, 0, 0, 1}, actual);
    }
    
    @Test
    public void mustConvertToIpv6BytesWhenIpv6() throws UnknownHostException {
        byte[] actual = NetworkUtils.convertAddressToIpv6Bytes(InetAddress.getByName("1111::1111"));
        assertArrayEquals(new byte[] {17, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 17}, actual);
    }
}
