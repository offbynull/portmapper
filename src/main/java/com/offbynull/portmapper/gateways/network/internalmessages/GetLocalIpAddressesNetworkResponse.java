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
package com.offbynull.portmapper.gateways.network.internalmessages;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Got local IP addresses. Successful response to {@link GetLocalIpAddressesNetworkRequest}.
 * @author Kasra Faghihi
 */
public final class GetLocalIpAddressesNetworkResponse implements NetworkResponse {
    private Set<InetAddress> localAddresses;

    /**
     * Constructs a {@link GetLocalIpAddressesNetworkRequest} object.
     * @param localAddresses local addresses
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     */
    public GetLocalIpAddressesNetworkResponse(Set<InetAddress> localAddresses) {
        Validate.notNull(localAddresses);
        Validate.noNullElements(localAddresses);
        this.localAddresses = new HashSet<>(localAddresses);
    }

    /**
     * Get local addresses.
     * @return local addresses
     */
    public Set<InetAddress> getLocalAddresses() {
        return localAddresses;
    }

    @Override
    public String toString() {
        return "GetLocalIpAddressesNetworkResponse{" + "localAddresses=" + localAddresses + '}';
    }
    
}
