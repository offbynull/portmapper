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
package com.offbynull.portmapper.mappers.pcp;

import org.apache.commons.lang3.Validate;

/**
 * PCP result codes. From the RFC:
 * <pre>
 * 7.4.  Result Codes
 * 
 *    The following result codes may be returned as a result of any Opcode
 *    received by the PCP server.  The only success result code is 0; other
 *    values indicate an error.  If a PCP server encounters multiple errors
 *    during processing of a request, it SHOULD use the most specific error
 *    message.  Each error code below is classified as either a 'long
 *    lifetime' error or a 'short lifetime' error, which provides guidance
 *    to PCP server developers for the value of the Lifetime field for
 *    these errors.  It is RECOMMENDED that short lifetime errors use a
 *    30-second lifetime and long lifetime errors use a 30-minute lifetime.
 * 
 *    0  SUCCESS: Success.
 * 
 *    1  UNSUPP_VERSION: The version number at the start of the PCP Request
 *       header is not recognized by this PCP server.  This is a long
 *       lifetime error.  This document describes PCP version 2.
 * 
 *    2  NOT_AUTHORIZED: The requested operation is disabled for this PCP
 *       client, or the PCP client requested an operation that cannot be
 *       fulfilled by the PCP server's security policy.  This is a long
 *       lifetime error.
 * 
 *    3  MALFORMED_REQUEST: The request could not be successfully parsed.
 *       This is a long lifetime error.
 * 
 *    4  UNSUPP_OPCODE: Unsupported Opcode.  This is a long lifetime error.
 * 
 *    5  UNSUPP_OPTION: Unsupported option.  This error only occurs if the
 *       option is in the mandatory-to-process range.  This is a long
 *       lifetime error.
 * 
 *    6  MALFORMED_OPTION: Malformed option (e.g., appears too many times,
 *       invalid length).  This is a long lifetime error.
 * 
 *    7  NETWORK_FAILURE: The PCP server or the device it controls is
 *       experiencing a network failure of some sort (e.g., has not yet
 *       obtained an external IP address).  This is a short lifetime error.
 * 
 *    8  NO_RESOURCES: Request is well-formed and valid, but the server has
 *       insufficient resources to complete the requested operation at this
 *       time.  For example, the NAT device cannot create more mappings at
 *       this time, is short of CPU cycles or memory, or is unable to
 *       handle the request due to some other temporary condition.  The
 *       same request may succeed in the future.  This is a system-wide
 *       error, different from USER_EX_QUOTA.  This can be used as a catch-
 *       all error, should no other error message be suitable.  This is a
 *       short lifetime error.
 * 
 *    9  UNSUPP_PROTOCOL: Unsupported transport protocol, e.g., SCTP in a
 *       NAT that handles only UDP and TCP.  This is a long lifetime error.
 * 
 *    10 USER_EX_QUOTA: This attempt to create a new mapping would exceed
 *       this subscriber's port quota.  This is a short lifetime error.
 * 
 *    11 CANNOT_PROVIDE_EXTERNAL: The suggested external port and/or
 *       external address cannot be provided.  This error MUST only be
 *       returned for:
 *       *  MAP requests that included the PREFER_FAILURE option
 *          (normal MAP requests will return an available external port)
 *       *  MAP requests for the SCTP protocol (PREFER_FAILURE is implied)
 *       *  PEER requests
 * 
 *       See Section 13.2 for details of the PREFER_FAILURE Option.  The
 *       error lifetime depends on the reason for the failure.
 * 
 *    12 ADDRESS_MISMATCH: The source IP address of the request packet does
 *       not match the contents of the PCP Client's IP Address field, due
 *       to an unexpected NAT on the path between the PCP client and the
 *       PCP-controlled NAT or firewall.  This is a long lifetime error.
 * 
 *    13 EXCESSIVE_REMOTE_PEERS: The PCP server was not able to create the
 *       filters in this request.  This result code MUST only be returned
 *       if the MAP request contained the FILTER option.  See Section 13.3
 *       for details of the FILTER Option.  This is a long lifetime error.
 * 
 * </pre>
 * @author Kasra Faghihi
 */
enum PcpResultCode {
    /**
     * See Javadoc header.
     */
    SUCCESS("Success."),
    /**
     * See Javadoc header.
     */
    UNSUPP_VERSION("The version number at the start of the PCP Request header is not recognized by this PCP server.  This is a long"
            + " lifetime error.  This document describes PCP version 2."),
    /**
     * See Javadoc header.
     */
    NOT_AUTHORIZED("The requested operation is disabled for this PCP client, or the PCP client requested an operation that cannot be"
            + " fulfilled by the PCP server's security policy.  This is a long lifetime error."),
    /**
     * See Javadoc header.
     */
    MALFORMED_REQUEST("The request could not be successfully parsed. This is a long lifetime error."),
    /**
     * See Javadoc header.
     */
    UNSUPP_OPCODE("Unsupported Opcode.  This is a long lifetime error."),
    /**
     * See Javadoc header.
     */
    UNSUPP_OPTION("Unsupported option.  This error only occurs if the option is in the mandatory-to-process range.  This is a long"
            + " lifetime error."),
    /**
     * See Javadoc header.
     */
    MALFORMED_OPTION("Malformed option (e.g., appears too many times, invalid length).  This is a long lifetime error."),
    /**
     * See Javadoc header.
     */
    NETWORK_FAILURE("The PCP server or the device it controls is experiencing a network failure of some sort (e.g., has not yet obtained"
            + " an external IP address).  This is a short lifetime error."),
    /**
     * See Javadoc header.
     */
    NO_RESOURCES("Request is well-formed and valid, but the server has insufficient resources to complete the requested operation at this"
            + " time.  For example, the NAT device cannot create more mappings at this time, is short of CPU cycles or memory, or is unable"
            + " to handle the request due to some other temporary condition.  The same request may succeed in the future.  This is a"
            + " system-wide error, different from USER_EX_QUOTA.  This can be used as a catch-all error, should no other error message be"
            + " suitable.  This is a short lifetime error."),
    /**
     * See Javadoc header.
     */
    UNSUPP_PROTOCOL("Unsupported transport protocol, e.g., SCTP in a NAT that handles only UDP and TCP.  This is a long lifetime error."),
    /**
     * See Javadoc header.
     */
    USER_EX_QUOTA("This attempt to create a new mapping would exceed this subscriber's port quota.  This is a short lifetime error."),
    /**
     * See Javadoc header.
     */
    CANNOT_PROVIDE_EXTERNAL("The suggested external port and/or external address cannot be provided.  This error MUST only be returned"
            + " for:\n"
            + " *  MAP requests that included the PREFER_FAILURE option\n"
            + "    (normal MAP requests will return an available external port)\n"
            + " *  MAP requests for the SCTP protocol (PREFER_FAILURE is implied)\n"
            + " *  PEER requests"),
    /**
     * See Javadoc header.
     */
    ADDRESS_MISMATCH("The source IP address of the request packet does not match the contents of the PCP Client's IP Address field, due"
            + " to an unexpected NAT on the path between the PCP client and the PCP-controlled NAT or firewall.  This is a long lifetime"
            + " error."),
    /**
     * See Javadoc header.
     */
    EXCESSIVE_REMOTE_PEERS("The PCP server was not able to create the filters in this request.  This result code MUST only be returned"
            + " if the MAP request contained the FILTER option.  See Section 13.3 for details of the FILTER Option.  This is a long"
            + " lifetime error.");
    
    private final String message;

    /**
     * Constructs a {@link PcpResultCode} object.
     * @param message user-friendly description of result code
     * @throws NullPointerException if any argument is {@code null}
     */
    PcpResultCode(String message) {
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
