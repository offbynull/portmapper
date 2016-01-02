package com.offbynull.portmapper.io;

import com.offbynull.portmapper.common.BasicBus;
import com.offbynull.portmapper.common.Bus;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkRequest;
import com.offbynull.portmapper.io.messages.CreateTcpSocketNetworkResponse;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkRequest;
import com.offbynull.portmapper.io.messages.DestroySocketNetworkResponse;
import com.offbynull.portmapper.io.messages.KillNetworkRequest;
import com.offbynull.portmapper.io.messages.ReadTcpBlockNetworkResponse;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkRequest;
import com.offbynull.portmapper.io.messages.WriteTcpBlockNetworkResponse;
import com.offbynull.portmapper.testtools.TcpServerEmulator;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
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
    }

}
