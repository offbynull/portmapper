package com.offbynull.portmapper.testtools;

import com.offbynull.portmapper.helpers.ByteBufferUtils;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public final class TcpServerEmulator implements Closeable {

    private ServerSocket serverSocket;
    private Map<ByteBuffer, ByteBuffer> requestResponseMap;

    private TcpServerEmulator(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        requestResponseMap = Collections.synchronizedMap(new HashMap<ByteBuffer, ByteBuffer>());
    }

    public void addMapping(ByteBuffer request, ByteBuffer response) {
        requestResponseMap.put(
                ByteBufferUtils.copyContents(request).asReadOnlyBuffer(),
                ByteBufferUtils.copyContents(response).asReadOnlyBuffer());
    }

    public static TcpServerEmulator create(int port) throws IOException {
        final TcpServerEmulator helper = new TcpServerEmulator(port);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        
                        byte[] data = new byte[1024];
                        ByteArrayOutputStream bufferOs = new ByteArrayOutputStream();
                        try (Socket clientSocket = helper.serverSocket.accept();
                                InputStream is = clientSocket.getInputStream();
                                OutputStream os = clientSocket.getOutputStream();) {
                            int readCount = is.read(data);
                            bufferOs.write(data, 0, readCount);
                            ByteBuffer request = ByteBuffer.wrap(bufferOs.toByteArray());
                            
                            ByteBuffer response = helper.requestResponseMap.get(request);
                            if (response != null) {
                                Thread.sleep(1000L); // sleep for 1 seconds before responding
                                IOUtils.write(ByteBufferUtils.copyContentsToArray(response, false), os);
                            }
                            
                            os.flush();
                            
                            Thread.sleep(1000L); // sleep for 2 seconds before forcing a close
                            os.close();
                        }
                    }
                } catch (IOException ioe) {
                    // do nothing
                } catch (InterruptedException ie) {
                    // do nothing
                }
            }
        });

        thread.start();
        return helper;
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
