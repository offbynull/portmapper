/*
 * Copyright (c) 2013-2016, Kasra Faghihi, All rights reserved.
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
package com.offbynull.portmapper.io;

import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.common.ByteBufferUtils;
import com.offbynull.portmapper.io.UdpNetworkEntry.AddressedByteBuffer;
import com.offbynull.portmapper.io.messages.ConnectedTcpNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkRequest;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkResponse;
import com.offbynull.portmapper.io.messages.ErrorNetworkResponse;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesRequest;
import com.offbynull.portmapper.io.messages.GetLocalIpAddressesResponse;
import com.offbynull.portmapper.io.messages.IdentifiableErrorNetworkResponse;
import com.offbynull.portmapper.io.messages.KillNetworkRequest;
import com.offbynull.portmapper.io.messages.ReadTcpNetworkNotification;
import com.offbynull.portmapper.io.messages.ReadUdpNetworkNotification;
import com.offbynull.portmapper.io.messages.WriteEmptyTcpNetworkNotification;
import com.offbynull.portmapper.io.messages.WriteEmptyUdpNetworkNotification;
import com.offbynull.portmapper.io.messages.WriteTcpNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteTcpNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteUdpNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteUdpNetworkResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Network communication gateway.
 *
 * @author Kasra Faghihi
 */
public final class NetworkGateway {

    private GatewayRunnable runnable;
    private Thread thread;
    
    public static NetworkGateway create() {
        NetworkGateway ng = new NetworkGateway();
        
        ng.runnable = new GatewayRunnable();
        ng.thread = new Thread(ng.runnable);
        
        ng.thread.start();
        
        return ng;
    }
    
    private NetworkGateway() {
        // do nothing
    }
    
    public Bus getBus() {
        return runnable.bus;
    }
    
    public void join() throws InterruptedException {
        thread.join();
    }
    
    private static final class GatewayRunnable implements Runnable {
        private final Bus bus;
        private final LinkedBlockingQueue<Object> queue;
        private final Selector selector;
        private int nextId = 0;

        public GatewayRunnable() {
            try {
                selector = Selector.open();
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            queue = new LinkedBlockingQueue<>();
            bus = new NetworkBus(selector, queue);
        }

        private Map<Integer, NetworkEntry<?>> idMap = new HashMap<>();
        private Map<Channel, NetworkEntry<?>> channelMap = new HashMap<>();
        private ByteBuffer buffer = ByteBuffer.allocate(65535);

        @Override
        public void run() {
            try {
                while (true) {
                    selector.select();

                    for (SelectionKey key : selector.selectedKeys()) {
                        if (!key.isValid()) {
                            continue;
                        }

                        Channel channel = (Channel) key.channel();

                        NetworkEntry<?> entry = channelMap.get(channel);
                        if (entry == null) {
                            channel.close();
                            continue;
                        }

                        try {
                            if (channel instanceof SocketChannel) {
                                handleSelectForTcpChannel(key, (TcpNetworkEntry) entry);
                            } else if (channel instanceof DatagramChannel) {
                                handleSelectForUdpChannel(key, (UdpNetworkEntry) entry);
                            } else {
                                throw new IllegalStateException(); // should never happen
                            }

                            updateReadWriteSelectionKey(entry, (AbstractSelectableChannel) channel);
                        } catch (SocketException | RuntimeException e) {
                            shutdownResource(channel);
                        }
                    }

                    LinkedList<Object> msgs = new LinkedList<>();
                    queue.drainTo(msgs);

                    for (Object msg : msgs) {
                        processMessage(msg);
                    }
                }
            } catch (Exception e) {
                shutdownResources();
                throw new RuntimeException(e);
            }
        }

        private void handleSelectForTcpChannel(SelectionKey selectionKey, TcpNetworkEntry entry) throws IOException {
            SocketChannel channel = (SocketChannel) entry.getChannel();
            Bus responseBus = entry.getResponseBus();
            int id = entry.getId();
            
            if (selectionKey.isConnectable()) {
                try {
                    // This block is sometimes called more than once for each connection -- we still call finishConnect but we also check to
                    // see if we're already connected before sending the CreateTcpSocketNetworkResponse msg
                    boolean alreadyConnected = channel.isConnected();
                    boolean connected = channel.finishConnect();
                    if (!alreadyConnected && connected) {
                        responseBus.send(new ConnectedTcpNetworkResponse(id));
                    }
                } catch (IOException ioe) {
                    // socket failed to connect
                    throw new RuntimeException(); // goes up the chain and shuts down the channel
                }
            }
            
            if (selectionKey.isReadable()) {
                buffer.clear();

                int readCount = channel.read(buffer);
                buffer.flip();
                
                if (readCount == -1) {
                    // socket disconnected
                    throw new RuntimeException(); // goes up the chain and shuts down the channel
                } else if (buffer.remaining() > 0) {
                    byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
                    Object readResp = new ReadTcpNetworkNotification(id, bufferAsArray);
                    responseBus.send(readResp);
                } else if (readCount == 0) {
                    // do nothing
                } else {
                    throw new IllegalStateException();
                }
            }
            
            if (selectionKey.isWritable()) {
                LinkedList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();

                // if OP_WRITE was set, WriteTcpBlockNetworkRequest is pending (we should have at least 1 outgoing buffer)
                int writeCount = 0;
                if (outBuffers.isEmpty() && !entry.isNotifiedOfWritable()) { // if empty but not notified yet
                    entry.setNotifiedOfWritable(true);
                    entry.getResponseBus().send(new WriteEmptyTcpNetworkNotification(id));
                } else {
                    while (!outBuffers.isEmpty()) {
                        ByteBuffer outBuffer = outBuffers.getFirst();

                        writeCount += channel.write(outBuffer);
                        if (outBuffer.remaining() > 0) {
                            // not everything was written, which means we can't send anymore data until we get another OP_WRITE, so leave
                            break;
                        }

                        outBuffers.removeFirst();

                        Object writeResp = new WriteTcpNetworkResponse(id, writeCount);
                        responseBus.send(writeResp);
                    }
                }
            }
        }

        private void handleSelectForUdpChannel(SelectionKey selectionKey, UdpNetworkEntry entry) throws IOException {
            DatagramChannel channel = (DatagramChannel) entry.getChannel();
            Bus responseBus = entry.getResponseBus();
            int id = entry.getId();

            if (selectionKey.isReadable()) {
                buffer.clear();
                InetSocketAddress incomingSocketAddress = (InetSocketAddress) channel.receive(buffer);
                if (incomingSocketAddress != null) {
                    buffer.flip();
                    byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
                    Object readResp = new ReadUdpNetworkNotification(id, incomingSocketAddress, bufferAsArray);
                    responseBus.send(readResp);
                }
            }
            
            if (selectionKey.isWritable()) {
                LinkedList<AddressedByteBuffer> outBuffers = entry.getOutgoingBuffers();
                if (!outBuffers.isEmpty()) { // if not empty
                    AddressedByteBuffer outBuffer = outBuffers.removeFirst();

                    int writeCount = channel.send(outBuffer.getBuffer(), outBuffer.getSocketAddress());

                    Object writeResp = new WriteUdpNetworkResponse(id, writeCount);
                    responseBus.send(writeResp);
                } else if (!entry.isNotifiedOfWritable()) { // if empty but not notified yet
                    entry.setNotifiedOfWritable(true);
                    entry.getResponseBus().send(new WriteEmptyUdpNetworkNotification(id));
                }
            }
        }

        private void updateReadWriteSelectionKey(NetworkEntry<?> entry, AbstractSelectableChannel channel) throws ClosedChannelException {
            int newKey = SelectionKey.OP_READ;
            if (!entry.getOutgoingBuffers().isEmpty()) { // if not empty 
                newKey |= SelectionKey.OP_WRITE;
                entry.setNotifiedOfWritable(false);
            } else if (!entry.isNotifiedOfWritable()) { // if is empty but not notified yet
                newKey |= SelectionKey.OP_WRITE;
            }

            if (newKey != entry.getSelectionKey()) {
                entry.setSelectionKey(newKey);
                channel.register(selector, newKey); // register new key if different -- calling register may have performance issues?
            }
        }

        private void setSelectionKey(NetworkEntry<?> entry, AbstractSelectableChannel channel, int selectionKey)
                throws ClosedChannelException {
            if (selectionKey != entry.getSelectionKey()) {
                entry.setSelectionKey(selectionKey);
                channel.register(selector, selectionKey); // register new key if different -- calling register may have performance issues?
            }
        }

        private void processMessage(Object msg) throws IOException {
            if (msg instanceof CreateUdpSocketNetworkRequest) {
                CreateUdpSocketNetworkRequest req = (CreateUdpSocketNetworkRequest) msg;

                Bus responseBus = req.getResponseBus();

                try {
                    DatagramChannel channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    channel.bind(new InetSocketAddress(req.getSourceAddress(), 0));

                    int id = nextId++;
                    UdpNetworkEntry entry = new UdpNetworkEntry(id, channel, responseBus);
                    idMap.put(id, entry);
                    channelMap.put(channel, entry);
                    
                    responseBus.send(new CreateUdpSocketNetworkResponse(id));
                    
                    updateReadWriteSelectionKey(entry, channel);
                } catch (RuntimeException re) {
                    responseBus.send(new ErrorNetworkResponse());
                }
            } else if (msg instanceof CreateTcpSocketNetworkRequest) {
                CreateTcpSocketNetworkRequest req = (CreateTcpSocketNetworkRequest) msg;

                Bus responseBus = req.getResponseBus();

                try {
                    SocketChannel channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    channel.bind(new InetSocketAddress(req.getSourceAddress(), 0));
                    
                    InetSocketAddress dst = new InetSocketAddress(req.getDestinationAddress(), req.getDestinationPort());
                    channel.connect(dst);

                    int id = nextId++;
                    TcpNetworkEntry entry = new TcpNetworkEntry(id, channel, responseBus);
                    idMap.put(id, entry);
                    channelMap.put(channel, entry);
                    
                    setSelectionKey(entry, channel, SelectionKey.OP_CONNECT);

                    responseBus.send(new CreateTcpSocketNetworkResponse(id));
                } catch (RuntimeException re) {
                    responseBus.send(new ErrorNetworkResponse());
                }
            } else if (msg instanceof DestroySocketNetworkRequest) {
                DestroySocketNetworkRequest req = (DestroySocketNetworkRequest) msg;

                Bus responseBus = null;
                int id = req.getId();
                try {
                    NetworkEntry<?> entry = idMap.get(id);

                    responseBus = entry.getResponseBus();
                    Channel channel = entry.getChannel();

                    idMap.remove(id);
                    channelMap.remove(channel);

                    channel.close();

                    responseBus.send(new DestroySocketNetworkResponse(id));
                } catch (RuntimeException re) {
                    if (responseBus != null) {
                        responseBus.send(new IdentifiableErrorNetworkResponse(id));
                    }
                }
            } else if (msg instanceof WriteTcpNetworkRequest) {
                WriteTcpNetworkRequest req = (WriteTcpNetworkRequest) msg;

                Bus responseBus = null;
                int id = req.getId();
                try {
                    TcpNetworkEntry entry = (TcpNetworkEntry) idMap.get(id);

                    responseBus = entry.getResponseBus();

                    LinkedList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();
                    ByteBuffer writeBuffer = ByteBuffer.wrap(req.getData());
                    if (writeBuffer.hasRemaining()) { // only add if it has content -- adding empty is worthless because this is a stream
                        outBuffers.add(writeBuffer);
                    }
                    
                    AbstractSelectableChannel channel = (AbstractSelectableChannel) entry.getChannel();
                    updateReadWriteSelectionKey(entry, channel);
                } catch (RuntimeException re) {
                    if (responseBus != null) {
                        responseBus.send(new IdentifiableErrorNetworkResponse(id));
                    }
                }
            } else if (msg instanceof WriteUdpNetworkRequest) {
                WriteUdpNetworkRequest req = (WriteUdpNetworkRequest) msg;

                Bus responseBus = null;
                int id = req.getId();
                try {
                    UdpNetworkEntry entry = (UdpNetworkEntry) idMap.get(id);

                    responseBus = entry.getResponseBus();

                    LinkedList<AddressedByteBuffer> outBuffers = entry.getOutgoingBuffers();
                    ByteBuffer writeBuffer = ByteBuffer.wrap(req.getData());
                    InetSocketAddress writeAddress = req.getOutgoingSocketAddress();
                    outBuffers.add(new AddressedByteBuffer(writeBuffer, writeAddress));
                    
                    AbstractSelectableChannel channel = (AbstractSelectableChannel) entry.getChannel();
                    updateReadWriteSelectionKey(entry, channel);
                } catch (RuntimeException re) {
                    if (responseBus != null) {
                        responseBus.send(new IdentifiableErrorNetworkResponse(id));
                    }
                }
            } else if (msg instanceof GetLocalIpAddressesRequest) {
                GetLocalIpAddressesRequest req = (GetLocalIpAddressesRequest) msg;

                Set<InetAddress> ret = new HashSet<>();
                Bus responseBus = req.getResponseBus();
                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = interfaces.nextElement();
                        Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
                        while (addrs.hasMoreElements()) {
                            InetAddress addr = addrs.nextElement();

                            if (!addr.isLoopbackAddress()) {
                                ret.add(addr);
                            }
                        }
                    }
                    responseBus.send(new GetLocalIpAddressesResponse(ret));
                } catch (RuntimeException re) {
                    if (responseBus != null) {
                        responseBus.send(new ErrorNetworkResponse());
                    }
                }
            } else if (msg instanceof KillNetworkRequest) {
                throw new RuntimeException("Kill requested");
            }
        }

        private void shutdownResources() {
            for (Entry<Channel, NetworkEntry<?>> entry : channelMap.entrySet()) {
                Channel channel = entry.getKey();
                NetworkEntry<?> networkEntry = entry.getValue();
                try {
                    int id = networkEntry.getId();
                    networkEntry.getResponseBus().send(new IdentifiableErrorNetworkResponse(id));
                    channel.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
            try {
                selector.close();
            } catch (Exception e) {
                // do nothing
            }

            channelMap.clear();
            idMap.clear();
        }

        private void shutdownResource(Channel channel) {
            NetworkEntry<?> ne = channelMap.remove(channel);
            
            int id = ne.getId();
            idMap.remove(id);
            
            try {
                ne.getResponseBus().send(new IdentifiableErrorNetworkResponse(id));
                channel.close();
            } catch (Exception e) {
                // do nothing
            }
        }
    }
}
