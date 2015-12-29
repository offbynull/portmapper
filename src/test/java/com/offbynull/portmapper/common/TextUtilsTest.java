package com.offbynull.portmapper.common;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
    
    @Test
    public void mustPassWithFullIpv6Components() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd 0123:1234:2345:3456:4567:5678:6789:789a fs\n\n \r\tfsf89ab:9abc:abcd:bcde:cdef:def0:ef01:f012sdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("0123:1234:2345:3456:4567:5678:6789:789a", addresses.get(0));
        assertEquals("89ab:9abc:abcd:bcde:cdef:def0:ef01:f012", addresses.get(1));
    }

    @Test
    public void mustPassWithNonFullIpv6Components() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd 0:1:2:3:4:5:6:7 fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("0:1:2:3:4:5:6:7", addresses.get(0));
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(1));
    }

    @Test
    public void mustPassWithEmptyIpv6() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd :: fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("::", addresses.get(0));
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(1));
    }

    @Test
    public void mustPassWithIpv6ComponentsThatHaveStartingEmptyGroup() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd ::a:b:c fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("::a:b:c", addresses.get(0));
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(1));
    }

    @Test
    public void mustPassWithIpv6ComponentsThatHaveEndingEmptyGroup() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd a:b:c:: fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("a:b:c::", addresses.get(0));
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(1));
    }

    @Test
    public void mustPassWithIpv6ComponentsThatHaveMiddleEmptyGroup() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd a::c fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");
        
        assertEquals(2, addresses.size());
        assertEquals("a::c", addresses.get(0));
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(1));
    }

    @Test
    public void mustRejectWithIpv6ComponentsThatHaveMoreThan1EmptyGroup() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd ::a::c a::c:: a::b::c fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");
        
        // Remember that this is correct ... it reads until it determines that what its read is invalid/valid. If it ends up being invalid,
        // it moves up 1 character from where it originally started reading from and restarts the process.
        assertEquals(4, addresses.size());
        assertEquals("a::c", addresses.get(0)); // e.g. ::a::c is valid, so is :a::c, which leads to a::c
        assertEquals("c::", addresses.get(1));
        assertEquals("b::c", addresses.get(2));
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(3));
    }

    @Test
    public void mustRejectWithIpv6WithTooManyComponents() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd 0:1:2:3:4:5:6:7:8:9 fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");
        
        // Remember that this is correct ... it reads until it determines that what its read is invalid/valid. If it ends up being invalid,
        // it moves up 1 character from where it originally started reading from and restarts the process.
        assertEquals(2, addresses.size());
        assertEquals("0:1:2:3:4:5:6:7", addresses.get(0));
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(1));
    }

    @Test
    public void mustRejectWithIpv6WithNotEnoughComponents() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd 0:1:2:3:4:5:6 fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");

        assertEquals(1, addresses.size());
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(0));
    }

    @Test
    public void mustRejectWithIpv6WithNotEnoughComponentsWithTrailingColon() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd 0:1:2:3:4:5:6: fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");

        assertEquals(1, addresses.size());
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(0));
    }
    
    @Test
    public void mustRejectWithIpv6WithNotEnoughComponentsWithStartingColon() {
        List<String> addresses = TextUtils.findAllIpv6Addresses(
                "sf sdf sd :0:1:2:3:4:5:6 fs\n\n \r\tfsf8:9:a:b:c:d:e:fsdfsfsdfs");

        assertEquals(1, addresses.size());
        assertEquals("f8:9:a:b:c:d:e:f", addresses.get(0));
    }
    
    @Test
    public void mustFindBlocksOfText() {
        List<String> blocks = TextUtils.findAllBlocks("\t\tsfosnfhello this is a test goodbyes\tfsdfshellogoodbye", "hello", "goodbye",
                false);
        
        assertEquals(2, blocks.size());
        assertEquals(" this is a test ", blocks.get(0));
        assertEquals("", blocks.get(1));
    }

    @Test
    public void mustFindBlocksOfTextCaseInsensitive() {
        List<String> blocks = TextUtils.findAllBlocks("\t\tsfosnfhello this is a test goodbyes\tfsdfshellogoodbye", "heLLo", "goodbye",
                true);
        
        assertEquals(2, blocks.size());
        assertEquals(" this is a test ", blocks.get(0));
        assertEquals("", blocks.get(1));
    }
    
    @Test
    public void mustNotFindBlocksOfTextThatDontEndWithIdentifier() {
        List<String> blocks = TextUtils.findAllBlocks("\t\tsfosnfhello this is a test goodbes\tfsdfshellogoodbe", "hello", "goodbye",
                false);
        
        assertEquals(0, blocks.size());
    }
    
    @Test
    public void mustNotFindBlocksOfTextThatDontStartWithIdentifier() {
        List<String> blocks = TextUtils.findAllBlocks("\t\tsfosnfhell this is a test goodbyes\tfsdfshellgoodbye", "hello", "goodbye",
                false);
        
        assertEquals(0, blocks.size());
    }
    
    @Test
    public void mustNotFindBlocksOfTextWhenStartIdentifierTooLargeWithIdentifier() {
        List<String> blocks = TextUtils.findAllBlocks("\t\thelloffffffffgoodbye", "hellooooooooooooooooooooooooooooooooooooooo", "goodbye",
                false);
        
        assertEquals(0, blocks.size());
    }
    
    @Test
    public void mustNotFindBlocksOfTextWhenEndIdentifierTooLargeWithIdentifier() {
        List<String> blocks = TextUtils.findAllBlocks("\t\thelloffffffffgoodbye", "hello", "goodbyeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                false);
        
        assertEquals(0, blocks.size());
    }

    @Test
    public void mustFindFirstBlockOfText() {
        String block = TextUtils.findFirstBlock("\t\tsfosnfhello this is a test goodbyes\tfsdfshellogoodbye", "hello", "goodbye",
                false);

        assertEquals(" this is a test ", block);
    }

    @Test
    public void mustFindFirstBlockOfTextCaseInsensitive() {
        String block = TextUtils.findFirstBlock("\t\tsfosnfhello this is a test goodbyes\tfsdfshellogoodbye", "heLLo", "goodbYe",
                true);

        assertEquals(" this is a test ", block);
    }
    
    @Test
    public void mustNotFindFirstBlockOfTextThatDontEndWithIdentifier() {
        String block = TextUtils.findFirstBlock("\t\tsfosnfhello this is a test goodbes\tfsdfshellogoodbe", "hello", "goodbye", false);
        
        assertNull(block);
    }
    
    @Test
    public void mustNotFindFirstBlockOfTextThatDontStartWithIdentifier() {
        String block = TextUtils.findFirstBlock("\t\tsfosnfhell this is a test goodbyes\tfsdfshellgoodbye", "hello", "goodbye", false);
        
        assertNull(block);
    }
    
    @Test
    public void mustNotFindFirstBlockOfTextWhenStartIdentifierTooLargeWithIdentifier() {
        String block = TextUtils.findFirstBlock("\t\thelloffffffffgoodbye", "hellooooooooooooooooooooooooooooooooooooooo", "goodbye",
                false);
        
        assertNull(block);
    }
    
    @Test
    public void mustNotFindFirstBlockOfTextWhenEndIdentifierTooLargeWithIdentifier() {
        String block = TextUtils.findFirstBlock("\t\thelloffffffffgoodbye", "hello", "goodbyeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                false);
        
        assertNull(block);
    }
    
    @Test
    public void mustCollapseWhitespace() {
        String collapsed = TextUtils.collapseWhitespace("    fff   \t\taaa v ");
        assertEquals(" fff aaa v ", collapsed);
    }
    
    @Test
    public void mustCollapseWhitespaceWhenNoPaddingInStartOrEnd() {
        String collapsed = TextUtils.collapseWhitespace("fff   \t\taaa v");
        assertEquals("fff aaa v", collapsed);
    }
    
    @Test
    public void mustNotCollapseSingleWhitespace() {
        String collapsed = TextUtils.collapseWhitespace(" fff\taaa v ");
        assertEquals(" fff aaa v ", collapsed);
    }
    
    @Test
    public void mustNotCollapseSingleWhitespaceWhenNoPaddingInStartOrEnd() {
        String collapsed = TextUtils.collapseWhitespace("fff\taaa v");
        assertEquals("fff aaa v", collapsed);
    }
}
