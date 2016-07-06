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
package com.offbynull.portmapper.mappers.natpmp.externalmessages;

/**
 * Represents a NAT-PMP UDP mapping response. From the RFC:
 * <pre>
 *    The NAT gateway responds with the following packet format:
 * 
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Vers = 0      | OP = 128 + x  | Result Code                   |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Seconds Since Start of Epoch                                  |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Internal Port                 | Mapped External Port          |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Port Mapping Lifetime in Seconds                              |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *    The epoch time, ports, and lifetime are transmitted in the
 *    traditional network byte order (i.e., most significant byte first).
 * 
 *    The 'x' in the OP field MUST match what the client requested.  Some
 *    NAT gateways are incapable of creating a UDP port mapping without
 *    also creating a corresponding TCP port mapping, and vice versa, and
 *    these gateways MUST NOT implement NAT Port Mapping Protocol until
 *    this deficiency is fixed.  A NAT gateway that implements this
 *    protocol MUST be able to create TCP-only and UDP-only port mappings.
 *    If a NAT gateway silently creates a pair of mappings for a client
 *    that only requested one mapping, then it may expose that client to
 *    receiving inbound UDP packets or inbound TCP connection requests that
 *    it did not ask for and does not want.
 * 
 *    While a NAT gateway MUST NOT automatically create mappings for TCP
 *    when the client requests UDP, and vice versa, the NAT gateway MUST
 *    reserve the companion port so the same client can choose to map it in
 *    the future.  For example, if a client requests to map TCP port 80,
 *    as long as the client maintains the lease for that TCP port mapping,
 *    another client with a different internal IP address MUST NOT be able
 *    to successfully acquire the mapping for UDP port 80.
 * 
 *    The client normally requests the external port matching the internal
 *    port.  If that external port is not available, the NAT gateway MUST
 *    return an available external port if possible, or return an error
 *    code if no external ports are available.
 * 
 *    The source address of the packet MUST be used for the internal
 *    address in the mapping.  This protocol is not intended to facilitate
 *    one device behind a NAT creating mappings for other devices.  If
 *    there are legacy devices that require inbound mappings, permanent
 *    mappings can be created manually by the user through an
 *    administrative interface, just as they are today.
 * 
 *    If a mapping already exists for a given internal address and port
 *    (whether that mapping was created explicitly using NAT-PMP,
 *    implicitly as a result of an outgoing TCP SYN packet, or manually by
 *    a human administrator) and that client requests another mapping for
 *    the same internal port (possibly requesting a different external
 *    port), then the mapping request should succeed, returning the
 *    already-assigned external port.  This is necessary to handle the case
 *    where a client requests a mapping with suggested external port X, and
 *    is granted a mapping with actual external port Y, but the
 *    acknowledgment packet gets lost.  When the client retransmits its
 *    mapping request, it should get back the same positive acknowledgment
 *    as was sent (and lost) the first time.
 * 
 *    The NAT gateway MUST NOT accept mapping requests destined to the NAT
 *    gateway's external IP address or received on its external network
 *    interface.  Only packets received on the internal interface(s) with a
 *    destination address matching the internal address(es) of the NAT
 *    gateway should be allowed.
 * 
 *    The NAT gateway MUST fill in the Seconds Since Start of Epoch field
 *    with the time elapsed since its port mapping table was initialized on
 *    startup or reset for any other reason (see Section 3.6, "Seconds
 *    Since Start of Epoch").
 * 
 *    The Port Mapping Lifetime is an unsigned integer in seconds.  The NAT
 *    gateway MAY reduce the lifetime from what the client requested.  The
 *    NAT gateway SHOULD NOT offer a lease lifetime greater than that
 *    requested by the client.
 * 
 *    Upon receiving the response packet, the client MUST check the source
 *    IP address, and silently discard the packet if the address is not the
 *    address of the gateway to which the request was sent.
 * 
 *    The client SHOULD begin trying to renew the mapping halfway to expiry
 *    time, like DHCP.  The renewal packet should look exactly the same as
 *    a request packet, except that the client SHOULD set the Suggested
 *    External Port to what the NAT gateway previously mapped, not what the
 *    client originally suggested.  As described above, this enables the
 *    gateway to automatically recover its mapping state after a crash or
 *    reboot.
 * 
 * ...
 * 
 *    When a mapping is destroyed successfully as a result of the client
 *    explicitly requesting the deletion, the NAT gateway MUST send a
 *    response packet that is formatted as defined in Section 3.3,
 *    "Requesting a Mapping".  The response MUST contain a result code of
 *    0, the internal port as indicated in the deletion request, an
 *    external port of 0, and a lifetime of 0.  The NAT gateway MUST
 *    respond to a request to destroy a mapping that does not exist as if
 *    the request were successful.  This is because of the case where the
 *    acknowledgment is lost, and the client retransmits its request to
 *    delete the mapping.  In this case, the second request to delete the
 *    mapping MUST return the same response packet as the first request.
 * </pre>
 * @author Kasra Faghihi
 */
public final class UdpMappingNatPmpResponse extends MappingNatPmpResponse {
    private static final int OP = 129;

    /**
     * Constructs a {@link UdpMappingNatPmpResponse} object by parsing a buffer.
     * @param buffer buffer containing NAT-PMP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} isn't the right size or is malformed ({@code op != 129 || version != 0 ||
     * 1 > internalPort > 65535 || 1 > externalPort > 65535 || 0 > lifetime > 0xFFFFFFFFL || 0 > externalPort > 65535 ||
     * (lifetime == 0 && externalPort != 0)})
     */
    public UdpMappingNatPmpResponse(byte[] buffer) {
        super(OP, buffer);
    }


    /**
     * Construct a {@link UdpMappingNatPmpResponse} object.
     * @param resultCode result code
     * @param secondsSinceStartOfEpoch seconds since start of epoch
     * @param internalPort internal port
     * @param externalPort external port
     * @param lifetime desired lifetime of mapping ({@code 0} to destroy mapping)
     * @throws IllegalArgumentException if {@code 1 > internalPort > 65535 || 1 > externalPort > 65535 || 0 > lifetime > 0xFFFFFFFFL ||
     * 0 > externalPort > 65535 || (lifetime == 0 && externalPort != 0)} (if both lifetime and externalPort is 0, it means mapping has been
     * deleted)
     */
    public UdpMappingNatPmpResponse(int resultCode, long secondsSinceStartOfEpoch, int internalPort, int externalPort, long lifetime) {
        super(OP, resultCode, secondsSinceStartOfEpoch, internalPort, externalPort, lifetime);
    }

    @Override
    public String toString() {
        return "UdpMappingNatPmpResponse{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
