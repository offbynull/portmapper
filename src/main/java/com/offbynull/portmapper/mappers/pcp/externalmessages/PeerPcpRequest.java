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

import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Represents a PEER PCP request. From the RFC:
 * <pre>
 *      0                   1                   2                   3
 *      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                                                               |
 *     |                 Mapping Nonce (96 bits)                       |
 *     |                                                               |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |   Protocol    |          Reserved (24 bits)                   |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |        Internal Port          |    Suggested External Port    |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                                                               |
 *     |           Suggested External IP Address (128 bits)            |
 *     |                                                               |
 *     |                                                               |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |       Remote Peer Port        |     Reserved (16 bits)        |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |                                                               |
 *     |               Remote Peer IP Address (128 bits)               |
 *     |                                                               |
 *     |                                                               |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 *                      Figure 11: PEER Opcode Request
 *
 *   These fields are described below:
 *
 *   Requested Lifetime (in common header):  Requested lifetime of this
 *      mapping, in seconds.  Note that it is not possible to reduce the
 *      lifetime of a mapping (or delete it, with requested lifetime=0)
 *      using PEER.
 *
 *   Mapping Nonce:  Random value chosen by the PCP client.  See
 *      Section 12.2, "Generating a PEER Request".  Zero is a legal value
 *      (but unlikely, occurring in roughly one in 2^96 requests).
 *
 *   Protocol:  Upper-layer protocol associated with this Opcode.  Values
 *      are taken from the IANA protocol registry [proto_numbers].  For
 *      example, this field contains 6 (TCP) if the Opcode is describing a
 *      TCP mapping.  This field contains 17 (UDP) if the Opcode is
 *      describing a UDP mapping.  Protocol MUST NOT be zero.
 *
 *   Reserved:  24 reserved bits, MUST be set to 0 on transmission and
 *      MUST be ignored on reception.
 *
 *   Internal Port:  Internal port for the mapping.  Internal port MUST
 *      NOT be zero.
 *
 *   Suggested External Port:  Suggested external port for the mapping.
 *      If the PCP client does not know the external port, or does not
 *      have a preference, it MUST use 0.
 *
 *   Suggested External IP Address:  Suggested external IP address for the
 *      mapping.  If the PCP client does not know the external address, or
 *      does not have a preference, it MUST use the address-family-
 *      specific all-zeros address (see Section 5).
 *
 *   Remote Peer Port:  Remote peer's port for the mapping.  Remote peer
 *      port MUST NOT be zero.
 *
 *   Reserved:  16 reserved bits, MUST be set to 0 on transmission and
 *      MUST be ignored on reception.
 *
 *   Remote Peer IP Address:  Remote peer's IP address.  This is from the
 *      perspective of the PCP client, so that the PCP client does not
 *      need to concern itself with NAT64 or NAT46 (which both cause the
 *      client's idea of the remote peer's IP address to differ from the
 *      remote peer's actual IP address).  This field allows the PCP
 *      client and PCP server to disambiguate multiple connections from
 *      the same port on the internal host to different servers.  An IPv6
 *      address is represented directly, and an IPv4 address is
 *      represented using the IPv4-mapped address syntax (Section 5).
 *
 *   When attempting to re-create a lost mapping, the suggested external
 *   IP address and port are set to the External IP Address and Port
 *   fields received in a previous PEER response from the PCP server.  On
 *   an initial PEER request, the external IP address and port are set to
 *   zero.
 *
 *   Note that semantics similar to the PREFER_FAILURE option are
 *   automatically implied by PEER requests.  If the Suggested External IP
 *   Address or Suggested External Port fields are non-zero, and the PCP
 *   server is unable to honor the suggested external IP address,
 *   protocol, or port, then the PCP server MUST return a
 *
 *   CANNOT_PROVIDE_EXTERNAL error response.  The PREFER_FAILURE option is
 *   neither required nor allowed in PEER requests, and if a PCP server
 *   receives a PEER request containing the PREFER_FAILURE option it MUST
 *   return a MALFORMED_REQUEST error response.
 * </pre>
 * @author Kasra Faghihi
 */
public final class PeerPcpRequest extends PcpRequest {
    private static final int OPCODE = 2;
    private static final int DATA_LENGTH = 56;
    private static final int NONCE_LENGTH = 12;
    
    private byte[] mappingNonce;
    private int protocol;
    private int internalPort;
    private int suggestedExternalPort;
    private InetAddress suggestedExternalIpAddress;
    private int remotePeerPort;
    private InetAddress remotePeerIpAddress;
    
    /**
     * Constructs a {@link PeerPcpRequest} object.
     * @param mappingNonce random value used to map requests to responses
     * @param protocol IANA protocol number
     * @param internalPort internal port
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param suggestedExternalIpAddress suggested external IP address ({@code ::} for no preference)
     * @param remotePeerPort remote port
     * @param remotePeerIpAddress remote IP address
     * @param lifetime requested lifetime in seconds
     * @param internalIp IP address on the interface used to access the PCP server
     * @param options PCP options to use
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if {@code 0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12 || 1 > protocol > 255
     * || 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || 1 > remotePeerPort > 65535}
     */
    public PeerPcpRequest(byte[] mappingNonce, int protocol, int internalPort, int suggestedExternalPort,
            InetAddress suggestedExternalIpAddress, int remotePeerPort, InetAddress remotePeerIpAddress, long lifetime,
            InetAddress internalIp, PcpOption ... options) {
        super(OPCODE, lifetime, internalIp, DATA_LENGTH, options);
        
        Validate.notNull(mappingNonce);
        Validate.notNull(suggestedExternalIpAddress);
        Validate.notNull(remotePeerIpAddress);

        this.mappingNonce = Arrays.copyOf(mappingNonce, mappingNonce.length);
        this.protocol = protocol;
        this.internalPort = internalPort;
        this.suggestedExternalPort = suggestedExternalPort;
        this.suggestedExternalIpAddress = suggestedExternalIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
        this.remotePeerPort = remotePeerPort;
        this.remotePeerIpAddress = remotePeerIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
        
        validateState();
    }
    
    /**
     * Constructs a {@link MapPcpRequest} object by parsing a buffer.
     * @param buffer buffer containing PCP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 0 || op == 2 || 0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12
     * || 1 > protocol > 255 || 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || 1 > remotePeerPort > 65535}) or contains an
     * unparseable options region.
     */
    public PeerPcpRequest(byte[] buffer) {
        super(buffer, DATA_LENGTH);
        
        Validate.isTrue(super.getOp() == OPCODE);
        
        int remainingLength = buffer.length - HEADER_LENGTH;
        Validate.isTrue(remainingLength >= DATA_LENGTH); // FYI: remaining length = data block len + options len
        
        int offset = HEADER_LENGTH;
        
        mappingNonce = new byte[NONCE_LENGTH];
        System.arraycopy(buffer, offset, mappingNonce, 0, mappingNonce.length);
        offset += mappingNonce.length;
        
        protocol = buffer[offset] & 0xFF;
        offset++;
        
        offset += 3; // 3 reserved bytes
        
        internalPort = InternalUtils.bytesToShort(buffer, offset) & 0xFFFF;
        offset += 2;
        
        suggestedExternalPort = InternalUtils.bytesToShort(buffer, offset) & 0xFFFF;
        offset += 2;

        suggestedExternalIpAddress = NetworkUtils.convertBytesToAddress(buffer, offset, 16);
        offset += 16;

        remotePeerPort = InternalUtils.bytesToShort(buffer, offset) & 0xFFFF;
        offset += 2;
        
        offset += 2; // reserved
        
        remotePeerIpAddress = NetworkUtils.convertBytesToAddress(buffer, offset, 16);
        offset += 16;
        
        
        validateState();
    }
    
    private void validateState() {
        Validate.notNull(mappingNonce);
        Validate.isTrue(mappingNonce.length == NONCE_LENGTH);
        Validate.inclusiveBetween(1, 255, protocol);
        Validate.inclusiveBetween(1, 65535, internalPort); // must not be 0
        Validate.inclusiveBetween(0, 65535, suggestedExternalPort); // 0 = no preference
        Validate.notNull(suggestedExternalIpAddress);
        Validate.inclusiveBetween(1, 65535, remotePeerPort); // cannot be 0
        Validate.notNull(remotePeerIpAddress);
    }

    @Override
    public byte[] getData() {
        byte[] data = new byte[DATA_LENGTH];
        
        int offset = 0;
        
        System.arraycopy(mappingNonce, 0, data, offset, mappingNonce.length);
        offset += mappingNonce.length;
        
        data[offset] = (byte) protocol;
        offset++;
        
        offset += 3; // 3 reserved bytes
        
        InternalUtils.shortToBytes(data, offset, (short) internalPort);
        offset += 2;
        
        InternalUtils.shortToBytes(data, offset, (short) suggestedExternalPort);
        offset += 2;
        
        byte[] ipv6Array = NetworkUtils.convertAddressToIpv6Bytes(suggestedExternalIpAddress);
        System.arraycopy(ipv6Array, 0, data, offset, ipv6Array.length);
        offset += ipv6Array.length;

        InternalUtils.shortToBytes(data, offset, (short) remotePeerPort);
        offset += 2;
        
        offset += 2; // 2 reserved bytes

        ipv6Array = NetworkUtils.convertAddressToIpv6Bytes(remotePeerIpAddress);
        System.arraycopy(ipv6Array, 0, data, offset, ipv6Array.length);
        offset += ipv6Array.length;
        
        return data;
    }

    /**
     * Get nonce.
     * @return nonce
     */
    public byte[] getMappingNonce() {
        return Arrays.copyOf(mappingNonce, mappingNonce.length);
    }

    /**
     * Get IANA protocol number.
     * @return IANA protocol number
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Get internal port number.
     * @return internal port number
     */
    public int getInternalPort() {
        return internalPort;
    }

    /**
     * Get suggested external port number.
     * @return suggested external port number
     */
    public int getSuggestedExternalPort() {
        return suggestedExternalPort;
    }

    /**
     * Get suggested external IP address.
     * @return suggested external IP address
     */
    public InetAddress getSuggestedExternalIpAddress() {
        return suggestedExternalIpAddress;
    }

    /**
     * Get remote peer port number.
     * @return remote peer port number
     */
    public int getRemotePeerPort() {
        return remotePeerPort;
    }

    /**
     * Get remote peer IP address.
     * @return remote peer IP address
     */
    public InetAddress getRemotePeerIpAddress() {
        return remotePeerIpAddress;
    }

    @Override
    public String toString() {
        return "PeerPcpRequest{super=" + super.toString() + "mappingNonce=" + Arrays.toString(mappingNonce) + ", protocol=" + protocol
                + ", internalPort=" + internalPort + ", suggestedExternalPort=" + suggestedExternalPort + ", suggestedExternalIpAddress="
                + suggestedExternalIpAddress + ", remotePeerPort=" + remotePeerPort + ", remotePeerIpAddress=" + remotePeerIpAddress + '}';
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 43 * hash + Arrays.hashCode(this.mappingNonce);
        hash = 43 * hash + this.protocol;
        hash = 43 * hash + this.internalPort;
        hash = 43 * hash + this.suggestedExternalPort;
        hash = 43 * hash + Objects.hashCode(this.suggestedExternalIpAddress);
        hash = 43 * hash + this.remotePeerPort;
        hash = 43 * hash + Objects.hashCode(this.remotePeerIpAddress);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PeerPcpRequest other = (PeerPcpRequest) obj;
        if (this.protocol != other.protocol) {
            return false;
        }
        if (this.internalPort != other.internalPort) {
            return false;
        }
        if (this.suggestedExternalPort != other.suggestedExternalPort) {
            return false;
        }
        if (this.remotePeerPort != other.remotePeerPort) {
            return false;
        }
        if (!Arrays.equals(this.mappingNonce, other.mappingNonce)) {
            return false;
        }
        if (!Objects.equals(this.suggestedExternalIpAddress, other.suggestedExternalIpAddress)) {
            return false;
        }
        if (!Objects.equals(this.remotePeerIpAddress, other.remotePeerIpAddress)) {
            return false;
        }
        return true;
    }
}
