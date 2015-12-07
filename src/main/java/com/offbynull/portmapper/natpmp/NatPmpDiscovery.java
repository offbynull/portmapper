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
import com.offbynull.portmapper.natpmp.messages.ExternalAddressNatPmpRequest;
import com.offbynull.portmapper.natpmp.messages.ExternalAddressNatPmpResponse;
import com.offbynull.portmapper.common.NetworkUtils;
import com.offbynull.portmapper.common.UdpCommunicator;
import com.offbynull.portmapper.common.UdpCommunicatorListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * Utility class used to discover NAT-PMP routers.
 * @author Kasra Faghihi
 */
public final class NatPmpDiscovery {
    private NatPmpDiscovery() {
        
    }
    
    /**
     * Discover NAT-PMP-enabled routers.
     * @param extraAddresses extra addresses to check
     * @return a collection of discovered PCP devices
     * @throws InterruptedException if interrupted
     * @throws IOException if IO error
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     */
    public static Set<DiscoveredNatPmpDevice> discover(InetAddress ... extraAddresses) throws InterruptedException, IOException {
        Validate.noNullElements(extraAddresses);
        
        Set<InetAddress> gateways = discoverGateways();
        gateways.addAll(Arrays.asList(extraAddresses));
        Map<InetAddress, InetAddress> localAddressToGatewayMap = discoverLocalAddressesToGateways(gateways);
        
        Set<DiscoveredNatPmpDevice> devices = new HashSet<>();
        for (Entry<InetAddress, InetAddress> e : localAddressToGatewayMap.entrySet()) {
            devices.add(new DiscoveredNatPmpDevice(e.getKey(), e.getValue()));
        }
        return devices;
    }
    
    private static Map<InetAddress, InetAddress> discoverLocalAddressesToGateways(Set<InetAddress> gateways) throws IOException,
            InterruptedException {
        Set<InetAddress> localAddresses = NetworkUtils.getAllLocalIpv4Addresses();
        List<DatagramChannel> channels = new ArrayList<>();
        final Map<DatagramChannel, InetAddress> bindMap =
                Collections.synchronizedMap(new HashMap<DatagramChannel, InetAddress>());
        final Map<InetAddress, InetAddress> localAddrToGatewayAddrMap =
                Collections.synchronizedMap(new HashMap<InetAddress, InetAddress>());
        
        try {
            for (InetAddress localAddress : localAddresses) {
                DatagramChannel unicastChannel = null;
                try {
                    unicastChannel = DatagramChannel.open();
                    unicastChannel.configureBlocking(false);
                    unicastChannel.socket().bind(new InetSocketAddress(localAddress, 0));
                } catch (IOException ioe) {
                    IOUtils.closeQuietly(unicastChannel);
                    throw ioe;
                }
                
                channels.add(unicastChannel);
                bindMap.put(unicastChannel, localAddress);
            }
        } catch (IOException ioe) {
            for (DatagramChannel channel : channels) {
                IOUtils.closeQuietly(channel);
            }
            throw ioe;
        }
        

        UdpCommunicator communicator = null;
        try {
            communicator = new UdpCommunicator(channels);
            communicator.startAsync().awaitRunning();
            communicator.addListener(new UdpCommunicatorListener() {

                @Override
                public void incomingPacket(InetSocketAddress sourceAddress, DatagramChannel channel, ByteBuffer packet) {
                    byte[] packetData = ByteBufferUtils.copyContentsToArray(packet, true);
                    new ExternalAddressNatPmpResponse(packetData); // should error out if not valid
                    
                    InetAddress localAddress = bindMap.get(channel);
                    if (localAddress == null) {
                        return;
                    }
                    localAddrToGatewayAddrMap.put(localAddress, sourceAddress.getAddress());
                }
            });

            ExternalAddressNatPmpRequest eanpr = new ExternalAddressNatPmpRequest();
            byte[] eanprData = eanpr.dump();
            ByteBuffer outBuf = ByteBuffer.wrap(eanprData);
            
            outBuf.flip();


            for (DatagramChannel channel : bindMap.keySet()) {
                for (InetAddress gateway : gateways) {
                    communicator.send(channel, new InetSocketAddress(gateway, 5351), outBuf.asReadOnlyBuffer());
                }
            }

            Thread.sleep(5000L);
        } finally {
            if (communicator != null) {
                communicator.stopAsync().awaitTerminated();
            }
        }
        
        return new HashMap<>(localAddrToGatewayAddrMap);
    }
    
    private static Set<InetAddress> discoverGateways() throws InterruptedException, IOException {
        final Set<InetAddress> foundGateways = Collections.synchronizedSet(new HashSet<InetAddress>());
        Set<InetAddress> potentialGateways = NetworkUtils.getPotentialGatewayAddresses(); // port 5351

        DatagramChannel unicastChannel = null;
        try {
            unicastChannel = DatagramChannel.open();
            unicastChannel.configureBlocking(false);
            unicastChannel.socket().bind(new InetSocketAddress(0));
        } catch (IOException ioe) {
            IOUtils.closeQuietly(unicastChannel);
            throw ioe;
        }
        
        UdpCommunicator communicator = null;
        try {
            communicator = new UdpCommunicator(Collections.singletonList(unicastChannel));
            communicator.startAsync().awaitRunning();
            communicator.addListener(new UdpCommunicatorListener() {

                @Override
                public void incomingPacket(InetSocketAddress sourceAddress, DatagramChannel channel, ByteBuffer packet) {
                    byte[] packetData = ByteBufferUtils.copyContentsToArray(packet, true);
                    new ExternalAddressNatPmpResponse(packetData); // should error out if not valid
                    
                    foundGateways.add(sourceAddress.getAddress());
                }
            });

            ExternalAddressNatPmpRequest eanpr = new ExternalAddressNatPmpRequest();
            byte[] eanprData = eanpr.dump();
            ByteBuffer outBuf = ByteBuffer.wrap(eanprData);
            
            outBuf.flip();

            for (InetAddress potentialGateway : potentialGateways) {
                communicator.send(unicastChannel, new InetSocketAddress(potentialGateway, 5351), outBuf.asReadOnlyBuffer());
            }

            Thread.sleep(5000L);
        } finally {
            if (communicator != null) {
                communicator.stopAsync().awaitTerminated();
            }
        }
        
        return new HashSet<>(foundGateways);
    }
    
}
