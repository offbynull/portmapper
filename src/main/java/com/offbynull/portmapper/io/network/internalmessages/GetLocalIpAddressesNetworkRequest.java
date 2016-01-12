/*
 * Copyright (c) 2013-2016, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.io.network.internalmessages;

import com.offbynull.portmapper.Bus;
import org.apache.commons.lang3.Validate;

/**
 * Get local IP addresses. Possible responses are {@link GetLocalIpAddressesNetworkResponse} and {@link ErrorNetworkResponse}).
 * @author Kasra Faghihi
 */
public final class GetLocalIpAddressesNetworkRequest implements NetworkRequest {
    private Bus responseBus;

    /**
     * Constructs a {@link GetLocalIpAddressesNetworkRequest} object.
     * @param responseBus bus to send response to
     * @throws NullPointerException if any argument {@code null}
     */
    public GetLocalIpAddressesNetworkRequest(Bus responseBus) {
        Validate.notNull(responseBus);

        this.responseBus = responseBus;
    }

    /**
     * Bus to send response to.
     * @return response bus
     */
    public Bus getResponseBus() {
        return responseBus;
    }
}
