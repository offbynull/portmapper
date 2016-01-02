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

import com.offbynull.portmapper.common.*;
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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
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
public final class NetworkGateway implements Runnable {

    private final Bus bus;
    private final LinkedBlockingQueue<Object> queue;
    private final Selector selector;
    private int nextId = 0;

    /**
     * Constructs a {@link NetworkGateway} object.
     *
     * @throws IllegalStateException if could not create NIO selector
     */
    public NetworkGateway() {
        try {
            selector = Selector.open();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        queue = new LinkedBlockingQueue<>();
        bus = new NetworkBus(selector, queue);
    }

    public Bus getBus() {
        return bus;
    }

    private Map<Integer, NetworkEntry> idMap = new HashMap<>();
    private Map<Channel, NetworkEntry> channelMap = new HashMap<>();
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

                    NetworkEntry entry = channelMap.get(channel);
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

            responseBus.send(new CreateTcpSocketNetworkResponse(id));
        } else if (selectionKey.isReadable()) {
            buffer.clear();

            int readCount = channel.read(buffer);
            if (readCount == -1) {
                // socket disconnected
                Object errorResp = new ErrorNetworkResponse();
                responseBus.send(errorResp);
                return;
            }

            buffer.flip();

            byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
            Object readResp = new ReadTcpBlockNetworkResponse(bufferAsArray);
            responseBus.send(readResp);
        } else if (selectionKey.isWritable()) {
            LinkedList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();
            ByteBuffer outBuffer = outBuffers.getFirst(); // if OP_WRITE was set, we should have at least 1 outgoing buffer

            int writeCount = channel.write(outBuffer);
            if (outBuffer.position() == outBuffer.limit()) {
                outBuffers.removeFirst();
            }

            Object writeResp = new WriteTcpBlockNetworkResponse(writeCount);
            responseBus.send(writeResp);
        }
    }

    private void handleSelectForUdpChannel(SelectionKey selectionKey, UdpNetworkEntry entry) throws IOException {
        DatagramChannel channel = (DatagramChannel) entry.getChannel();
        Bus responseBus = entry.getResponseBus();

        if (selectionKey.isReadable()) {
            buffer.clear();

            InetSocketAddress incomingSocketAddress = (InetSocketAddress) channel.receive(buffer);

            buffer.flip();

            byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
            Object readResp = new ReadUdpBlockNetworkResponse(incomingSocketAddress, bufferAsArray);
            responseBus.send(readResp);
        } else if (selectionKey.isWritable()) {
            LinkedList<AddressedByteBuffer> outBuffers = entry.getOutgoingBuffers();
            AddressedByteBuffer outBuffer = outBuffers.removeFirst(); // if OP_WRITE was set, we should have at least 1 outgoing buffer

            int writeCount = channel.send(outBuffer.getBuffer(), outBuffer.getSocketAddres());

            Object writeResp = new WriteTcpBlockNetworkResponse(writeCount);
            responseBus.send(writeResp);
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
                channel.register(selector, SelectionKey.OP_READ);

                int id = nextId++;
                TcpNetworkEntry entry = new TcpNetworkEntry(id, channel, responseBus);
                idMap.put(id, entry);
                channelMap.put(channel, entry);

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
                channel.register(selector, SelectionKey.OP_CONNECT);

                int id = nextId++;
                TcpNetworkEntry entry = new TcpNetworkEntry(id, channel, responseBus);
                idMap.put(id, entry);
                channelMap.put(channel, entry);

                // no response -- we'll respond when connection succeeds
            } catch (RuntimeException re) {
                responseBus.send(new ErrorNetworkResponse());
            }
        } else if (msg instanceof DestroySocketNetworkRequest) {
            DestroySocketNetworkRequest req = (DestroySocketNetworkRequest) msg;

            Bus responseBus = null;
            try {
                int id = req.getId();
                NetworkEntry entry = idMap.get(id);

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

                AbstractSelectableChannel channel = (AbstractSelectableChannel) entry.getChannel();
                channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

                LinkedList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();
                ByteBuffer writeBuffer = ByteBuffer.wrap(req.getData());
                outBuffers.add(writeBuffer);
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

                AbstractSelectableChannel channel = (AbstractSelectableChannel) entry.getChannel();
                channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

                LinkedList<AddressedByteBuffer> outBuffers = entry.getOutgoingBuffers();
                ByteBuffer writeBuffer = ByteBuffer.wrap(req.getData());
                InetSocketAddress writeAddress = req.getOutgoingSocketAddress();
                outBuffers.add(new AddressedByteBuffer(writeBuffer, writeAddress));
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
        for (Entry<Channel, NetworkEntry> entry : channelMap.entrySet()) {
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
