package com.offbynull.portmapper.common;

import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TextUtilsTest {
    
    @Test
    public void mustPassWithFullIpv4Components() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 212.123.222.199 fs\n\n \r\tfsf123.124.125.126sdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("212.123.222.199", addresses.get(0));
        assertEquals("123.124.125.126", addresses.get(1));
    }

    @Test
    public void mustPassWithNonFullIpv4Components() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 10.6.0.255 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("10.6.0.255", addresses.get(0));
        assertEquals("10.6.0.2", addresses.get(1));
    }

    @Test
    public void mustRejectIpv4ComponentsWithTrailingZeros() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 10.06.0.254 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(1, addresses.size());
        assertEquals("10.6.0.2", addresses.get(0));
    }

    @Test
    public void mustPassWhenIpv4PrecededByDigit() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 010.6.0.254 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("10.6.0.254", addresses.get(0));
        assertEquals("10.6.0.2", addresses.get(1));
    }

    @Test
    public void mustRejectIpv4ComponentsThatExceed255() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 10.6.0.256 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(1, addresses.size());
        assertEquals("10.6.0.2", addresses.get(0));
    }

    @Test
    public void mustRejectNotEnoughIpv4Components() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 10.6.0. fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(1, addresses.size());
        assertEquals("10.6.0.2", addresses.get(0));
    }

    @Test
    public void mustRejectNotEnoughIpv4ComponentsWithoutDot() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 10.6.0 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(1, addresses.size());
        assertEquals("10.6.0.2", addresses.get(0));
    }

    @Test
    public void mustRejectNotEnoughIpv4ComponentsMissingFromMiddle() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "sf sdf sd 10.6..4 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(1, addresses.size());
        assertEquals("10.6.0.2", addresses.get(0));
    }

    @Test
    public void mustRejectNotEnoughIpv4ComponentsMissingFromIpAtStart() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "1.1.4 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        assertEquals(1, addresses.size());
        assertEquals("10.6.0.2", addresses.get(0));
    }

    @Test
    public void mustRejectNotEnoughIpv4ComponentsMissingFromIpAtEnd() {
        List<String> addresses = TextUtils.findAllIpv4Addresses(
                "fs\n\n \r\tfsf10.6.0.2sdfsfsdfs1.1.4");
        
        assertEquals(1, addresses.size());
        assertEquals("10.6.0.2", addresses.get(0));
    }
}
