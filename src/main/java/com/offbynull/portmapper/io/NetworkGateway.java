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
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkRequest;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkResponse;
import com.offbynull.portmapper.io.messages.ErrorNetworkResponse;
import com.offbynull.portmapper.io.messages.KillNetworkRequest;
import com.offbynull.portmapper.io.messages.ReadTcpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.ReadUdpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteUdpBlockNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteUdpBlockNetworkResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Network communication gateway.
 *
 * @author Kasra Faghihi
 */
public final class NetworkGateway {

    private NetworkRunnable runnable;
    private Thread thread;
    
    public static NetworkGateway create() {
        NetworkGateway ng = new NetworkGateway();
        
        ng.runnable = new NetworkRunnable();
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
    
    private static final class NetworkRunnable implements Runnable {
        private final Bus bus;
        private final LinkedBlockingQueue<Object> queue;
        private final Selector selector;
        private int nextId = 0;

        public NetworkRunnable() {
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

                        if (channel instanceof SocketChannel) {
                            handleSelectForTcpChannel(key, (TcpNetworkEntry) entry);
                        } else if (channel instanceof DatagramChannel) {
                            handleSelectForUdpChannel(key, (UdpNetworkEntry) entry);
                        } else {
                            throw new IllegalStateException(); // should never happen
                        }

                        updateReadWriteSelectionKey(entry, (AbstractSelectableChannel) channel);
                    }

                    LinkedList<Object> msgs = new LinkedList<>();
                    queue.drainTo(msgs);

                    for (Object msg : msgs) {
                        processMessage(msg);
                    }
                }
            } catch (Exception e) {
                releaseResources();
                throw new RuntimeException(e);
            }
        }

        private void handleSelectForTcpChannel(SelectionKey selectionKey, TcpNetworkEntry entry) throws IOException {
            SocketChannel channel = (SocketChannel) entry.getChannel();
            Bus responseBus = entry.getResponseBus();

            if (selectionKey.isConnectable()) {
                int id = entry.getId();

                try {
                    // This block is sometimes called more than once for each connection -- we still call finishConnect but we also check to
                    // see if we're already connected before sending the CreateTcpSocketNetworkResponse msg
                    boolean alreadyConnected = channel.isConnected();
                    boolean connected = channel.finishConnect();
                    if (!alreadyConnected && connected) {
                        responseBus.send(new CreateTcpSocketNetworkResponse(id));
                    }
                } catch (IOException ioe) {
                    // socket failed to connect
                    Object errorResp = new ErrorNetworkResponse();
                    responseBus.send(errorResp);
                }
            }
            
            if (selectionKey.isReadable()) {
                buffer.clear();

                int readCount = channel.read(buffer);
                if (readCount == -1) {
                    // socket disconnected
                    Object errorResp = new ErrorNetworkResponse();
                    responseBus.send(errorResp);
                } else {
                    buffer.flip();

                    if (buffer.remaining() > 0) {
                        byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
                        Object readResp = new ReadTcpBlockNetworkResponse(bufferAsArray);
                        responseBus.send(readResp);
                    }
                }
            }
            
            if (selectionKey.isWritable()) {
                LinkedList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();

                // if OP_WRITE was set, WriteTcpBlockNetworkRequest is pending (we should have at least 1 outgoing buffer)
                int writeCount = 0;
                while (!outBuffers.isEmpty()) {
                    ByteBuffer outBuffer = outBuffers.getFirst();
                    
                    writeCount += channel.write(outBuffer);
                    if (outBuffer.remaining() > 0) {
                        break; // not everything was written, which means we can't send anymore data until we get another OP_WRITE, so leave
                    }
                    
                    outBuffers.removeFirst();
                    
                    Object writeResp = new WriteTcpBlockNetworkResponse(writeCount);
                    responseBus.send(writeResp);
                }
            }
        }

        private void handleSelectForUdpChannel(SelectionKey selectionKey, UdpNetworkEntry entry) throws IOException {
            DatagramChannel channel = (DatagramChannel) entry.getChannel();
            Bus responseBus = entry.getResponseBus();

            if (selectionKey.isReadable()) {
                buffer.clear();

                InetSocketAddress incomingSocketAddress = (InetSocketAddress) channel.receive(buffer);

                buffer.flip();

                if (buffer.remaining() > 0) {
                    byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
                    Object readResp = new ReadUdpBlockNetworkResponse(incomingSocketAddress, bufferAsArray);
                    responseBus.send(readResp);
                }
            }
            
            if (selectionKey.isWritable()) {
                LinkedList<AddressedByteBuffer> outBuffers = entry.getOutgoingBuffers();
                AddressedByteBuffer outBuffer = outBuffers.removeFirst(); // if OP_WRITE was set, we should have at least 1 outgoing buffer

                int writeCount = channel.send(outBuffer.getBuffer(), outBuffer.getSocketAddres());

                Object writeResp = new WriteUdpBlockNetworkResponse(writeCount);
                responseBus.send(writeResp);
            }
        }

        private void updateReadWriteSelectionKey(NetworkEntry<?> entry, AbstractSelectableChannel channel) throws ClosedChannelException {
            int newKey = SelectionKey.OP_READ;
            if (!entry.getOutgoingBuffers().isEmpty()) {
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
                    TcpNetworkEntry entry = new TcpNetworkEntry(id, channel, responseBus);
                    idMap.put(id, entry);
                    channelMap.put(channel, entry);
                    
                    updateReadWriteSelectionKey(entry, channel);

                    responseBus.send(new CreateUdpSocketNetworkResponse(id));
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

                    // no response -- we'll respond when connection succeeds
                } catch (RuntimeException re) {
                    responseBus.send(new ErrorNetworkResponse());
                }
            } else if (msg instanceof DestroySocketNetworkRequest) {
                DestroySocketNetworkRequest req = (DestroySocketNetworkRequest) msg;

                Bus responseBus = null;
                try {
                    int id = req.getId();
                    NetworkEntry<?> entry = idMap.get(id);

                    responseBus = entry.getResponseBus();
                    Channel channel = entry.getChannel();

                    idMap.remove(id);
                    channelMap.remove(channel);

                    channel.close();

                    responseBus.send(new DestroySocketNetworkResponse());
                } catch (RuntimeException re) {
                    if (responseBus != null) {
                        responseBus.send(new ErrorNetworkResponse());
                    }
                }
            } else if (msg instanceof WriteTcpBlockNetworkRequest) {
                WriteTcpBlockNetworkRequest req = (WriteTcpBlockNetworkRequest) msg;

                Bus responseBus = null;
                try {
                    int id = req.getId();
                    TcpNetworkEntry entry = (TcpNetworkEntry) idMap.get(id);

                    responseBus = entry.getResponseBus();

                    LinkedList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();
                    ByteBuffer writeBuffer = ByteBuffer.wrap(req.getData());
                    outBuffers.add(writeBuffer);
                    
                    AbstractSelectableChannel channel = (AbstractSelectableChannel) entry.getChannel();
                    updateReadWriteSelectionKey(entry, channel);
                } catch (RuntimeException re) {
                    if (responseBus != null) {
                        responseBus.send(new ErrorNetworkResponse());
                    }
                }
            } else if (msg instanceof WriteUdpBlockNetworkRequest) {
                WriteUdpBlockNetworkRequest req = (WriteUdpBlockNetworkRequest) msg;

                Bus responseBus = null;
                try {
                    int id = req.getId();
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
                        responseBus.send(new ErrorNetworkResponse());
                    }
                }
            } else if (msg instanceof KillNetworkRequest) {
                throw new RuntimeException("Kill requested");
            }
        }

        private void releaseResources() {
            for (Entry<Channel, NetworkEntry<?>> entry : channelMap.entrySet()) {
                try {
                    entry.getValue().getResponseBus().send(new ErrorNetworkResponse());
                    entry.getKey().close();
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
    }
}
