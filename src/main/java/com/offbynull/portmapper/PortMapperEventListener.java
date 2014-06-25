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
package com.offbynull.portmapper;

/**
 * An interface used for listening to events from {@link PortMapper}.
 * @author Kasra Faghihi
 */
public interface PortMapperEventListener {
    /**
     * Indicates that one or more port mappings may have been lost or changed. If this occurs, the mapper should be closed and the
     * recreated. If the mapper isn't recreated, it may or may not continue to operate. The behaviour is undefined.
     * <p>
     * <b>WARNING:</b> Do not block in this method. Do not make any changes to the {@link PortMapper} that triggered this event in this
     * method.
     * @param details details of the problem that caused this invocation
     */
    void resetRequired(String details);
}
