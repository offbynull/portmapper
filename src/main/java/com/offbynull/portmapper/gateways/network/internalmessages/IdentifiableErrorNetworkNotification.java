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

/**
 * Error notification associated with a socket.  Does not mean that the socket associated with this ID is dead (no longer usable) -- you
 * should still close the socket.
 * @author Kasra Faghihi
 */
public final class IdentifiableErrorNetworkNotification extends IdentifiableNetworkNotification {

    /**
     * Constructs a {@link IdentifiableErrorNetworkNotification} object.
     * @param id id of socket
     */
    public IdentifiableErrorNetworkNotification(int id) {
        super(id);
    }

    @Override
    public String toString() {
        return "IdentifiableErrorNetworkNotification{super=" + super.toString() + '}';
    }

}
