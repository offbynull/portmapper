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
package com.offbynull.portmapper.pcp;

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
 * Utility class used to discover PCP routers.
 * @author Kasra Faghihi
 */
public final class PcpDiscovery {
    private PcpDiscovery() {
        
    }
    
    /**
     * Discover PCP-enabled routers.
     * @param extraAddresses extra addresses to check
     * @return a collection of discovered PCP devices
     * @throws InterruptedException if interrupted
     * @throws IOException if IO error occurs
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     */
    public static Set<DiscoveredPcpDevice> discover(InetAddress ... extraAddresses) throws InterruptedException, IOException {
        Validate.noNullElements(extraAddresses);
        
        Set<InetAddress> gateways = discoverGateways();
        gateways.addAll(Arrays.asList(extraAddresses));
        Map<InetAddress, InetAddress> localAddressToGatewayMap = discoverLocalAddressesToGateways(gateways);
        
        Set<DiscoveredPcpDevice> devices = new HashSet<>();
        for (Entry<InetAddress, InetAddress> e : localAddressToGatewayMap.entrySet()) {
            devices.add(new DiscoveredPcpDevice(e.getKey(), e.getValue()));
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
                    // make sure version is 2 and error isn't ADDRESS_MISMATCH and we're good to go
                    if (packet.remaining() < 4 || packet.get(0) == 2 && packet.get(4) == PcpResultCode.ADDRESS_MISMATCH.ordinal()) {
                        return;
                    }

                    InetAddress localAddress = bindMap.get(channel);
                    if (localAddress == null) {
                        return;
                    }
                    localAddrToGatewayAddrMap.put(localAddress, sourceAddress.getAddress());
                }
            });


            for (DatagramChannel channel : bindMap.keySet()) {
                for (InetAddress gateway : gateways) {
                    ByteBuffer outBuf = ByteBuffer.allocate(1100);
                    MapPcpRequest mpr = new MapPcpRequest(ByteBuffer.allocate(12), 0, 0, 0, InetAddress.getByName("::"), 0L);
                    mpr.dump(outBuf, bindMap.get(channel));
                    outBuf.flip();
                    
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
                    foundGateways.add(sourceAddress.getAddress());
                }
            });

            ByteBuffer outBuf = ByteBuffer.allocate(1100);
            MapPcpRequest mpr = new MapPcpRequest(ByteBuffer.allocate(12), 0, 0, 0, InetAddress.getByName("::"), 0L);
            mpr.dump(outBuf, InetAddress.getByAddress(new byte[4])); // should get back an error for this, but this
                                                                       // should be fine because all we're looking for is a response, not
                                                                       // nessecarily a correct response -- self address being sent is
                                                                       // 0.0.0.0 (IPV4)
                                                                       //
                                                                       // also, we need to pass in MAP because Apple's garbage routers
                                                                       // give back NATPMP responses when you pass in ANNOUNCE
            
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
        
        foundGateways.retainAll(potentialGateways); // just incase we get back some unsolicited responses
        return new HashSet<>(foundGateways);
    }
    
}
