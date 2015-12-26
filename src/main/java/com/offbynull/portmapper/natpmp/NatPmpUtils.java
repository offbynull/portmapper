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
package com.offbynull.portmapper.natpmp;

import static com.offbynull.portmapper.natpmp.NatPmpResultCode.SUCCESS;
import com.offbynull.portmapper.natpmp.messages.NatPmpResponse;

/**
 * NAT-PMP Utilities.
 * @author Kasra Faghihi
 */
public final class NatPmpUtils {

    private NatPmpUtils() {
        // do nothing
    }

    /**
     * Checks to see if response has successful result code.
     * @param response NAT-PMP response
     * @return {@code true} if response is successful, {@code false} otherwise
     */
    public static final boolean isSuccess(NatPmpResponse response) {
        return isSuccess(response.getResultCode());
    }

    /**
     * Checks to see if result code is successful.
     * @param resultCode result code
     * @return {@code true} if result code is successful, {@code false} otherwise
     */
    public static final boolean isSuccess(int resultCode) {
        return resultCode == SUCCESS.ordinal();
    }

    /**
     * Validate that response was successful.
     * @param response NAT-PMP response
     * @throws IllegalArgumentException if response was not successful
     */
    public static final void validateResultCode(NatPmpResponse response) {
        validateResultCode(response.getResultCode());
    }

    /**
     * Validate that result code was successful.
     * @param resultCode result code
     * @throws IllegalArgumentException if result code was not successful
     */
    public static final void validateResultCode(int resultCode) {
        if (resultCode == SUCCESS.ordinal()) {
            return;
        }

        NatPmpResultCode[] natPmpResultCodes = NatPmpResultCode.values();
        if (resultCode >= natPmpResultCodes.length) {
            throw new IllegalArgumentException("Unrecognized result code: " + resultCode);
        }
        NatPmpResultCode natPmpResultCode = natPmpResultCodes[resultCode];

        throw new IllegalArgumentException("Bad result code: " + resultCode + " -- " + natPmpResultCode.getMessage());
    }
}
