/*
 * Copyright (c) 2013-2014, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.pcp.messages;

import com.offbynull.portmapper.common.NetworkUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Represents a FILTER PCP option. From the RFC:
 * <pre>
    The FILTER option is formatted as follows:
 
       0                   1                   2                   3
       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      | Option Code=3 |  Reserved     |   Option Length=20            |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |    Reserved   | Prefix Length |      Remote Peer Port         |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                                                               |
      |               Remote Peer IP remotePeerIpAddress (128 bits)               |
      |                                                               |
      |                                                               |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 
                       Figure 15: FILTER Option Layout
 
    These fields are described below:
 
    Reserved:  8 reserved bits, MUST be sent as 0 and MUST be ignored
       when received.
 
    Prefix Length:  indicates how many bits of the IPv4 or IPv6 remotePeerIpAddress
       are relevant for this filter.  The value 0 indicates "no filter",
       and will remove all previous filters.  See below for detail.
 
    Remote Peer Port:  the port number of the remote peer.  The value 0
       indicates "all ports".
 
    Remote Peer IP remotePeerIpAddress:  The IP remotePeerIpAddress of the remote peer.
 
       Option Name: FILTER
       Number: 3
       Purpose: specifies a filter for incoming packets
       Valid for Opcodes: MAP
       Length: 20 octets
       May appear in: request.  May appear in response only if it
       appeared in the associated request.
       Maximum occurrences: as many as fit within maximum PCP message
       size
 </pre>
 * @author Kasra Faghihi
 */
public final class FilterPcpOption extends PcpOption {
    private int prefixLength;
    private int remotePeerPort;
    private InetAddress remotePeerIpAddress;
    
    /**
     * Constructs a {@link FilterPcpOption} by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if option code is not {@code 3}, or if the field {@code prefixLength > 128}
     */
    public FilterPcpOption(ByteBuffer buffer) {
        super(buffer);
        
        Validate.isTrue(super.getCode() == 3);
        
        buffer.get(); // reserved
        prefixLength = buffer.get() & 0xFF;
        remotePeerPort = buffer.getShort() & 0xFFFF;
        
        Validate.inclusiveBetween(0, 128, prefixLength); // 0 indicates 'no filter'
        Validate.inclusiveBetween(0, 65535, remotePeerPort); // 0 indicates 'all ports', should never trigger
        
        byte[] addrArr = new byte[16];
        buffer.get(addrArr);
        try {
            remotePeerIpAddress = InetAddress.getByAddress(addrArr);
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe); // should never happen
        }
    }
    
    /**
     * Constructs a {@link FilterPcpOption}.
     * @param prefixLength prefix length ({@code 0} = no filter}
     * @param remotePeerPort remote peer port ({@code 0} = all ports)
     * @param remotePeerIpAddress remote peer IP remotePeerIpAddress
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code prefixLength < 0 || > 128}, or if {@code remotePeerPort < 0 || > 65535} 
     */
    public FilterPcpOption(int prefixLength, int remotePeerPort, InetAddress remotePeerIpAddress) {
        super(3, toDataSection(prefixLength, remotePeerPort, remotePeerIpAddress));
        
        this.prefixLength = prefixLength;
        this.remotePeerPort = remotePeerPort;
        this.remotePeerIpAddress = remotePeerIpAddress;
    }
    
    private static ByteBuffer toDataSection(int prefixLength, int remotePeerPort, InetAddress remotePeerIpAddress) {
        Validate.inclusiveBetween(0, 128, prefixLength); // 0 indicates 'no filter'
        Validate.inclusiveBetween(0, 65535, remotePeerPort); // 0 indicates 'all ports'
        Validate.notNull(remotePeerIpAddress);
        
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put((byte) 0); // reserved
        buffer.put((byte) prefixLength);
        buffer.putShort((short) remotePeerPort);
        buffer.put(NetworkUtils.convertToIpv6Array(remotePeerIpAddress));
        
        return buffer;
    }

    /**
     * Get the prefix length.
     * @return prefix length
     */
    public int getPrefixLength() {
        return prefixLength;
    }

    /**
     * Get the remote peer port.
     * @return remote peer port
     */
    public int getRemotePeerPort() {
        return remotePeerPort;
    }

    /**
     * Get the remote IP address.
     * @return remote IP address
     */
    public InetAddress getRemotePeerIpAddress() {
        return remotePeerIpAddress;
    }
    
}
