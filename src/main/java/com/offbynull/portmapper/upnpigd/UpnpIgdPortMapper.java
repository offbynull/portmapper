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
package com.offbynull.portmapper.upnpigd;

import com.offbynull.portmapper.MappedPort;
import com.offbynull.portmapper.PortMapper;
import com.offbynull.portmapper.PortMapperEventListener;
import com.offbynull.portmapper.PortType;
import com.offbynull.portmapper.common.ResponseException;
import com.offbynull.portmapper.upnpigd.UpnpIgdController.PortMappingInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import org.apache.commons.lang3.Validate;

/**
 * A UPNP-IGD {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public final class UpnpIgdPortMapper implements PortMapper {

    private Random random = new Random();
    private UpnpIgdController controller;
    private volatile boolean closed;

    /**
     * Constructs a {@link UpnpIgdPortMapper} object.
     * @param service UPNP-IGD service
     * @param listener event listener
     * @throws NullPointerException if any argument is {@code null}
     */
    public UpnpIgdPortMapper(UpnpIgdService service, final PortMapperEventListener listener) {
        Validate.notNull(service);
        Validate.notNull(listener);

        controller = new UpnpIgdController(service, new UpnpIgdControllerListener() {
            @Override
            public void mappingExpired(UpnpIgdController.PortMappingInfo mappedPort) {
                if (closed) {
                    return;
                }

                listener.resetRequired("Mapping may have been lost: " + mappedPort);
            }
        });
    }

    @Override
    public MappedPort mapPort(PortType portType, int internalPort, long lifetime) throws InterruptedException {
        Validate.validState(!closed);
        Validate.notNull(portType);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
        
        int externalPort = random.nextInt(55535) + 10000; // 10000 - 65535
        PortMappingInfo info;
        InetAddress externalAddress;
        try {
            info = controller.addPortMapping(externalPort, internalPort, portType, lifetime);
            externalAddress = controller.getExternalIp();
        } catch (IllegalArgumentException | ResponseException re) {
            throw new IllegalStateException(re);
        }
        
        return new MappedPort(info.getInternalPort(), info.getExternalPort(), externalAddress, info.getPortType(),
                info.getRemainingDuration());
    }

    @Override
    public void unmapPort(MappedPort mappedPort) throws InterruptedException {
        Validate.validState(!closed);
        Validate.notNull(mappedPort);
        
        try {
            controller.deletePortMapping(mappedPort.getExternalPort(), mappedPort.getPortType());
        } catch (IllegalArgumentException | ResponseException re) {
            throw new IllegalStateException(re);
        }
    }

    @Override
    public MappedPort refreshPort(MappedPort mappedPort, long lifetime) throws InterruptedException {
        Validate.validState(!closed);
        Validate.notNull(mappedPort);
        Validate.inclusiveBetween(1L, Long.MAX_VALUE, lifetime);
        
        PortMappingInfo info;
        InetAddress externalAddress;
        try {
            controller.deletePortMapping(mappedPort.getExternalPort(), mappedPort.getPortType());
            info = controller.addPortMapping(mappedPort.getExternalPort(), mappedPort.getInternalPort(), mappedPort.getPortType(),
                    lifetime);
            externalAddress = controller.getExternalIp();
        } catch (IllegalArgumentException | ResponseException re) {
            throw new IllegalStateException(re);
        }
        
        return new MappedPort(info.getInternalPort(), info.getExternalPort(), externalAddress, info.getPortType(),
                info.getRemainingDuration());
    }

    @Override
    public void close() throws IOException {
        closed = true;
        controller.close();
    }

}
