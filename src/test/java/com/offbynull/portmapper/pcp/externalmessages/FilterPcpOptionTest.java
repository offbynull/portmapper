package com.offbynull.portmapper.pcp.externalmessages;

import com.offbynull.portmapper.pcp.externalmessages.FilterPcpOption;
import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FilterPcpOptionTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4});
    private static final InetAddress IPV6_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});

    @Test
    public void mustProperlyCreatePacket() throws Exception {
        FilterPcpOption opt = new FilterPcpOption(5, 0xF1F2, IPV6_TEST_ADDRESS);
        assertEquals(3, opt.getCode());
        assertEquals(5, opt.getPrefixLength());
        assertEquals(0xF1F2, opt.getRemotePeerPort());
        assertEquals(IPV6_TEST_ADDRESS, opt.getRemotePeerIpAddress());
    }

    @Test
    public void mustProperlyCreatePacketWithZeroPrefix() throws Exception {
        FilterPcpOption opt = new FilterPcpOption(0, 0xF1F2, IPV6_TEST_ADDRESS);
        assertEquals(3, opt.getCode());
        assertEquals(0, opt.getPrefixLength());
        assertEquals(0xF1F2, opt.getRemotePeerPort());
        assertEquals(IPV6_TEST_ADDRESS, opt.getRemotePeerIpAddress());
    }

    @Test
    public void mustProperlyCreatePacketWithZeroRemotePort() throws Exception {
        FilterPcpOption opt = new FilterPcpOption(5, 0, IPV6_TEST_ADDRESS);
        assertEquals(3, opt.getCode());
        assertEquals(5, opt.getPrefixLength());
        assertEquals(0, opt.getRemotePeerPort());
        assertEquals(IPV6_TEST_ADDRESS, opt.getRemotePeerIpAddress());
    }

    @Test
    public void mustProperlyCreatePacketWithIpv4Address() throws Exception {
        FilterPcpOption opt = new FilterPcpOption(5, 0xF1F2, IPV4_TEST_ADDRESS);
        assertEquals(3, opt.getCode());
        assertEquals(5, opt.getPrefixLength());
        assertEquals(0xF1F2, opt.getRemotePeerPort());
        assertEquals(IPV4_TEST_ADDRESS, opt.getRemotePeerIpAddress());
    }

    @Test
    public void mustParseCreatedPacket() {
        byte[] finalBuffer = new byte[100];
        
        FilterPcpOption origOpt = new FilterPcpOption(5, 0xF1F2, IPV6_TEST_ADDRESS);
        byte[] buffer = origOpt.dump();
        
        final int finalBufferOffset = 20;
        System.arraycopy(buffer, 0, finalBuffer, finalBufferOffset, buffer.length);
        
        FilterPcpOption parsedOpt = new FilterPcpOption(finalBuffer, finalBufferOffset);
        assertEquals(3, parsedOpt.getCode());
        assertEquals(5, parsedOpt.getPrefixLength());
        assertEquals(0xF1F2, parsedOpt.getRemotePeerPort());
        assertEquals(IPV6_TEST_ADDRESS, parsedOpt.getRemotePeerIpAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        FilterPcpOption origOpt = new FilterPcpOption(5, 0xF1F2, IPV6_TEST_ADDRESS);
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        FilterPcpOption parsedOpt = new FilterPcpOption(buffer, 0);
    }

    @Test
    public void mustProperlyParsePacketIfItHasExtraData() {
        FilterPcpOption origOpt = new FilterPcpOption(5, 0xF1F2, IPV6_TEST_ADDRESS);
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        FilterPcpOption parsedOpt = new FilterPcpOption(buffer, 0);
        assertEquals(3, parsedOpt.getCode());
        assertEquals(5, parsedOpt.getPrefixLength());
        assertEquals(0xF1F2, parsedOpt.getRemotePeerPort());
        assertEquals(IPV6_TEST_ADDRESS, parsedOpt.getRemotePeerIpAddress());
    }
}
