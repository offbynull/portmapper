/*
 * Copyright (c) 2013-2014, Kasra Faghihi, All rights reserved.
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
    private static final int IPV6_MAX_NUMBER_OF_COMPONENTS = 8;

    private TextUtils() {
        // do nothing
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
                String ip = readIp4(text, i);
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
            
            // If less than 8 components, must have exactly 1 empty component -- empty = group of zeroes
            if (components.size() < IPV6_MAX_NUMBER_OF_COMPONENTS) {
                int emptyComponentCount = 0;
                for (String component : components) {
                    if (component.isEmpty()) {
                        emptyComponentCount++;
                    }
                }
                Validate.isTrue(emptyComponentCount == 1);
            }

            // Construct IP
            StringBuilder ip = new StringBuilder();
            for (int i = 0; i < components.size(); i++) {
                String component = components.get(i);
                ip.append(component);
                
                if (i != components.size()) {
                    ip.append(':');
                }
            }

            return ip.toString();
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
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

            // no need to check for leading 0's -- leading zeros may be omitted
            
            // may be empty -- empty means 0000
            
            return component.toString();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
