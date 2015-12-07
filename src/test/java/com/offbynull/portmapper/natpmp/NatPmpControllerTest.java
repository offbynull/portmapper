package com.offbynull.portmapper.natpmp;

import com.offbynull.portmapper.natpmp.messages.ExternalAddressNatPmpResponse;
import com.offbynull.portmapper.natpmp.messages.TcpMappingNatPmpResponse;
import com.offbynull.portmapper.natpmp.messages.UdpMappingNatPmpResponse;
import com.offbynull.portmapper.common.ResponseException;
import com.offbynull.portmapper.testtools.UdpServerEmulator;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NatPmpControllerTest {

    private UdpServerEmulator helper;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Throwable {
        helper = UdpServerEmulator.create(5351);
    }

    @After
    public void tearDown() throws Throwable {
        helper.close();
        Thread.sleep(1000L); // give OS time to clean up
    }

    @Test
    public void exposedAddressTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 0}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128, 0, 0, 0, 0, 0, 0, 127, 0, 0, 1}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        ExternalAddressNatPmpResponse res = controller.requestExternalAddress(4);
        InetAddress address = res.getAddress();
        
        Assert.assertEquals(InetAddress.getLoopbackAddress(), address);
    }

    @Test
    public void failedExposedAddressTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 0}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128, 0, 1, 0, 0, 0, 0, 127, 0, 0, 1}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        ExternalAddressNatPmpResponse res = controller.requestExternalAddress(4);
        Assert.assertEquals(1, res.getResultCode());
    }

    
    @Test(expected = ResponseException.class)
    public void truncatedExposedAddressTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 0}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128, 0, 0}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        controller.requestExternalAddress(4);
    }
    
    @Test
    public void openUdpPortTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 1, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 10}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        UdpMappingNatPmpResponse res = controller.requestUdpMappingOperation(4, 1, 2, 9);

        Assert.assertEquals(3, res.getExternalPort());
        Assert.assertEquals(1, res.getInternalPort());
        Assert.assertEquals(10, res.getLifetime());
    }
    
    @Test
    public void openTcpPortTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 2, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 10}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        TcpMappingNatPmpResponse res = controller.requestTcpMappingOperation(4, 1, 2, 9);

        Assert.assertEquals(3, res.getExternalPort());
        Assert.assertEquals(1, res.getInternalPort());
        Assert.assertEquals(10, res.getLifetime());
    }
    
    @Test(expected = ResponseException.class)
    public void openUdpPortWrongPortTypeResponseTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 1, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 10}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        UdpMappingNatPmpResponse res = controller.requestUdpMappingOperation(4, 1, 2, 9);

        Assert.assertEquals(3, res.getExternalPort());
        Assert.assertEquals(1, res.getInternalPort());
        Assert.assertEquals(10, res.getLifetime());
    }
    
    @Test(expected = ResponseException.class)
    public void openTcpPortWrongPortTypeResponseTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 2, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 10}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        TcpMappingNatPmpResponse res = controller.requestTcpMappingOperation(4, 1, 2, 9);

        Assert.assertEquals(3, res.getExternalPort());
        Assert.assertEquals(1, res.getInternalPort());
        Assert.assertEquals(10, res.getLifetime());
    }

    @Test
    public void failedOpenUdpPortTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 1, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 10}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        UdpMappingNatPmpResponse res = controller.requestUdpMappingOperation(4, 1, 2, 9);
        Assert.assertEquals(1, res.getResultCode());
    }

    @Test
    public void failedOpenTcpPortTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 2, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 2, 0, 1, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0, 0, 10}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        TcpMappingNatPmpResponse res = controller.requestTcpMappingOperation(4, 1, 2, 9);
        Assert.assertEquals(1, res.getResultCode());
    }

    
    @Test(expected = ResponseException.class)
    public void truncatedOpenUdpPortTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 1, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 1, 0, 0}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        controller.requestUdpMappingOperation(4, 1, 2, 9);
    }

    @Test(expected = ResponseException.class)
    public void truncatedOpenTcpPortTest() throws Throwable {
        helper.addMapping(ByteBuffer.wrap(new byte[] {0, 2, 0, 0, 0, 1, 0, 2, 0, 0, 0, 9}),
                ByteBuffer.wrap(new byte[] {0, (byte) 128 + 2, 0, 0}));
        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.1"), null);
        
        controller.requestTcpMappingOperation(4, 1, 2, 9);
    }

    @Test(expected = ResponseException.class)
    public void timedOutTest() throws Throwable {        
        NatPmpController controller = new NatPmpController(InetAddress.getByName("127.0.0.2"), null);
        
        controller.requestTcpMappingOperation(4, 1, 2, 9);
    }
}
