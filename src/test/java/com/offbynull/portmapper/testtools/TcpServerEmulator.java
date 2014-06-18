package com.offbynull.portmapper.testtools;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

public final class TcpServerEmulator implements Closeable {

    public ServerSocket serverSocket;
    public Map<Pattern, String> requestResponseMap;

    private TcpServerEmulator(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        requestResponseMap = Collections.synchronizedMap(new HashMap<Pattern, String>());
    }

    public void addMapping(Pattern request, String response) {
        requestResponseMap.put(request, response);
    }

    public static TcpServerEmulator create(int port) throws IOException {
        final TcpServerEmulator helper = new TcpServerEmulator(port);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        
                        try (Socket clientSocket = helper.serverSocket.accept();
                                InputStream is = clientSocket.getInputStream();
                                OutputStream os = clientSocket.getOutputStream();) {
                            String msg = IOUtils.toString(is);

                            for (Entry<Pattern, String> e : helper.requestResponseMap.entrySet()) {
                                Matcher m = e.getKey().matcher(msg);
                                if (m.matches()) {
                                    IOUtils.write(e.getValue(), os);
                                }
                            }
                            
                            os.flush();
                        }
                    }
                } catch (IOException ioe) {
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
