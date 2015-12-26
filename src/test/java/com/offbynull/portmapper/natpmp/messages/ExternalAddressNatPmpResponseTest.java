package com.offbynull.portmapper.natpmp.messages;

import java.net.InetAddress;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ExternalAddressNatPmpResponseTest {
    
    @Test
    public void mustProperlyCreatePacket() throws Exception {
        InetAddress addr = InetAddress.getByAddress(new byte[] {1, 2, 3, 4});
        
        ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, addr);
        
        assertEquals(128, resp.getOp());
        assertEquals(1, resp.getResultCode());
        assertEquals(0xFFFFFFFFL, resp.getSecondsSinceStartOfEpoch());
        assertEquals(addr, resp.getAddress());
    }
    
    @Test
    public void mustParseCreatedPacket() throws Exception {
        InetAddress addr = InetAddress.getByAddress(new byte[] {1, 2, 3, 4});
        
        ExternalAddressNatPmpResponse resp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, addr);
        byte[] buffer = resp.dump();
        
        ExternalAddressNatPmpResponse parsedResp = new ExternalAddressNatPmpResponse(buffer);

        assertEquals(128, parsedResp.getOp());
        assertEquals(1, parsedResp.getResultCode());
        assertEquals(0xFFFFFFFFL, parsedResp.getSecondsSinceStartOfEpoch());
        assertEquals(addr, parsedResp.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() throws Exception {
        InetAddress addr = InetAddress.getByAddress(new byte[] {1, 2, 3, 4});
        
        ExternalAddressNatPmpResponse origResp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, addr);
        byte[] buffer = origResp.dump();

        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        ExternalAddressNatPmpRequest parsedReq = new ExternalAddressNatPmpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() throws Exception {
        InetAddress addr = InetAddress.getByAddress(new byte[] {1, 2, 3, 4});
        
        ExternalAddressNatPmpResponse origResp = new ExternalAddressNatPmpResponse(1, 0xFFFFFFFFL, addr);
        byte[] buffer = origResp.dump();

        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        ExternalAddressNatPmpRequest parsedReq = new ExternalAddressNatPmpRequest(buffer);
    }
}
