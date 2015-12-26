package com.offbynull.portmapper.natpmp.messages;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ExternalAddressNatPmpRequestTest {

    @Test
    public void mustProperlyCreatePacket() {
        ExternalAddressNatPmpRequest req = new ExternalAddressNatPmpRequest();
        assertEquals(0, req.getOp());
    }
    
    @Test
    public void mustParseCreatedPacket() {
        ExternalAddressNatPmpRequest origReq = new ExternalAddressNatPmpRequest();
        byte[] buffer = origReq.dump();
        
        ExternalAddressNatPmpRequest parsedReq = new ExternalAddressNatPmpRequest(buffer);
        assertEquals(0, parsedReq.getOp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        ExternalAddressNatPmpRequest origReq = new ExternalAddressNatPmpRequest();
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        ExternalAddressNatPmpRequest parsedReq = new ExternalAddressNatPmpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        ExternalAddressNatPmpRequest origReq = new ExternalAddressNatPmpRequest();
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        ExternalAddressNatPmpRequest parsedReq = new ExternalAddressNatPmpRequest(buffer);
    }
}
