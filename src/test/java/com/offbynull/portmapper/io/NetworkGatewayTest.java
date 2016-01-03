package com.offbynull.portmapper.io;

import com.offbynull.portmapper.common.BasicBus;
import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateUdpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkRequest;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkResponse;
import com.offbynull.portmapper.io.messages.KillNetworkRequest;
import com.offbynull.portmapper.io.messages.ReadTcpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.ReadUdpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteUdpBlockNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteUdpBlockNetworkResponse;
import com.offbynull.portmapper.testtools.TcpServerEmulator;
import com.offbynull.portmapper.testtools.UdpServerEmulator;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class NetworkGatewayTest {

    private NetworkGateway fixture;
    private Bus fixtureBus;

    @Before
    public void before() {
        fixture = NetworkGateway.create();
        fixtureBus = fixture.getBus();
    }

    public void after() {
        fixtureBus.send(new KillNetworkRequest());
    }

    @Test
    public void mustPerformTcpConnection() throws Exception {
        TcpServerEmulator emulator = TcpServerEmulator.create(12345);
        try {
            emulator.addMapping(
                    ByteBuffer.wrap("hello".getBytes("UTF-8")),
                    ByteBuffer.wrap("goodbye".getBytes("UTF-8")));

            int id;

            LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
            fixtureBus.send(new CreateTcpSocketNetworkRequest(
                    new BasicBus(queue),
                    InetAddress.getByName("0.0.0.0"),
                    InetAddress.getLoopbackAddress(),
                    12345));
            CreateTcpSocketNetworkResponse resp1 = (CreateTcpSocketNetworkResponse) queue.take();
            id = resp1.getId();


            fixtureBus.send(new WriteTcpBlockNetworkRequest(id, "hello".getBytes("UTF-8")));
            int remainingWriteBytes = 5;
            while (remainingWriteBytes > 0) {
                WriteTcpBlockNetworkResponse writeResp = (WriteTcpBlockNetworkResponse) queue.take();
                remainingWriteBytes -= writeResp.getAmountWritten();
            }


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int remainingReadBytes = 7;
            while (remainingReadBytes > 0) {
                ReadTcpBlockNetworkResponse readResp = (ReadTcpBlockNetworkResponse) queue.take();
                byte[] data = readResp.getData();
                baos.write(data);
                remainingReadBytes -= data.length;
            }
            assertEquals("goodbye", new String(baos.toByteArray(), Charset.forName("UTF-8")));


            fixtureBus.send(new DestroySocketNetworkRequest(id));
            DestroySocketNetworkResponse destoryResp = (DestroySocketNetworkResponse) queue.take();
        } finally {
            emulator.close();
        }
    }
    
    @Test
    public void mustPerformUdpConnection() throws Exception {
        UdpServerEmulator emulator = UdpServerEmulator.create(12345);
        try {
            emulator.addMapping(
                    ByteBuffer.wrap("hello".getBytes("UTF-8")),
                    ByteBuffer.wrap("goodbye".getBytes("UTF-8")));

            int id;

            LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
            fixtureBus.send(new CreateUdpSocketNetworkRequest(
                    new BasicBus(queue),
                    InetAddress.getByName("0.0.0.0")));
            CreateUdpSocketNetworkResponse resp1 = (CreateUdpSocketNetworkResponse) queue.take();
            id = resp1.getId();


            
            fixtureBus.send(new WriteUdpBlockNetworkRequest(id, new InetSocketAddress("127.0.0.1", 12345), "hello".getBytes("UTF-8")));
            WriteUdpBlockNetworkResponse writeResp = (WriteUdpBlockNetworkResponse) queue.take();


            
            ReadUdpBlockNetworkResponse readResp = (ReadUdpBlockNetworkResponse) queue.take();
            assertEquals("goodbye", new String(readResp.getData(), Charset.forName("UTF-8")));
            


            fixtureBus.send(new DestroySocketNetworkRequest(id));
            DestroySocketNetworkResponse destoryResp = (DestroySocketNetworkResponse) queue.take();
        } finally {
            emulator.close();
        }
    }

}
