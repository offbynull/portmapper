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
package com.offbynull.portmapper.mapper;

import java.net.InetAddress;

/**
 * Represents a mapped port.
 * @author Kasra Faghihi
 */
public interface MappedPort {

    /**
     * Get internal port.
     * @return internal port
     */
    int getInternalPort();

    /**
     * Get external port.
     * @return external port
     */
    int getExternalPort();

    /**
     * Get external address (optional).
     * @return external address ({@code null} if underlying protocol doesn't support getting the external address)
     */
    InetAddress getExternalAddress();
    
    /**
     * Get port type.
     * @return port type
     */
    PortType getPortType();

    /**
     * Get mapping lifetime.
     * @return mapping lifetime
     */
    long getLifetime();
}
