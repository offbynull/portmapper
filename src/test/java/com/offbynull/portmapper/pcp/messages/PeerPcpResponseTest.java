package com.offbynull.portmapper.pcp.messages;

import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PeerPcpResponseTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertArrayToIp(new byte[] {1, 2, 3, 4});
    private static final InetAddress IPV6_TEST_ADDRESS
            = NetworkUtils.convertArrayToIp(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
    
    private static byte[] nonce = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }; // DONT CHANGE CONTENTS IN TEST, POTENTIAL THREADING
                                                                                        // ISSUE

    @Test
    public void mustProperlyCreatePacketWithIpv6() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(0xFFFFFFFEL, req.getEpochTime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(4, req.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getRemotePeerIpAddress());
        assertEquals(0, req.getResultCode());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithIpv4() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 1, 2, 3, IPV4_TEST_ADDRESS, 4, IPV6_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(0xFFFFFFFEL, req.getEpochTime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getAssignedExternalPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(4, req.getRemotePeerPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getRemotePeerIpAddress());
        assertEquals(0, req.getResultCode());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroInternalPort() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 1, 0, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
    }

    @Test
    public void mustProperlyCreatePacketWithZeroAssignedExternalPortOnFailure() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 1, 2, 0, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 1, 0xFFFFFFFFL, 0xFFFFFFFEL);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(1, req.getResultCode());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(0xFFFFFFFEL, req.getEpochTime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(0, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroAssignedExternalPortOnSuccess() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 1, 2, 0, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroIANA() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 0, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroRemotePeerPort() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 0, 2, 3, IPV6_TEST_ADDRESS, 0, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL);
    }

    @Test
    public void mustProperlyCreatePacketWithOptions() throws Exception {
        PeerPcpResponse req = new PeerPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(0xFFFFFFFEL, req.getEpochTime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getAssignedExternalIpAddress());
        assertEquals(4, req.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getRemotePeerIpAddress());
        assertEquals(0, req.getResultCode());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), req.getOptions());
    }

    @Test
    public void mustParseCreatedPacket() {
        PeerPcpResponse origReq = new PeerPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        PeerPcpResponse parsedReq = new PeerPcpResponse(buffer);
        assertArrayEquals(nonce, parsedReq.getMappingNonce());
        assertEquals(2, parsedReq.getOp());
        assertEquals(0xFFFFFFFFL, parsedReq.getLifetime());
        assertEquals(0xFFFFFFFEL, parsedReq.getEpochTime());
        assertEquals(1, parsedReq.getProtocol());
        assertEquals(2, parsedReq.getInternalPort());
        assertEquals(3, parsedReq.getAssignedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, parsedReq.getAssignedExternalIpAddress());
        assertEquals(4, parsedReq.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, parsedReq.getRemotePeerIpAddress());
        assertEquals(0, parsedReq.getResultCode());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), parsedReq.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        PeerPcpResponse origReq = new PeerPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        PeerPcpResponse parsedReq = new PeerPcpResponse(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        PeerPcpResponse origReq = new PeerPcpResponse(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0, 0xFFFFFFFFL, 0xFFFFFFFEL,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        PeerPcpResponse parsedReq = new PeerPcpResponse(buffer);
    }
}
