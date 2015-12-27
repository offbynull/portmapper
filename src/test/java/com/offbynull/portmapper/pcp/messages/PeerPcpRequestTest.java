package com.offbynull.portmapper.pcp.messages;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PeerPcpRequestTest {
    private static final InetAddress IPV6_TEST_ADDRESS;
    private static final InetAddress IPV4_TEST_ADDRESS;
    private static final InetAddress IPV4_TEST_ADDRESS_2;
    static {
        try {
            IPV6_TEST_ADDRESS = InetAddress.getByAddress(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 });
            IPV4_TEST_ADDRESS = InetAddress.getByAddress(new byte[] { 1, 2, 3, 4 });
            IPV4_TEST_ADDRESS_2 = InetAddress.getByAddress(new byte[] { 5, 6, 7, 8 });
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe);
        }
    }
    
    private static byte[] nonce = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }; // DONT CHANGE CONTENTS IN TEST, POTENTIAL THREADING
                                                                                        // ISSUE

    @Test
    public void mustProperlyCreatePacketWithIpv6() throws Exception {
        PeerPcpRequest req = new PeerPcpRequest(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS_2);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getSuggestedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(4, req.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getRemotePeerIpAddress());
        assertEquals(IPV4_TEST_ADDRESS_2, req.getInternalIp());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithIpv4() throws Exception {
        PeerPcpRequest req = new PeerPcpRequest(nonce, 1, 2, 3, IPV4_TEST_ADDRESS, 4, IPV6_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS_2);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getSuggestedExternalPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(4, req.getRemotePeerPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getRemotePeerIpAddress());
        assertEquals(IPV4_TEST_ADDRESS_2, req.getInternalIp());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroInternalPort() throws Exception {
        PeerPcpRequest req = new PeerPcpRequest(nonce, 1, 0, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroRemotePeerPort() throws Exception {
        PeerPcpRequest req = new PeerPcpRequest(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 0, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithZeroIANA() throws Exception {
        PeerPcpRequest req = new PeerPcpRequest(nonce, 0, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS_2);
    }

    @Test
    public void mustProperlyCreatePacketWithZeroSuggestedExternalPort() throws Exception {
        PeerPcpRequest req = new PeerPcpRequest(nonce, 1, 2, 0, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS_2);
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(0, req.getSuggestedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(4, req.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getRemotePeerIpAddress());
        assertEquals(IPV4_TEST_ADDRESS_2, req.getInternalIp());
        assertEquals(Collections.emptyList(), req.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithOptions() throws Exception {
        PeerPcpRequest req = new PeerPcpRequest(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL, IPV4_TEST_ADDRESS_2,
                new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        assertArrayEquals(nonce, req.getMappingNonce());
        assertEquals(2, req.getOp());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
        assertEquals(1, req.getProtocol());
        assertEquals(2, req.getInternalPort());
        assertEquals(3, req.getSuggestedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, req.getSuggestedExternalIpAddress());
        assertEquals(4, req.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, req.getRemotePeerIpAddress());
        assertEquals(IPV4_TEST_ADDRESS_2, req.getInternalIp());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), req.getOptions());
    }

    @Test
    public void mustParseCreatedPacket() {
        PeerPcpRequest origReq = new PeerPcpRequest(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL,
                IPV4_TEST_ADDRESS_2, new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        PeerPcpRequest parsedReq = new PeerPcpRequest(buffer);
        assertArrayEquals(nonce, parsedReq.getMappingNonce());
        assertEquals(2, parsedReq.getOp());
        assertEquals(0xFFFFFFFFL, parsedReq.getLifetime());
        assertEquals(1, parsedReq.getProtocol());
        assertEquals(2, parsedReq.getInternalPort());
        assertEquals(3, parsedReq.getSuggestedExternalPort());
        assertEquals(IPV6_TEST_ADDRESS, parsedReq.getSuggestedExternalIpAddress());
        assertEquals(4, parsedReq.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, parsedReq.getRemotePeerIpAddress());
        assertEquals(IPV4_TEST_ADDRESS_2, parsedReq.getInternalIp());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), parsedReq.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        PeerPcpRequest origReq = new PeerPcpRequest(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL,
                IPV4_TEST_ADDRESS_2, new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        PeerPcpRequest parsedReq = new PeerPcpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        PeerPcpRequest origReq = new PeerPcpRequest(nonce, 1, 2, 3, IPV6_TEST_ADDRESS, 4, IPV4_TEST_ADDRESS, 0xFFFFFFFFL,
                IPV4_TEST_ADDRESS_2, new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        PeerPcpRequest parsedReq = new PeerPcpRequest(buffer);
    }
}
