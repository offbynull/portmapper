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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.Validate;

/**
 * Utility class to help with regex searches.
 * @author Kasra Faghihi
 */
public final class RegexUtils {
    /**
     * Semi-IPV4 Regex. Groups 1 to 4 are the individual address components. Further checking needs to be done to
     * ensure that each component is between 0 to 255 and that there are no trailing zeros.
     * 
     * This isn't being done in the regex due to complexity.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "([0-9]{1,3})\\."
            + "([0-9]{1,3})\\."
            + "([0-9]{1,3})\\."
            + "([0-9]{1,3})");
    
    private static final int IPV4_COMPONENT_COUNT = 4;
    
    private static final int IPV4_COMPONENT_MAX = 255;
    
    private RegexUtils() {
        // do nothing
    }
    
    /**
     * Finds all IPv4 addresses in a block of text.
     * @param text block of text to search in
     * @return all IPv4 addresses in {@code text}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static List<String> findAllIpv4Addresses(String text) {
        Validate.notNull(text);
        
        List<String> ret = new LinkedList<>();
        
        Matcher matcher = IPV4_PATTERN.matcher(text);
        top:
        while (matcher.find()) {
            for (int i = 1; i <= IPV4_COMPONENT_COUNT; i++) {
                String componentStr = matcher.group(i);
                int component = Integer.parseInt(componentStr);
                
                // Fail if component has trailing zeros
                if (!String.valueOf(component).equals(componentStr)) {
                    continue top;
                }
                
                // Fail if component is greater than 255. The regex ensures that it's never below 0.
                if (component > IPV4_COMPONENT_MAX) {
                    continue top;
                }
            }

            // IP is valid, add it to the return list
            ret.add(matcher.group(0));
        }
        
        return ret;
    }
}
