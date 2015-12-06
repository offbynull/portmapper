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
package com.offbynull.portmapper.pcp.messages;

import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;

/**
 * A {@link PcpOption} that doesn't map to any of the known PCP option values presented in the RFC. From the RFC:
 * <pre>
 *    Because the PCP client cannot reject a response containing an Option,
 *    PCP clients MUST ignore Options that they do not understand that
 *    appear in responses, including Options in the mandatory-to-process
 *    range.  Naturally, if a client explicitly requests an Option where
 *    correct execution of that Option requires processing the Option data
 *    in the response, that client SHOULD implement code to do that.
 * </pre>
 * @author Kasra Faghihi
 */
public final class UnknownPcpOption extends PcpOption {
 
    /**
     * Constructs a {@link UnknownPcpOption} object.
     * @param buffer buffer containing PCP option data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     */
    public UnknownPcpOption(ByteBuffer buffer) {
        super(buffer);
    }
}
