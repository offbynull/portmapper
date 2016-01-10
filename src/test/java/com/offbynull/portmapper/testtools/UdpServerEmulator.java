package com.offbynull.portmapper.testtools;

import com.offbynull.portmapper.helpers.ByteBufferUtils;
import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UdpServerEmulator implements Closeable {
    private DatagramSocket socket;
    private Map<ByteBuffer, ByteBuffer> requestResponseMap;
    
    private UdpServerEmulator(int port) throws IOException {
        socket = new DatagramSocket(port);
        requestResponseMap = Collections.synchronizedMap(new HashMap<ByteBuffer, ByteBuffer>());
    }
    
    public void addMapping(ByteBuffer request, ByteBuffer response) {
        requestResponseMap.put(
                ByteBufferUtils.copyContents(request).asReadOnlyBuffer(),
                ByteBufferUtils.copyContents(response).asReadOnlyBuffer());
    }
    
    public static UdpServerEmulator create(int port) throws IOException {
        final UdpServerEmulator helper = new UdpServerEmulator(port);
        
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] buffer = new byte[65535];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        helper.socket.receive(packet);
                        
                        ByteBuffer request = ByteBuffer.wrap(buffer);
                        request.limit(packet.getLength());
                        
                        ByteBuffer response = helper.requestResponseMap.get(request);
                        
                        if (response != null) {
                            Thread.sleep(1000L);
                            response = response.asReadOnlyBuffer();
                            int rem = response.remaining();
                            response.get(buffer, 0, rem);
                            packet.setLength(rem);
                            helper.socket.send(packet);
                        }
                    }
                } catch (IOException ioe) {
                    // do nothing
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
        });
        
        thread.start();
        return helper;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
