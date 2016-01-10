package com.offbynull.portmapper.pcp.externalmessages;

import com.offbynull.portmapper.pcp.externalmessages.ThirdPartyPcpOption;
import com.offbynull.portmapper.pcp.externalmessages.PreferFailurePcpOption;
import com.offbynull.portmapper.pcp.externalmessages.AnnouncePcpResponse;
import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AnnouncePcpResponseTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertBytesToAddress(new byte[] {1, 2, 3, 4});

    @Test
    public void mustProperlyCreatePacket() throws Exception {
        AnnouncePcpResponse opt = new AnnouncePcpResponse(0, 0xFFFFFFFFL);
        assertEquals(0, opt.getOp());
        assertEquals(0L, opt.getLifetime());
        assertEquals(0xFFFFFFFFL, opt.getEpochTime());
        assertEquals(Collections.emptyList(), opt.getOptions());
    }

    @Test
    public void mustProperlyCreatePacketWithOptions() throws Exception {
        AnnouncePcpResponse opt = new AnnouncePcpResponse(0, 0xFFFFFFFFL, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        assertEquals(0, opt.getOp());
        assertEquals(0L, opt.getLifetime());
        assertEquals(0xFFFFFFFFL, opt.getEpochTime());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV4_TEST_ADDRESS)), opt.getOptions());
    }

    @Test
    public void mustParseCreatedPacket() {
        AnnouncePcpResponse origReq = new AnnouncePcpResponse(0, 0xFFFFFFFFL, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        AnnouncePcpResponse parsedOpt = new AnnouncePcpResponse(buffer);
        assertEquals(0, parsedOpt.getOp());
        assertEquals(0L, parsedOpt.getLifetime());
        assertEquals(0xFFFFFFFFL, parsedOpt.getEpochTime());
        assertEquals(Arrays.asList(new PreferFailurePcpOption(), new ThirdPartyPcpOption(IPV4_TEST_ADDRESS)), parsedOpt.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        AnnouncePcpResponse origReq = new AnnouncePcpResponse(0, 0xFFFFFFFFL, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        AnnouncePcpResponse parsedReq = new AnnouncePcpResponse(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        AnnouncePcpResponse origReq = new AnnouncePcpResponse(0, 0xFFFFFFFFL, new PreferFailurePcpOption(),
                new ThirdPartyPcpOption(IPV4_TEST_ADDRESS));
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        AnnouncePcpResponse parsedReq = new AnnouncePcpResponse(buffer);
    }
}
