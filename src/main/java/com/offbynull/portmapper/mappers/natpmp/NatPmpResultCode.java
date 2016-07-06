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
package com.offbynull.portmapper.mappers.natpmp;

import org.apache.commons.lang3.Validate;

/**
 * NAT-PMP result codes. From the RFC:
 * <pre>
 * 
 * 3.5.  Result Codes
 * 
 *    Currently defined result codes:
 * 
 *    0 - Success
 *    1 - Unsupported Version
 *    2 - Not Authorized/Refused
 *        (e.g., box supports mapping, but user has turned feature off)
 *    3 - Network Failure
 *        (e.g., NAT box itself has not obtained a DHCP lease)
 *    4 - Out of resources
 *        (NAT box cannot create any more mappings at this time)
 *    5 - Unsupported opcode
 * 
 *    If the version in the request is not zero, then the NAT-PMP server
 *    MUST return the following "Unsupported Version" error response to the
 *    client:
 * 
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Vers = 0      | OP = 0        | Result Code = 1               |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Seconds Since Start of Epoch (in network byte order)          |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *    If the opcode in the request is 128 or greater, then this is not a
 *    request; it's a response, and the NAT-PMP server MUST silently ignore
 *    it.  Otherwise, if the opcode in the request is less than 128, but is
 *    not a supported opcode (currently 0, 1, or 2), then the entire
 *    request MUST be returned to the sender, with the top bit of the
 *    opcode set (to indicate that this is a response) and the result code
 *    set to 5 (Unsupported opcode).
 * 
 *    For version 0 and a supported opcode (0, 1, or 2), if the operation
 *    fails for some reason (Not Authorized, Network Failure, or Out of
 *    resources), then a valid response MUST be sent to the client, with
 *    the top bit of the opcode set (to indicate that this is a response)
 *    and the result code set appropriately.  Other fields in the response
 *    MUST be set appropriately.  Specifically:
 * 
 *    o Seconds Since Start of Epoch MUST be set correctly
 * 
 *    o The External IPv4 Address should be set to the correct address, or
 *      to 0.0.0.0, as appropriate.
 * 
 *    o The Internal Port MUST be set to the client's requested Internal
 *      Port.  This is particularly important, because the client needs
 *      this information to identify which request suffered the failure.
 * 
 *    o The Mapped External Port and Port Mapping Lifetime MUST be set
 *      appropriately -- i.e., zero if no successful port mapping was
 *      created.
 * 
 *    Should future NAT-PMP opcodes be defined, their error responses MUST
 *    similarly be specified to include sufficient information to identify
 *    which request suffered the failure.  By design, NAT-PMP messages do
 *    not contain any transaction identifiers.  All NAT-PMP messages are
 *    idempotent and self-describing; therefore, the specifications of
 *    future NAT-PMP messages need to include enough information for their
 *    responses to be self-describing.
 * 
 *    Clients MUST be able to properly handle result codes not defined in
 *    this document.  Undefined results codes MUST be treated as fatal
 *    errors of the request.
 * </pre>
 * @author Kasra Faghihi
 */
public enum NatPmpResultCode {
    /**
     * See Javadoc header.
     */
    SUCCESS("Success"),
    /**
     * See Javadoc header.
     */
    UNSUPP_VERSION("Unsupported Version"),
    /**
     * See Javadoc header.
     */
    NOT_AUTHORIZED("Not Authorized/Refused (e.g., box supports mapping, but user has turned feature off)"),
    /**
     * See Javadoc header.
     */
    NETWORK_FAILURE("Network Failure (e.g., NAT box itself has not obtained a DHCP lease)"),
    /**
     * See Javadoc header.
     */
    OUT_OF_RESOURCES("Out of resources (NAT box cannot create any more mappings at this time)"),
    /**
     * See Javadoc header.
     */
    UNSUPP_OPCODE("Unsupported opcode");
    
    private final String message;

    /**
     * Constructs a {@link NatPmpResultCode} object.
     * @param message user-friendly description of result code
     * @throws NullPointerException if any argument is {@code null}
     */
    NatPmpResultCode(String message) {
        Validate.notNull(message);
        this.message = message;
    }

    /**
     * Get user-friendly message for result code.
     * @return user-friendly message for result code
     */
    public String getMessage() {
        return message;
    }
}
