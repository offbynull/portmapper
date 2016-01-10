package com.offbynull.portmapper.io;

import com.offbynull.portmapper.common.BasicBus;
import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.io.internalmessages.CreateTcpIoRequest;
import com.offbynull.portmapper.io.internalmessages.CreateTcpIoResponse;
import com.offbynull.portmapper.io.internalmessages.CreateUdpIoRequest;
import com.offbynull.portmapper.io.internalmessages.CreateUdpIoResponse;
import com.offbynull.portmapper.io.internalmessages.DestroySocketIoRequest;
import com.offbynull.portmapper.io.internalmessages.DestroySocketIoResponse;
import com.offbynull.portmapper.io.internalmessages.KillIoRequest;
import com.offbynull.portmapper.io.internalmessages.ReadTcpIoNotification;
import com.offbynull.portmapper.io.internalmessages.ReadUdpIoNotification;
import com.offbynull.portmapper.io.internalmessages.WriteEmptyTcpIoNotification;
import com.offbynull.portmapper.io.internalmessages.WriteEmptyUdpIoNotification;
import com.offbynull.portmapper.io.internalmessages.WriteTcpIoRequest;
import com.offbynull.portmapper.io.internalmessages.WriteTcpIoResponse;
import com.offbynull.portmapper.io.internalmessages.WriteUdpIoRequest;
import com.offbynull.portmapper.io.internalmessages.WriteUdpIoResponse;
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
        fixtureBus.send(new KillIoRequest());
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
            fixtureBus.send(new CreateTcpIoRequest(
                    new BasicBus(queue),
                    InetAddress.getByName("0.0.0.0"),
                    InetAddress.getLoopbackAddress(),
                    12345));
            CreateTcpIoResponse resp1 = (CreateTcpIoResponse) queue.take();
            id = resp1.getId();


            
            fixtureBus.send(new WriteTcpIoRequest(id, "hello".getBytes("UTF-8")));
            int remainingWriteBytes = 5;
            while (remainingWriteBytes > 0) {
                WriteEmptyTcpIoNotification writeReady = (WriteEmptyTcpIoNotification) queue.take();
                WriteTcpIoResponse writeResp = (WriteTcpIoResponse) queue.take();
                remainingWriteBytes -= writeResp.getAmountWritten();
            }
            
            WriteEmptyTcpIoNotification writeReady = (WriteEmptyTcpIoNotification) queue.take();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int remainingReadBytes = 7;
            while (remainingReadBytes > 0) {
                ReadTcpIoNotification readResp = (ReadTcpIoNotification) queue.take();
                byte[] data = readResp.getData();
                baos.write(data);
                remainingReadBytes -= data.length;
            }
            assertEquals("goodbye", new String(baos.toByteArray(), Charset.forName("UTF-8")));


            fixtureBus.send(new DestroySocketIoRequest(id));
            DestroySocketIoResponse destoryResp = (DestroySocketIoResponse) queue.take();
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
            fixtureBus.send(new CreateUdpIoRequest(
                    new BasicBus(queue),
                    InetAddress.getByName("0.0.0.0")));
            CreateUdpIoResponse resp1 = (CreateUdpIoResponse) queue.take();
            id = resp1.getId();


            WriteEmptyUdpIoNotification writeReadyResp = (WriteEmptyUdpIoNotification) queue.take();
            
            fixtureBus.send(new WriteUdpIoRequest(id, new InetSocketAddress("127.0.0.1", 12345), "hello".getBytes("UTF-8")));
            WriteUdpIoResponse writeResp = (WriteUdpIoResponse) queue.take();


            WriteEmptyUdpIoNotification writeReadyResp2 = (WriteEmptyUdpIoNotification) queue.take();
            
            ReadUdpIoNotification readResp = (ReadUdpIoNotification) queue.take();
            assertEquals("goodbye", new String(readResp.getData(), Charset.forName("UTF-8")));

            
            fixtureBus.send(new DestroySocketIoRequest(id));
            DestroySocketIoResponse destoryResp = (DestroySocketIoResponse) queue.take();
        } finally {
            emulator.close();
        }
    }

}
