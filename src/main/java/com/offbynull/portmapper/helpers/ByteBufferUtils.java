/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.helpers;

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
