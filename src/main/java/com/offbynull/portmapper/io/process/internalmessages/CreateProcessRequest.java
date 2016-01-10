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
package com.offbynull.portmapper.io.process.internalmessages;

import com.offbynull.portmapper.io.network.internalmessages.*;
import com.offbynull.portmapper.common.Bus;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.lang3.Validate;

public final class CreateProcessRequest implements ProcessRequest {

    private Bus responseBus;
    private String executable;
    private UnmodifiableList<String> parameters;

    public CreateProcessRequest(Bus responseBus, String executable, String ... parameters) {
        Validate.notNull(responseBus);
        Validate.notNull(executable);
        Validate.notNull(parameters);
        Validate.noNullElements(parameters);

        this.responseBus = responseBus;
        this.executable = executable;
        this.parameters = (UnmodifiableList<String>) UnmodifiableList.unmodifiableList(new ArrayList<>(Arrays.asList(parameters)));
    }

    public Bus getResponseBus() {
        return responseBus;
    }

    public String getExecutable() {
        return executable;
    }

    public UnmodifiableList<String> getParameters() {
        return parameters;
    }

}
