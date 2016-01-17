package com.offbynull.portmapper.gateways.network;

import com.offbynull.portmapper.gateway.BasicBus;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateTcpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateTcpNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateUdpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateUdpNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.CloseNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CloseNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.ConnectedTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.KillNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadUdpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteEmptyTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteEmptyUdpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteTcpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteTcpNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteUdpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteUdpNetworkResponse;
import com.offbynull.portmapper.testtools.TcpServerEmulator;
import com.offbynull.portmapper.testtools.UdpServerEmulator;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
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

    @After
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

            LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
            Bus responseBus = new BasicBus(queue);
            
            fixtureBus.send(new GetNextIdNetworkRequest(responseBus));
            GetNextIdNetworkResponse nextIdResp = (GetNextIdNetworkResponse) queue.take();
            
            int id = nextIdResp.getId();
            
            fixtureBus.send(new CreateTcpNetworkRequest(
                    id,
                    responseBus,
                    InetAddress.getByName("0.0.0.0"),
                    InetAddress.getLoopbackAddress(),
                    12345));
            CreateTcpNetworkResponse createdResp = (CreateTcpNetworkResponse) queue.take();
            ConnectedTcpNetworkNotification connectedResp = (ConnectedTcpNetworkNotification) queue.take();
            
            WriteEmptyTcpNetworkNotification writeReady1 = (WriteEmptyTcpNetworkNotification) queue.take();
            fixtureBus.send(new WriteTcpNetworkRequest(id, "hello".getBytes("UTF-8")));
            int remainingWriteBytes = 5;
            while (remainingWriteBytes > 0) {
                WriteTcpNetworkResponse writeResp = (WriteTcpNetworkResponse) queue.take();
                remainingWriteBytes -= writeResp.getAmountWritten();
            }
            
            WriteEmptyTcpNetworkNotification writeReady2 = (WriteEmptyTcpNetworkNotification) queue.take();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int remainingReadBytes = 7;
            while (remainingReadBytes > 0) {
                ReadTcpNetworkNotification readResp = (ReadTcpNetworkNotification) queue.take();
                byte[] data = readResp.getData();
                baos.write(data);
                remainingReadBytes -= data.length;
            }
            assertEquals("goodbye", new String(baos.toByteArray(), Charset.forName("UTF-8")));


            fixtureBus.send(new CloseNetworkRequest(id));
            CloseNetworkResponse destoryResp = (CloseNetworkResponse) queue.take();
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

            LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
            Bus responseBus = new BasicBus(queue);
            
            fixtureBus.send(new GetNextIdNetworkRequest(responseBus));
            GetNextIdNetworkResponse nextIdResp = (GetNextIdNetworkResponse) queue.take();

            int id = nextIdResp.getId();
            
            fixtureBus.send(new CreateUdpNetworkRequest(
                    id,
                    responseBus,
                    InetAddress.getByName("0.0.0.0")));
            CreateUdpNetworkResponse resp1 = (CreateUdpNetworkResponse) queue.take();


            WriteEmptyUdpNetworkNotification writeReadyResp = (WriteEmptyUdpNetworkNotification) queue.take();
            
            fixtureBus.send(new WriteUdpNetworkRequest(id, new InetSocketAddress("127.0.0.1", 12345), "hello".getBytes("UTF-8")));
            WriteUdpNetworkResponse writeResp = (WriteUdpNetworkResponse) queue.take();


            WriteEmptyUdpNetworkNotification writeReadyResp2 = (WriteEmptyUdpNetworkNotification) queue.take();
            
            ReadUdpNetworkNotification readResp = (ReadUdpNetworkNotification) queue.take();
            assertEquals("goodbye", new String(readResp.getData(), Charset.forName("UTF-8")));

            
            fixtureBus.send(new CloseNetworkRequest(id));
            CloseNetworkResponse destoryResp = (CloseNetworkResponse) queue.take();
        } finally {
            emulator.close();
        }
    }

}
