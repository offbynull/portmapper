package com.offbynull.portmapper.natpmp.externalmessages;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TcpMappingNatPmpResponseTest {
    
    @Test
    public void mustProperlyCreatePacket() {
        TcpMappingNatPmpResponse resp = new TcpMappingNatPmpResponse(1, 0xFFFFFFFEL, 0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        assertEquals(1, resp.getResultCode());
        assertEquals(0xFFFFFFFEL, resp.getSecondsSinceStartOfEpoch());
        assertEquals(130, resp.getOp());
        assertEquals(0xF1F2, resp.getInternalPort());
        assertEquals(0xF3F4, resp.getExternalPort());
        assertEquals(0xFFFFFFFFL, resp.getLifetime());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithInternalPortSetTo0() {
        TcpMappingNatPmpResponse resp = new TcpMappingNatPmpResponse(1, 0xFFFFFFFEL, 0, 0xF3F4, 0xFFFFFFFFL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToCreatePacketWithExternalPortSetTo0() {
        TcpMappingNatPmpResponse resp = new TcpMappingNatPmpResponse(1, 0xFFFFFFFEL, 0xF1F2, 0, 0xFFFFFFFFL);
    }

    @Test
    public void mustParseCreatedPacket() {
        TcpMappingNatPmpResponse origResp = new TcpMappingNatPmpResponse(1, 0xFFFFFFFEL, 0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origResp.dump();
        
        TcpMappingNatPmpResponse parsedResp = new TcpMappingNatPmpResponse(buffer);
        assertEquals(1, parsedResp.getResultCode());
        assertEquals(0xFFFFFFFEL, parsedResp.getSecondsSinceStartOfEpoch());
        assertEquals(130, parsedResp.getOp());
        assertEquals(0xF1F2, parsedResp.getInternalPort());
        assertEquals(0xF3F4, parsedResp.getExternalPort());
        assertEquals(0xFFFFFFFFL, parsedResp.getLifetime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        TcpMappingNatPmpResponse origResp = new TcpMappingNatPmpResponse(1, 0xFFFFFFFEL, 0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origResp.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        TcpMappingNatPmpResponse parsedResp = new TcpMappingNatPmpResponse(buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooLong() {
        TcpMappingNatPmpResponse origResp = new TcpMappingNatPmpResponse(1, 0xFFFFFFFEL, 0xF1F2, 0xF3F4, 0xFFFFFFFFL);
        byte[] buffer = origResp.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        TcpMappingNatPmpResponse parsedResp = new TcpMappingNatPmpResponse(buffer);
    }
}
