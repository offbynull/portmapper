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
package com.offbynull.portmapper.gateways.process.internalmessages;

import com.offbynull.portmapper.gateway.Bus;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.lang3.Validate;

/**
 * Create a process. Possible responses are {@link CreateProcessResponse} and {@link IdentifiableErrorProcessResponse}).
 * @author Kasra Faghihi
 */
public final class CreateProcessRequest extends IdentifiableProcessRequest {

    private Bus responseBus;
    private String executable;
    private UnmodifiableList<String> parameters;

    /**
     * Constructs a {@link CreateProcessRequest} object.
     * @param id of process
     * @param responseBus bus to send responses/notifications to for the created process 
     * @param executable executable to run
     * @param parameters parameters to use when running {@code executable}
     * @throws NullPointerException if any argument is {@code null}, or contains {@code null}
     */
    public CreateProcessRequest(int id, Bus responseBus, String executable, String ... parameters) {
        super(id);
        Validate.notNull(responseBus);
        Validate.notNull(executable);
        Validate.notNull(parameters);
        Validate.noNullElements(parameters);

        this.responseBus = responseBus;
        this.executable = executable;
        this.parameters = (UnmodifiableList<String>) UnmodifiableList.unmodifiableList(new ArrayList<>(Arrays.asList(parameters)));
    }

    /**
     * Bus to send responses/notifications to for the created process.
     * @return response bus
     */
    public Bus getResponseBus() {
        return responseBus;
    }

    /**
     * Get executable to run.
     * @return executable
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Get parameters to use when running executables.
     * @return parameters
     */
    public UnmodifiableList<String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "CreateProcessRequest{" + "responseBus=" + responseBus + ", executable=" + executable
                + ", parameters=" + parameters + '}';
    }

}
