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
     * IPV4 Regex. Taken from http://stackoverflow.com/questions/106179/regular-expression-to-match-hostname-or-ip-address.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9‌​]{2}|2[0-4][0-9]|25[0-5])");
    
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
        while (matcher.find()) {
            ret.add(matcher.group(0));
        }
        
        return ret;
    }
}
