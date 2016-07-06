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
package com.offbynull.portmapper.gateways.process.internalmessages;

import com.offbynull.portmapper.gateway.Bus;
import org.apache.commons.lang3.Validate;

/**
 * Get ID to use for a new socket. Only possible response is {@link GetNextIdProcessResponse}.
 * @author Kasra Faghihi
 */
public final class GetNextIdProcessRequest implements ProcessRequest {
    
    private Bus responseBus;
    
    /**
     * Constructs a {@link GetNextIdProcessRequest} object.
     * @param responseBus bus to send new ID to
     * @throws NullPointerException if any argument is {@code null}, or contains {@code null}
     */
    public GetNextIdProcessRequest(Bus responseBus) {
        Validate.notNull(responseBus);

        this.responseBus = responseBus;
    }
    
    /**
     * Bus to send responses/notifications to for the created process.
     * @return response bus
     */
    public Bus getResponseBus() {
        return responseBus;
    }
}
