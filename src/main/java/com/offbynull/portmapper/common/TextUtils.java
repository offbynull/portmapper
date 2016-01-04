/*
 * Copyright (c) 2013-2015, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.portmapper.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Utility class to help with text searches.
 *
 * @author Kasra Faghihi
 */
public final class TextUtils {

    // NOTE: We don't use regex/xml parsing for these because we eventually want to convert this project to other languages -- meaning that
    // we want to keep the code as light as possible.
    
    private static final int IPV4_COMPONENT_MAX = 255;
    private static final int IPV4_COMPONENT_MAX_SIZE = 3;
    private static final int IPV6_MAX_NUMBER_OF_COMPONENTS = 8;
    private static final int IPV6_COMPONENT_MAX_SIZE = 4;

    private TextUtils() {
        // do nothing
    }

    /**
     * Split a host/port combination (split by a colon). For example, {@code myhost.com:12345} will be split in to host {@code myhost.com}
     * and port {@code 12345}.
     * @param hostPort host port combination (split by colon)
     * @param defaultPort default port to use if port portion isn't present in {@code hostPort}
     * @return host and port combination
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code 1 > defaultPort > 65535}, or if the port parsed from {@code hostPort} is invalid
     * ({@code < 1 || > 65535})
     */
    public static ParsedHost splitHostAndPort(String hostPort, int defaultPort) {
        Validate.notNull(hostPort);
        Validate.inclusiveBetween(1, 65535, defaultPort);
        int idx = StringUtils.lastIndexOf(hostPort, ':');
        if (idx == -1) {
            return new ParsedHost(hostPort, defaultPort);
        }
        
        int port = Integer.parseInt(hostPort.substring(idx)); // throws nfe, but nfe is derived from illegalargexc
        Validate.inclusiveBetween(1, 65535, port);
        
        return new ParsedHost(hostPort, port);
    }
    
    /**
     * Parsed host/port combination
     */
    public static final class ParsedHost {
        private final String host;
        private final int port;

        private ParsedHost(String host, int port) {
            Validate.notNull(host);
            Validate.inclusiveBetween(1, 65535, port);
            this.host = host;
            this.port = port;
        }

        /**
         * Get host.
         * @return host
         */
        public String getHost() {
            return host;
        }

        /**
         * Get port.
         * @return port
         */
        public int getPort() {
            return port;
        }
    }

    /**
     * Collapses contiguous whitespace in to a single space character (0x20).
     * @param text text containing whitespace to collapse
     * @return {@code text} with whitespace collapsed
     * @throws NullPointerException if any argument is {@code null}
     */
    public static String collapseWhitespace(String text) {
        Validate.notNull(text);
        
        StringBuilder sb = new StringBuilder(text.length());

        int len = text.length();
        boolean prevCharWhitespace = false;
        for (int i = 0; i < len;) {
            int cp = text.codePointAt(i);
            
            if (Character.isWhitespace(cp)) {
                if (prevCharWhitespace) {
                    i += Character.charCount(cp);
                } else {
                    prevCharWhitespace = true;
                    sb.append(' ');
                    i += Character.charCount(cp);
                }
            } else {
                prevCharWhitespace = false;
                sb.appendCodePoint(cp);
                i += Character.charCount(cp);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Finds first block of text that start with a particular string (case insensitive) and end with a particular string (case insensitive).
     *
     * @param text block of text to search in
     * @param begin starting string to search for
     * @param end ending string to search for
     * @param insensitiveSearch if there should be a case insensitive search for {@code start} and {@code end}
     * @return all found blocks in in {@code text}, not including the starting and ending strings
     * @throws NullPointerException if any argument is {@code null}
     */
    public static String findFirstBlock(String text, String begin, String end, boolean insensitiveSearch) {
        Validate.notNull(text);
        Validate.notNull(begin);
        Validate.notNull(end);

        List<String> blocks = findAllBlocks(text, begin, end, insensitiveSearch);
        
        if (blocks.isEmpty()) {
            return null;
        }
        
        return blocks.get(0);
    }

    /**
     * Finds all blocks of text that start with a particular string (case insensitive) and end with a particular string (case insensitive).
     *
     * @param text block of text to search in
     * @param begin starting string to search for
     * @param end ending string to search for
     * @param insensitiveSearch if there should be a case insensitive search for {@code start} and {@code end}
     * @return all found blocks in in {@code text}, not including the starting and ending strings
     * @throws NullPointerException if any argument is {@code null}
     */
    public static List<String> findAllBlocks(String text, String begin, String end, boolean insensitiveSearch) {
        Validate.notNull(text);
        Validate.notNull(begin);
        Validate.notNull(end);
        
        List<String> ret = new LinkedList<>();
        
        int nextIdx = 0;
        
        while (true) {
            // Get start position
            int startIdx = insensitiveSearch ? StringUtils.indexOfIgnoreCase(text, begin, nextIdx) : text.indexOf(begin, nextIdx);
            if (startIdx == -1) {
                break;
            }
            startIdx += begin.length();
            
            // Get end position
            int endIdx = insensitiveSearch ? StringUtils.indexOfIgnoreCase(text, end, startIdx) : text.indexOf(end, startIdx);
            if (endIdx == -1) {
                break;
            }
            
            // Get block of text from being and end
            String block = text.substring(startIdx, endIdx);
            ret.add(block);
            
            // Calculate index to start searching for next block at
            nextIdx = endIdx + end.length();
        }
        
        return ret;
    }
    
    /**
     * Finds all IPv4 addresses in a block of text.
     *
     * @param text block of text to search in
     * @return all IPv4 addresses in {@code text}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static List<String> findAllIpv4Addresses(String text) {
        Validate.notNull(text);

        List<String> ret = new LinkedList<>();

        int len = text.length();
        for (int i = 0; i < len;) {
            int cp = text.codePointAt(i);
            try {
                String ip = readIp4(text, i);
                i += ip.length();
                
                ret.add(ip);
            } catch (IllegalArgumentException iae) {
                i += Character.charCount(cp);
            }
        }
        
        return ret;
    }

    private static String readIp4(String text, int offset) {
        try {
            String component1 = readIp4Component(text, offset);
            offset += component1.length();

            Validate.isTrue(text.codePointAt(offset) == '.');
            offset++;

            String component2 = readIp4Component(text, offset);
            offset += component2.length();

            Validate.isTrue(text.codePointAt(offset) == '.');
            offset++;

            String component3 = readIp4Component(text, offset);
            offset += component3.length();

            Validate.isTrue(text.codePointAt(offset) == '.');
            offset++;

            String component4 = readIp4Component(text, offset);
            offset += component4.length();

            return component1 + '.' + component2 + '.' + component3 + '.' + component4;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String readIp4Component(String text, int offset) {
        try {
            int len = text.length();

            StringBuilder component = new StringBuilder();
            while (offset < len) {
                int cp = text.codePointAt(offset);
                if (cp >= '0' && cp <= '9') {
                    component.appendCodePoint(cp);
                } else {
                    break;
                }
                offset += Character.charCount(cp);
            }

            String componentStr = component.toString();
            
            // make sure we only read a max of 3 bytes
            Validate.isTrue(componentStr.length() <= IPV4_COMPONENT_MAX_SIZE);
            
            int componentAsNum = Integer.parseInt(componentStr);

            // not a IP4 component if it has has trailing zeros
            if (!String.valueOf(componentAsNum).equals(componentStr)) {
                throw new IllegalArgumentException();
            }

            // not a IP4 component if its greater than 255
            if (componentAsNum > IPV4_COMPONENT_MAX) {
                throw new IllegalArgumentException();
            }

            return componentStr;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Finds all IPv6 addresses in a block of text.
     *
     * @param text block of text to search in
     * @return all IPv6 addresses in {@code text}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static List<String> findAllIpv6Addresses(String text) {
        Validate.notNull(text);

        List<String> ret = new LinkedList<>();

        int len = text.length();
        for (int i = 0; i < len;) {
            int cp = text.codePointAt(i);
            try {
                String ip = readIp6(text, i);
                i += ip.length();
                
                ret.add(ip);
            } catch (IllegalArgumentException iae) {
                i += Character.charCount(cp);
            }
        }
        
        return ret;
    }

    private static String readIp6(String text, int offset) {
        try {
            List<String> components = new ArrayList<>(IPV6_MAX_NUMBER_OF_COMPONENTS);
            for (int i = 0; i < IPV6_MAX_NUMBER_OF_COMPONENTS; i++) {
                String component = readIp6Component(text, offset);
                offset += component.length();
                
                components.add(component);

                if (text.codePointAt(offset) != ':') {
                    break;
                }
                
                offset++;
            }
            
            if (components.size() < IPV6_MAX_NUMBER_OF_COMPONENTS) {
                // If less than 8 components, must have 1 empty group ...
                // "::" is a valid address  (has 3 empty components at start -- note that this means 1 empty group)
                // "::1" is a valid address  (has 2 empty components at start -- note that this means 1 empty group)
                // "1::" is a valid address  (has 2 empty components at end -- note that this means 1 empty group)
                // "1::1" is a valid address  (has 1 empty component in the middle -- note that this means 1 empty group)
                // "0:0::0" is a valid address  (has 1 empty component in the middle -- note that this means 1 empty group)
                // "0::0::0" is NOT a valid address  (has 2 empty component in the middle -- note that this means 2 empty group)
        
                if (components.size() == 3
                        && components.get(0).isEmpty()
                        && components.get(1).isEmpty()
                        && components.get(2).isEmpty()) {
                    // SPECIAL CASE:
                    // If 3 empty components, then this is a valid address that means full 0...
                    // "::" is a valid address        
                    
                    // do nothing -- all 3 components are empty so this is valid
                } else if (components.size() > 2
                        && components.get(0).isEmpty()
                        && components.get(1).isEmpty()) {
                    // SPECIAL CASE:
                    // If more than 2 components, but first 2 componetns are empty, then this is a valid address that starts with 0s...
                    // "::1:2" is a valid address        
                    
                    Validate.isTrue(2 == getEmptyComponentCount(components)); // already made sure there's 2 in the start, making sure there
                                                                              // are no others by doing this check
                } else if (components.size() > 2
                        && components.get(components.size() - 2).isEmpty()
                        && components.get(components.size() - 1).isEmpty()) {
                    // SPECIAL CASE:
                    // If more than 2 components, but last 2 componetns are empty, then this is a valid address that ends with 0s...
                    // "1:2::" is a valid address        
                    
                    Validate.isTrue(2 == getEmptyComponentCount(components)); // already made sure there's 2 in the end, making sure there
                                                                              // are no others by doing this check.
                } else if (components.size() > 2
                        && !components.get(0).isEmpty()
                        && !components.get(components.size() - 1).isEmpty()) {
                    // SPECIAL CASE:
                    // If more than 2 components, but the first and last components are not empty, there must be at least 1 empty component
                    // in the middle...
                    // "1::2" is a valid address        
                    Validate.isTrue(1 == getEmptyComponentCount(components));
                } else {
                    // This is a bad address... addresses that don't have enough components + property empty components end up here ..
                    // "" is an invalid address
                    // "abcd" is an invalid address
                    // "abcd:" is an invalid address
                    // ":abcd:" is an invalid address
                    // "abcd:abcd:" is an invalid address
                    throw new IllegalArgumentException();
                }
            } else {
                // Otherwise must have exactly 8 non-empty components
                Validate.isTrue(components.size() == IPV6_MAX_NUMBER_OF_COMPONENTS);
                Validate.isTrue(0 == getEmptyComponentCount(components));
            }

            // Construct IP
            StringBuilder ip = new StringBuilder();
            for (int i = 0; i < components.size(); i++) {
                String component = components.get(i);
                ip.append(component);
                
                if (i != components.size() - 1) {
                    ip.append(':');
                }
            }

            return ip.toString();
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static int getEmptyComponentCount(List<String> components) {
        int emptyComponentCount = 0;
        for (String component : components) {
            if (component.isEmpty()) {
                emptyComponentCount++;
            }
        }
        return emptyComponentCount;
    }

    private static String readIp6Component(String text, int offset) {
        try {
            int len = text.length();

            StringBuilder component = new StringBuilder();
            while (offset < len) {
                int cp = text.codePointAt(offset);
                if ((cp >= '0' && cp <= '9')
                        || (cp >= 'a' && cp <= 'f')
                        || (cp >= 'A' && cp <= 'F')) {
                    component.appendCodePoint(cp);
                } else {
                    break;
                }
                offset += Character.charCount(cp);
            }

            String componentStr = component.toString();
            
            // make sure we only read a max of 4 bytes
            Validate.isTrue(componentStr.length() <= IPV6_COMPONENT_MAX_SIZE);

            // no need to check for leading 0's -- leading zeros may be omitted
            
            // may be empty -- empty means 0000
            
            return componentStr;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
