package com.offbynull.portmapper.natpmp.messages;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UdpMappingNatPmpRequestTest {
    
    @Test
    public void mustProperlyCreatePacket() {
        UdpMappingNatPmpRequest req = new UdpMappingNatPmpRequest(0xF1F2, 0, 0xFFFFFFFFL);
        assertEquals(2, req.getOp());
        assertEquals(0xF1F2, req.getInternalPort());
        assertEquals(0, req.getSuggestedExternalPort());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
    }
    
    @Test
    public void mustProperlyCreatePacketWithNonZeroPorts() {
        UdpMappingNatPmpRequest req = new UdpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        assertEquals(2, req.getOp());
        assertEquals(0xF1F2, req.getInternalPort());
        assertEquals(0xF3F4, req.getSuggestedExternalPort());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
    }
    
    @Test
    public void mustParseCreatedPacket() {
        UdpMappingNatPmpRequest origReq = new UdpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origReq.dump();
        
        UdpMappingNatPmpRequest parsedReq = new UdpMappingNatPmpRequest(buffer);
        assertEquals(2, parsedReq.getOp());
        assertEquals(0xF1F2, parsedReq.getInternalPort());
        assertEquals(0xF3F4, parsedReq.getSuggestedExternalPort());
        assertEquals(0xFFFFFFFFL, parsedReq.getLifetime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        UdpMappingNatPmpRequest origReq = new UdpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        UdpMappingNatPmpRequest parsedReq = new UdpMappingNatPmpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        UdpMappingNatPmpRequest origReq = new UdpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        UdpMappingNatPmpRequest parsedReq = new UdpMappingNatPmpRequest(buffer);
    }
}
