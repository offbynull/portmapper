package com.offbynull.portmapper.io.process;

import com.offbynull.portmapper.BasicBus;
import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.io.network.internalmessages.CloseNetworkRequest;
import com.offbynull.portmapper.io.process.internalmessages.CloseProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.CreateProcessRequest;
import com.offbynull.portmapper.io.process.internalmessages.CreateProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.ExitProcessNotification;
import com.offbynull.portmapper.io.process.internalmessages.IdentifiableErrorProcessResponse;
import com.offbynull.portmapper.io.process.internalmessages.KillProcessRequest;
import com.offbynull.portmapper.io.process.internalmessages.ReadProcessNotification;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class ProcessGatewayTest {

    private ProcessGateway fixture;
    private Bus fixtureBus;

    @Before
    public void before() {
        fixture = ProcessGateway.create();
        fixtureBus = fixture.getBus();
    }

    public void after() {
        fixtureBus.send(new KillProcessRequest());
    }

    @Test
    public void mustRunProcess() throws Exception {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        fixtureBus.send(new CreateProcessRequest(
                new BasicBus(queue),
                "java", "-version"));
        CreateProcessResponse resp1 = (CreateProcessResponse) queue.take();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            Object resp = queue.take();
            if (resp instanceof ReadProcessNotification) {
                ReadProcessNotification readResp = (ReadProcessNotification) resp;
                byte[] data = readResp.getData();
                baos.write(data);
            } else if (resp instanceof ExitProcessNotification) {
                assertEquals(0, ((ExitProcessNotification) resp).getExitCode().intValue());
                break;
            }
        }

        assertEquals("java version \"1.7.0_80\"\r\n"
                + "Java(TM) SE Runtime Environment (build 1.7.0_80-b15)\r\n"
                + "Java HotSpot(TM) 64-Bit Server VM (build 24.80-b11, mixed mode)\r\n",
                new String(baos.toByteArray(), Charset.forName("US-ASCII")));
    }

}
