package com.offbynull.portmapper.common;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class RegexUtilsTest {
    
    @Test
    public void mustPassOnProperAddressesWithFullComponents() {
        List<String> addresses = RegexUtils.findAllIpv4Addresses(
                "sf sdf sd 212.123.222.199 fs\n\n \r\tfsf123.124.125.126sdfsfsdfs");
        
        Assert.assertEquals(2, addresses.size());
        Assert.assertEquals("212.123.222.199", addresses.get(0));
        Assert.assertEquals("123.124.125.126", addresses.get(1));
    }

    @Test
    public void mustPassOnProperAddressesWithNonFullComponents() {
        List<String> addresses = RegexUtils.findAllIpv4Addresses(
                "sf sdf sd 10.6.0.254 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        Assert.assertEquals(2, addresses.size());
        Assert.assertEquals("10.6.0.254", addresses.get(0));
        Assert.assertEquals("10.6.0.2", addresses.get(1));
    }

    @Test
    public void mustRejectComponentsWithTrailingZeros() {
        List<String> addresses = RegexUtils.findAllIpv4Addresses(
                "sf sdf sd 010.6.0.254 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        Assert.assertEquals(1, addresses.size());
        Assert.assertEquals("10.6.0.2", addresses.get(0));
    }

    @Test
    public void mustRejectComponentsThatExceed255() {
        List<String> addresses = RegexUtils.findAllIpv4Addresses(
                "sf sdf sd 10.6.0.256 fs\n\n \r\tfsf10.6.0.2sdfsfsdfs");
        
        Assert.assertEquals(1, addresses.size());
        Assert.assertEquals("10.6.0.2", addresses.get(0));
    }
}
