package com.offbynull.portmapper.gateways.process;

import com.offbynull.portmapper.BasicBus;
import com.offbynull.portmapper.Bus;
import com.offbynull.portmapper.gateways.process.internalmessages.CreateProcessRequest;
import com.offbynull.portmapper.gateways.process.internalmessages.CreateProcessResponse;
import com.offbynull.portmapper.gateways.process.internalmessages.ExitProcessNotification;
import com.offbynull.portmapper.gateways.process.internalmessages.GetNextIdProcessRequest;
import com.offbynull.portmapper.gateways.process.internalmessages.KillProcessRequest;
import com.offbynull.portmapper.gateways.process.internalmessages.ReadProcessNotification;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Not a stable test -- depends on OS and environment settings")
public class ProcessGatewayTest {

    private ProcessGateway fixture;
    private Bus fixtureBus;

    @Before
    public void before() {
        fixture = ProcessGateway.create();
        fixtureBus = fixture.getBus();
    }

    @After
    public void after() {
        fixtureBus.send(new KillProcessRequest());
    }

    @Test
    public void mustRunProcess() throws Exception {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        Bus responseBus = new BasicBus(queue);

        fixtureBus.send(new GetNextIdProcessRequest(responseBus));
        CreateProcessResponse nextIdResp = (CreateProcessResponse) queue.take();
        
        int id = nextIdResp.getId();
                
        fixtureBus.send(new CreateProcessRequest(
                id,
                responseBus,
                "java", "-version"));
        CreateProcessResponse createResp = (CreateProcessResponse) queue.take();

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
