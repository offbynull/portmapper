package com.offbynull.portmapper.pcp.externalmessages;

import com.offbynull.portmapper.pcp.externalmessages.PreferFailurePcpOption;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PreferFailurePcpOptionTest {

    @Test
    public void mustProperlyCreatePacket() throws Exception {
        PreferFailurePcpOption opt = new PreferFailurePcpOption();
        assertEquals(2, opt.getCode());
    }
    
    @Test
    public void mustParseCreatedPacket() {
        byte[] finalBuffer = new byte[100];
        
        PreferFailurePcpOption origOpt = new PreferFailurePcpOption();
        byte[] buffer = origOpt.dump();
        
        final int finalBufferOffset = 20;
        System.arraycopy(buffer, 0, finalBuffer, finalBufferOffset, buffer.length);
        
        PreferFailurePcpOption parsedOpt = new PreferFailurePcpOption(finalBuffer, finalBufferOffset);
        assertEquals(2, parsedOpt.getCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustFailToParsePacketThatsTooShort() {
        PreferFailurePcpOption origOpt = new PreferFailurePcpOption();
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length - 1);
        
        PreferFailurePcpOption parsedOpt = new PreferFailurePcpOption(buffer, 0);
    }

    @Test
    public void mustProperlyParsePacketIfItHasExtraData() {
        PreferFailurePcpOption origOpt = new PreferFailurePcpOption();
        byte[] buffer = origOpt.dump();
        
        buffer = Arrays.copyOf(buffer, buffer.length + 1);
        
        PreferFailurePcpOption parsedOpt = new PreferFailurePcpOption(buffer, 0);
        assertEquals(2, parsedOpt.getCode());
    }
}
