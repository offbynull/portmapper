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
 * Interface for port mapping.
 * @author Kasra Faghihi
 */
public interface PortMapper {

    /**
     * Map a port. Mapping the same port multiple times has undefined behaviour.
     * @param portType port type
     * @param internalPort internal port
     * @param externalPort suggested external port (mapped external port may be different)
     * @param lifetime number of seconds to acquire mapping for (may be reduced or extended depending on server and/or client)
     * @return object that describes mapping
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is {@code <= 0}, or if {@code internalPort > 65535 || externalPort > 65535}
     * @throws IllegalStateException if the port could not be mapped for any reason
     * @throws InterruptedException if thread was interrupted
     */
    MappedPort mapPort(PortType portType, int internalPort, int externalPort, long lifetime) throws InterruptedException;
    
    /**
     * Unmap a port. Unmapping the same port multiple times or unmapping a port that hasn't been mapped yet has undefined behaviour.
     * @param mappedPort mapped port details
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code mappedPort} is not expected by this mapper (e.g. created by a different mapper or not the
     * same type of mapped port that was expected by this mapper)
     * @throws IllegalStateException if the port could not be unmapped for any reason
     * @throws InterruptedException if thread was interrupted
     */
    void unmapPort(MappedPort mappedPort) throws InterruptedException;

    /**
     * Refresh a mapping. Refreshing a port that hasn't been mapped or has been unmapped has undefined behaviour.
     * @param mappedPort mapped port
     * @param lifetime number of seconds to acquire mapping for (may be reduced or extended depending on server and/or client)
     * @return object that describes the refreshed mapping (this is the object that should be used now to control the mapping, the previous
     * object must no longer be used)
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is non-positive, or if {@code mappedPort} is not expected by this mapper
     * (e.g. created by a different mapper or not the same type of mapped port that was expected by this mapper)
     * @throws IllegalStateException if the port could not be refreshed to the same external IP/port for any reason
     * @throws InterruptedException if thread was interrupted
     */
    MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException;
    
    /**
     * Get the source address used by this port mapper. Use the source IP to figure out which interface the gateway was found on.
     * @return source address
     */
    InetAddress getSourceAddress();
}
