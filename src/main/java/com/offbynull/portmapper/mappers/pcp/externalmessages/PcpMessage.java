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
package com.offbynull.portmapper.mappers.pcp.externalmessages;

/**
 * Represents a PCP message.
 * @author Kasra Faghihi
 */
public interface PcpMessage {
    /**
     * Dump out the PCP message as a packet.
     * @return PCP packet
     * @throws IndexOutOfBoundsException if the generated packet is greater than 1100 bytes (section 7 of the RFC states: All PCP messages
     * are sent over UDP, with a maximum UDP payload length of 1100 octets)
     */
    byte[] dump();
}
