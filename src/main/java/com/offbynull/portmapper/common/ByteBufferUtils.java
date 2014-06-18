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

import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Utility class to help with {@link ByteBuffer}s.
 * @author Kasra Faghihi
 */
public final class ByteBufferUtils {
    private ByteBufferUtils() {
        // do nothing
    }

    /**
     * Copy the remaining content of a {@link ByteBuffer} in to a new array. Equivalent to calling
     * {@code copyContentsToArray(src, true)}.
     * @param src buffer to copy
     * @return new buffer with the remaining content in {@code src}
     * @throws NullPointerException if any arguments are {@code null}
     */
    public static byte[] copyContentsToArray(ByteBuffer src) {
        Validate.notNull(src);
        return copyContentsToArray(src, true);
    }
    
    /**
     * Copy the remaining content of a {@link ByteBuffer} in to a new array.
     * @param src buffer to copy
     * @param incrementSrc of {@code true} increments {@code src}'s position
     * @return new buffer with the remaining content in {@code src}
     * @throws NullPointerException if any arguments are {@code null}
     */
    public static byte[] copyContentsToArray(ByteBuffer src, boolean incrementSrc) {
        Validate.notNull(src);
        if (!incrementSrc) {
            src.mark();
        }
        
        ByteBuffer dst = ByteBuffer.allocate(src.remaining());
        dst.put(src);
        
        if (!incrementSrc) {
            src.reset();
        }
        
        return dst.array();
    }

    /**
     * Copy the remaining content of a {@link ByteBuffer} in to a new non-direct {@link ByteBuffer}. Equivalent to calling
     * {@code copyContents(src, true, false)}.
     * @param src buffer to copy
     * @return new buffer with the remaining content in {@code src}
     * @throws NullPointerException if any arguments are {@code null}
     */
    public static ByteBuffer copyContents(ByteBuffer src) {
        Validate.notNull(src);
        return copyContents(src, true, false);
    }
    
    /**
     * Copy the remaining content of a {@link ByteBuffer} in to a new non-direct {@link ByteBuffer}.
     * @param src buffer to copy
     * @param incrementSrc of {@code true} increments {@code src}'s position
     * @param incrementDst of {@code true} increments {@code dst}'s position
     * @return new buffer with the remaining content in {@code src}
     * @throws NullPointerException if any arguments are {@code null}
     */
    public static ByteBuffer copyContents(ByteBuffer src, boolean incrementSrc, boolean incrementDst) {
        Validate.notNull(src);
        if (!incrementSrc) {
            src.mark();
        }
        
        ByteBuffer dst = ByteBuffer.allocate(src.remaining());
        dst.put(src);
        
        if (!incrementSrc) {
            src.reset();
        }
        
        if (!incrementDst) {
            dst.flip();
        }
        
        return dst;
    }
}
