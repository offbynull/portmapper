package com.offbynull.portmapper.pcp.externalmessages;

import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MapPcpResponseTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4});
    private static final InetAddress IPV6_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
    
    private static byte[] nonce = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }; // DONT CHANGE CONTENTS IN TEST, POTENTIAL THREADING
                                                                                        // ISSUE

    @Test
    public void mustProperlyCreatePacket() throws Exception {
        MapPcpResponse req = new MapPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0, req.getResultCode());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(0, req.getResultCode());
        assertEquals(0xFFFFFFFEL, req.getEpochTime());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithZeroInternalPort() throws Exception {
        MapPcpResponse req = new MapPcpResponse(nonce, 1, 0, 3, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0, req.getResultCode());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(0, req.getInternalPort());
        assertEquals(3, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithZeroAssignedPortOnFailure() throws Exception {
        MapPcpResponse req = new MapPcpResponse(nonce, 1, 0, 0, IPV6_TEST_ADDRESS, 1, 0xFFFFFFFFL, 0xFFFFFFFEL);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(1, req.getResultCode());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(0, req.getInternalPort());
        assertEquals(0, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroAssignedPortOnSuccess() throws Exception {
        MapPcpResponse req = new MapPcpResponse(nonce, 1, 0, 0, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
    }

    @Test
    public void mustProperlyCreatePacketWithZeroIANA() throws Exception {
        MapPcpResponse req = new MapPcpResponse(nonce, 0, 0, 3, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0, req.getResultCode());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(0, req.getProtocol());
        assertEquals(0, req.getInternalPort());
        assertEquals(3, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithOptions() throws Exception {
        MapPcpResponse req = new MapPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(1, req.getOp());
        assertEquals(0, req.getResultCode());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(0, req.getResultCode());
        assertEquals(0xFFFFFFFEL, req.getEpochTime());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV4_TEST_ADDRESS)), req.getOptions());
    }

    @Test
    public void mustParseCreatedPacket() {
        MapPcpResponse origReq = new MapPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        MapPcpResponse parsedReq = new MapPcpResponse(buffer);
        assertArrayEquals(nonce, parsedReq.getMappingNonce());
        assertEquals(1, parsedReq.getOp());
        assertEquals(0, parsedReq.getResultCode());
        assertEquals(0xFFFFFFFFL, parsedReq.getLifetime());
        assertEquals(1, parsedReq.getProtocol());
        assertEquals(2, parsedReq.getInternalPort());
        assertEquals(3, parsedReq.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, parsedReq.getAssignedExternalIpAddress());
        assertEquals(0, parsedReq.getResultCode());
        assertEquals(0xFFFFFFFEL, parsedReq.getEpochTime());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV4_TEST_ADDRESS)), parsedReq.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        MapPcpResponse origReq = new MapPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        MapPcpResponse parsedReq = new MapPcpResponse(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        MapPcpResponse origReq = new MapPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        MapPcpResponse parsedReq = new MapPcpResponse(buffer);
    }
}
