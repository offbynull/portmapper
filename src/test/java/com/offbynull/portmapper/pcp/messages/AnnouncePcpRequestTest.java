package com.offbynull.portmapper.pcp.messages;

import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AnnouncePcpRequestTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4});
    private static final InetAddress IPV6_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});

    @Test
    public void mustProperlyCreatePacketWithIpv4() throws Exception {
        AnnouncePcpRequest opt = new AnnouncePcpRequest(IPV4_TEST_ADDRESS);
        assertEquals(0, opt.getOp());
        assertEquals(0L, opt.getLifetime());
        assertEquals(IPV4_TEST_ADDRESS, opt.getInternalIp());
        assertEquals(Collections.emptyList(), opt.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithIpv6() throws Exception {
        AnnouncePcpRequest opt = new AnnouncePcpRequest(IPV6_TEST_ADDRESS);
        assertEquals(0, opt.getOp());
        assertEquals(0L, opt.getLifetime());
        assertEquals(IPV6_TEST_ADDRESS, opt.getInternalIp());
        assertEquals(Collections.emptyList(), opt.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithOptions() throws Exception {
        AnnouncePcpRequest opt = new AnnouncePcpRequest(IPV6_TEST_ADDRESS, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        assertEquals(0, opt.getOp());
        assertEquals(0L, opt.getLifetime());
        assertEquals(IPV6_TEST_ADDRESS, opt.getInternalIp());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), opt.getOptions());
    }

    @Test
    public void mustParseCreatedPacket() {
        AnnouncePcpRequest origReq = new AnnouncePcpRequest(IPV6_TEST_ADDRESS, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        AnnouncePcpRequest parsedOpt = new AnnouncePcpRequest(buffer);
        assertEquals(0, parsedOpt.getOp());
        assertEquals(0L, parsedOpt.getLifetime());
        assertEquals(IPV6_TEST_ADDRESS, parsedOpt.getInternalIp());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS)), parsedOpt.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        AnnouncePcpRequest origReq = new AnnouncePcpRequest(IPV6_TEST_ADDRESS, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        AnnouncePcpRequest parsedReq = new AnnouncePcpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        AnnouncePcpRequest origReq = new AnnouncePcpRequest(IPV6_TEST_ADDRESS, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV6_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        AnnouncePcpRequest parsedReq = new AnnouncePcpRequest(buffer);
    }
}
