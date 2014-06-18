package com.offbynull.portmapper.testtools;

import com.offbynull.portmapper.common.ByteBufferUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import org.apache.commons.io.IOUtils;

public final class UdpTestUtils {
    private UdpTestUtils() {
    }
    
    public static void send(InetAddress dstAddr, int dstPort, ByteBuffer buffer) throws IOException {
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket(0);
            ByteBuffer bufferCopy = ByteBufferUtils.copyContents(buffer);
            DatagramPacket request = new DatagramPacket(bufferCopy.array(), bufferCopy.limit(), dstAddr, dstPort);
            socket.send(request);
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }
    
    public static void sendMulticast(InetAddress dstAddr, int dstPort, ByteBuffer buffer) throws IOException {
        MulticastSocket socket = null;
        
        try {
            socket = new MulticastSocket();
            ByteBuffer bufferCopy = ByteBufferUtils.copyContents(buffer);
            DatagramPacket request = new DatagramPacket(bufferCopy.array(), bufferCopy.limit(), dstAddr, dstPort);
            socket.send(request, (byte) 10);
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }
}
