/*
 * Copyright (c) 2013-2015, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.natpmp;

import com.offbynull.portmapper.common.ByteBufferUtils;
import com.offbynull.portmapper.natpmp.messages.ExternalAddressNatPmpResponse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * Listens for NAT-PMP events from a gateway/router. For best results, avoid using this and call
 * {@link NatPmpController#requestExternalAddress(int) } periodically to determine if the external IP address changed or the device rebooted
 * -- it's result has a 'seconds since last epoch' property {@link ExternalAddressNatPmpResponse#getSecondsSinceStartOfEpoch() } which you
 * can track to determine if the device rebooted.
 * <p>
 * For more information see section 3.2.1 of http://tools.ietf.org/html/rfc6886.
 *
 * @author Kasra Faghihi
 */
public final class NatPmpReceiver {
    private InetAddress gatewayAddress;
    private AtomicReference<MulticastSocket> currentSocket = new AtomicReference<>();

    /**
     * Construct a {@link NatPmpReceiver} object.
     * @param gatewayAddress gateway address
     * @throws NullPointerException if any argument is {@code null}
     */
    public NatPmpReceiver(InetAddress gatewayAddress) {
        Validate.notNull(gatewayAddress);
        
        this.gatewayAddress = gatewayAddress;
    }
    
    /**
     * Start listening for NAT-PMP events. This method blocks until {@link #stop() } is called.
     * @param listener listener to notify of events
     * @throws IOException if socket error occurs
     * @throws NullPointerException if any argument is {@code null}
     */
    public void start(NatPmpEventListener listener) throws IOException {
        Validate.notNull(listener);

        MulticastSocket socket = null;
        try {
            final InetAddress group = InetAddress.getByName("224.0.0.1"); // NOPMD
            final int port = 5350;
            final InetSocketAddress groupAddress = new InetSocketAddress(group, port);

            socket = new MulticastSocket(port);
            
            if (!currentSocket.compareAndSet(null, socket)) {
                IOUtils.closeQuietly(socket);
                return;
            }
            
            socket.setReuseAddress(true);
            
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
                while (addrs.hasMoreElements()) { // make sure atleast 1 ipv4 addr bound to interface
                    InetAddress addr = addrs.nextElement();
                    
                    try {
                        if (addr instanceof Inet4Address) {
                            socket.joinGroup(groupAddress, networkInterface);
                        }
                    } catch (IOException ioe) { // NOPMD
                        // occurs with certain interfaces
                        // do nothing
                    }
                }
            }

            ByteBuffer buffer = ByteBuffer.allocate(12);
            DatagramPacket data = new DatagramPacket(buffer.array(), buffer.capacity());

            while (true) {
                buffer.clear();
                socket.receive(data);
                buffer.position(data.getLength());
                buffer.flip();
                
                if (!data.getAddress().equals(gatewayAddress)) { // data isn't from our gateway, ignore
                    continue;
                }

                if (buffer.remaining() != 12) { // data isn't the expected size, ignore
                    continue;
                }

                int version = buffer.get(0);
                if (version != 0) { // data doesn't have the correct version, ignore
                    continue;
                }

                int opcode = buffer.get(1) & 0xFF;
                if (opcode != 128) { // data doesn't have the correct op, ignore
                    continue;
                }

                int resultCode = buffer.getShort(2) & 0xFFFF;
                switch (resultCode) {
                    case 0:
                        break;
                    default:
                        continue; // data doesn't have a successful result, ignore
                }
                
                byte[] bufferData = ByteBufferUtils.copyContentsToArray(buffer, true);
                listener.publicAddressUpdated(new ExternalAddressNatPmpResponse(bufferData));
            }

        } catch (IOException ioe) {
            if (currentSocket.get() == null) {
                return; // ioexception caused by interruption/stop, so just return without propogating error up
            }
            
            throw ioe;
        } finally {
            IOUtils.closeQuietly(socket);
            currentSocket.set(null);
        }
    }
    
    /**
     * Stop listening.
     */
    public void stop() {
        MulticastSocket socket = currentSocket.getAndSet(null);
        if (socket != null) {
            IOUtils.closeQuietly(socket);
        }
    }
}
