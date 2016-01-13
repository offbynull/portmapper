package com.offbynull.portmapper.pcp.externalmessages;

import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ThirdPartyPcpOptionTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4});
    private static final InetAddress IPV6_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});

    @Test
    public void mustProperlyCreatePacket() throws Exception {
        ThirdPartyPcpOption opt = new ThirdPartyPcpOption(IPV6_TEST_ADDRESS);
        assertEquals(1, opt.getCode());
        assertEquals(IPV6_TEST_ADDRESS, opt.getInternalIpAddress());
    }

    @Test
    public void mustProperlyCreatePacketWithIpv4Address() throws Exception {
        ThirdPartyPcpOption opt = new ThirdPartyPcpOption(IPV4_TEST_ADDRESS);
        assertEquals(1, opt.getCode());
        assertEquals(IPV4_TEST_ADDRESS, opt.getInternalIpAddress());
    }
    
    @Test
    public void mustParseCreatedPacket() {
        byte[] finalBuffer = new byte[100];
        
        ThirdPartyPcpOption origOpt = new ThirdPartyPcpOption(IPV6_TEST_ADDRESS);
        byte[] buffer = origOpt.dump();
        
        final int finalBufferOffset = 20;
        System.arraycopy(buffer, 0, finalBuffer, finalBufferOffset, buffer.length);
        
        ThirdPartyPcpOption parsedOpt = new ThirdPartyPcpOption(finalBuffer, finalBufferOffset);
        assertEquals(1, parsedOpt.getCode());
        assertEquals(IPV6_TEST_ADDRESS, parsedOpt.getInternalIpAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        ThirdPartyPcpOption origOpt = new ThirdPartyPcpOption(IPV6_TEST_ADDRESS);
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        ThirdPartyPcpOption parsedOpt = new ThirdPartyPcpOption(buffer, 0);
    }

    @Test
    public void mustProperlyParsePacketIfItHasExtraData() {
        ThirdPartyPcpOption origOpt = new ThirdPartyPcpOption(IPV6_TEST_ADDRESS);
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        ThirdPartyPcpOption parsedOpt = new ThirdPartyPcpOption(buffer, 0);
        assertEquals(1, parsedOpt.getCode());
        assertEquals(IPV6_TEST_ADDRESS, parsedOpt.getInternalIpAddress());
    }
}
