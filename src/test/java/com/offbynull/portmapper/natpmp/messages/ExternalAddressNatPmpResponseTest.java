package com.offbynull.portmapper.natpmp.messages;

import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ExternalAddressNatPmpResponseTest {
    private static final InetAddress IPV4_TEST_ADDRESS
            = NetworkUtils.convertArrayToIp(new byte[] {1, 2, 3, 4});
    private static final InetAddress IPV6_TEST_ADDRESS
            = NetworkUtils.convertArrayToIp(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
    
    @Test
    public void mustProperlyCreatePacket() throws Exception {
        ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, IPV4_TEST_ADDRESS);
        
        assertEquals(128, resp.getOp());
        assertEquals(1, resp.getResultCode());
        assertEquals(0xFFFFFFFFL, resp.getSecondsSinceStartOfEpoch());
        assertEquals(IPV4_TEST_ADDRESS, resp.getAddress());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void mustFailToCreatePacketWithIpv6Address() throws Exception {
        ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, IPV6_TEST_ADDRESS);
    }
    
    @Test
    public void mustParseCreatedPacket() throws Exception {
        ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, IPV4_TEST_ADDRESS);
        byte[] buffer = resp.dump();
        
        ExternalAddressNatPmpResponse parsedResp = new ExternalAddressNatPmpResponse(buffer);

        assertEquals(128, parsedResp.getOp());
        assertEquals(1, parsedResp.getResultCode());
        assertEquals(0xFFFFFFFFL, parsedResp.getSecondsSinceStartOfEpoch());
        assertEquals(IPV4_TEST_ADDRESS, parsedResp.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() throws Exception {
        ExternalAddressNatPmpResponse origResp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, IPV4_TEST_ADDRESS);
        byte[] buffer = origResp.dump();

        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        ExternalAddressNatPmpRequest parsedReq = new ExternalAddressNatPmpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() throws Exception {
        ExternalAddressNatPmpResponse origResp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, IPV4_TEST_ADDRESS);
        byte[] buffer = origResp.dump();

        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        ExternalAddressNatPmpRequest parsedReq = new ExternalAddressNatPmpRequest(buffer);
    }
}
