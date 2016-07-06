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

/**
 * Closed a process. Successful response to {@link CloseProcessRequest}.
 * <p>
 * If an ID is used after issuing a close, the results are undefined.
 * @author Kasra Faghihi
 */
public final class CloseProcessResponse extends IdentifiableProcessResponse {

    /**
     * Constructs a {@link CloseProcessResponse}.
     * @param id id of process
     */
    public CloseProcessResponse(int id) {
        super(id);
    }

    @Override
    public String toString() {
        return "CloseProcessResponse{super=" + super.toString() + '}';
    }

}
