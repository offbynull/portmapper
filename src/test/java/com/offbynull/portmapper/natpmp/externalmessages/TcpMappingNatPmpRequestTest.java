package com.offbynull.portmapper.natpmp.externalmessages;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TcpMappingNatPmpRequestTest {
    
    @Test
    public void mustProperlyCreatePacket() {
        TcpMappingNatPmpRequest req = new TcpMappingNatPmpRequest(0xF1F2, 0, 0xFFFFFFFFL);
        assertEquals(2, req.getOp());
        assertEquals(0xF1F2, req.getInternalPort());
        assertEquals(0, req.getSuggestedExternalPort());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
    }
    
    @Test
    public void mustProperlyCreatePacketWithNonZeroPorts() {
        TcpMappingNatPmpRequest req = new TcpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        assertEquals(2, req.getOp());
        assertEquals(0xF1F2, req.getInternalPort());
        assertEquals(0xF3F4, req.getSuggestedExternalPort());
        assertEquals(0xFFFFFFFFL, req.getLifetime());
    }
    
    @Test
    public void mustParseCreatedPacket() {
        TcpMappingNatPmpRequest origReq = new TcpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origReq.dump();
        
        TcpMappingNatPmpRequest parsedReq = new TcpMappingNatPmpRequest(buffer);
        assertEquals(2, parsedReq.getOp());
        assertEquals(0xF1F2, parsedReq.getInternalPort());
        assertEquals(0xF3F4, parsedReq.getSuggestedExternalPort());
        assertEquals(0xFFFFFFFFL, parsedReq.getLifetime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        TcpMappingNatPmpRequest origReq = new TcpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        TcpMappingNatPmpRequest parsedReq = new TcpMappingNatPmpRequest(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        TcpMappingNatPmpRequest origReq = new TcpMappingNatPmpRequest(0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origReq.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        TcpMappingNatPmpRequest parsedReq = new TcpMappingNatPmpRequest(buffer);
    }
}
