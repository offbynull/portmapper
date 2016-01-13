package com.offbynull.portmapper.pcp.externalmessages;

import java.util.Arrays;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UnknownPcpOptionTest {

    @Test
    public void mustProperlyCreatePacket() {
        UnknownPcpOption opt = new UnknownPcpOption(0xF7, new byte[] { 1, 2, 3, 4 });
        assertEquals(0xF7, opt.getCode());
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, opt.getData());
    }
    
    @Test
    public void mustParseCreatedPacket() {
        byte[] finalBuffer = new byte[100];
        
        UnknownPcpOption origOpt = new UnknownPcpOption(0xF7, new byte[] { 1, 2, 3, 4 });
        byte[] buffer = origOpt.dump();
        
        final int finalBufferOffset = 20;
        System.arraycopy(buffer, 0, finalBuffer, finalBufferOffset, buffer.length);
        
        UnknownPcpOption parsedOpt = new UnknownPcpOption(finalBuffer, finalBufferOffset);
        assertEquals(0xF7, parsedOpt.getCode());
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, parsedOpt.getData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        UnknownPcpOption origOpt = new UnknownPcpOption(0xF7, new byte[] { 1, 2, 3, 4 });
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        UnknownPcpOption parsedOpt = new UnknownPcpOption(buffer, 0);
    }

    @Test
    public void mustProperlyParsePacketIfItHasExtraData() {
        UnknownPcpOption origOpt = new UnknownPcpOption(0xF7, new byte[] { 1, 2, 3, 4 });
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        UnknownPcpOption parsedOpt = new UnknownPcpOption(buffer, 0);
        assertEquals(0xF7, parsedOpt.getCode());
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, parsedOpt.getData());
    }
}
