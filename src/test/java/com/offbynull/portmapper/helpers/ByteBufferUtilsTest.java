package com.offbynull.portmapper.helpers;

import java.nio.ByteBuffer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ByteBufferUtilsTest {
    
    @Test
    public void mustCopyContents() {
        ByteBuffer in = ByteBuffer.wrap("test".getBytes());
        
        ByteBuffer actual = ByteBufferUtils.copyContents(in);
        ByteBuffer expected = ByteBuffer.wrap("test".getBytes());
        
        assertEquals(expected, actual);
        assertEquals(4, in.position());
    }

    @Test
    public void mustCopyContentsWithoutIncrementingSource() {
        ByteBuffer in = ByteBuffer.wrap("test".getBytes());
        
        ByteBuffer actual = ByteBufferUtils.copyContents(in, false, false);
        ByteBuffer expected = ByteBuffer.wrap("test".getBytes());
        
        assertEquals(expected, actual);
        assertEquals(0, in.position());
    }

    @Test
    public void mustCopyContentsAndIncrementingDestination() {
        ByteBuffer in = ByteBuffer.wrap("test".getBytes());
        
        ByteBuffer actual = ByteBufferUtils.copyContents(in, false, true);
        ByteBuffer expected = ByteBuffer.wrap("test".getBytes());
        expected.position(expected.limit());
        
        assertEquals(expected, actual);
        assertEquals(0, in.position());
    }
    
    @Test
    public void mustCopyContentsToArray() {
        ByteBuffer in = ByteBuffer.wrap("test".getBytes());
        
        byte[] actual = ByteBufferUtils.copyContentsToArray(in);
        byte[] expected = "test".getBytes();
        
        assertArrayEquals(expected, actual);
        assertEquals(4, in.position());
    }
    
    @Test
    public void mustCopyContentsToArrayWithoutIncrementingSource() {
        ByteBuffer in = ByteBuffer.wrap("test".getBytes());
        
        byte[] actual = ByteBufferUtils.copyContentsToArray(in, false);
        byte[] expected = "test".getBytes();
        
        assertArrayEquals(expected, actual);
        assertEquals(0, in.position());
    }
}
