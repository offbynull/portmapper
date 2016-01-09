package com.offbynull.portmapper.pcp.externalmessages;

import com.offbynull.portmapper.pcp.externalmessages.ThirdPartyPcpOption;
import com.offbynull.portmapper.pcp.externalmessages.PreferFailurePcpOption;
import com.offbynull.portmapper.pcp.externalmessages.MapPcpRequest;
import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MapPcpRequestTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4});
    private static final InetAddress IPV6_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
    
    private static byte[] nonce = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }; // DONT CHANGE CONTENTS IN TEST, POTENTIAL THREADING
                                                                                        // ISSUE

    @Test
    public void mustProperlyCreatePacketWithIpv4() throws Exception {
        MapPcpRequest req = new MapPcpRequest(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getSuggestedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(IPV4_TEST_ADDRESS, req.getInternalIp());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithIpv6() throws Exception {
        MapPcpRequest req = new MapPcpRequest(nonce, 1, 2, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV6_TEST_ADDRESS);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(0, req.getSuggestedExternalPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(IPV6_TEST_ADDRESS, req.getInternalIp());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithZeroInternalPort() throws Exception {
        MapPcpRequest req = new MapPcpRequest(nonce, 1, 0, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV6_TEST_ADDRESS);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(0, req.getInternalPort());
        assertEquals(0, req.getSuggestedExternalPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(IPV6_TEST_ADDRESS, req.getInternalIp());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithZeroIANA() throws Exception {
        MapPcpRequest req = new MapPcpRequest(nonce, 0, 0, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV6_TEST_ADDRESS);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(0, req.getProtocol());
        assertEquals(0, req.getInternalPort());
        assertEquals(0, req.getSuggestedExternalPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(IPV6_TEST_ADDRESS, req.getInternalIp());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithOptions() throws Exception {
        MapPcpRequest req = new MapPcpRequest(nonce, 1, 2, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV6_TEST_ADDRESS,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(0, req.getSuggestedExternalPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(IPV6_TEST_ADDRESS, req.getInternalIp());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), req.getOptions());
    }

    @Test
    public void mustParseCreatedPacket() {
        MapPcpRequest origReq = new MapPcpRequest(nonce, 1, 2, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV6_TEST_ADDRESS,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        MapPcpRequest parsedReq = new MapPcpRequest(buffer);
        assertArrayEquals(nonce, parsedReq.getMappingNonce());
        assertEquals(1, parsedReq.getOp());
        assertEquals(0xFFFFFFFFL, parsedReq.getLifetime());
        assertEquals(1, parsedReq.getProtocol());
        assertEquals(2, parsedReq.getInternalPort());
        assertEquals(0, parsedReq.getSuggestedExternalPort());
        assertEquals(IPV4_TEST_ADDRESS, parsedReq.getSuggestedExternalIpAddress());
        assertEquals(IPV6_TEST_ADDRESS, parsedReq.getInternalIp());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), parsedReq.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        MapPcpRequest origReq = new MapPcpRequest(nonce, 1, 2, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV6_TEST_ADDRESS,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        MapPcpRequest parsedReq = new MapPcpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        MapPcpRequest origReq = new MapPcpRequest(nonce, 1, 2, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV6_TEST_ADDRESS,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        MapPcpRequest parsedReq = new MapPcpRequest(buffer);
    }
}
