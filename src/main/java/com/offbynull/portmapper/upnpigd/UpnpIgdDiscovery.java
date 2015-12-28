/*
 * Copyright (c) 2013-2014, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.portmapper.upnpigd;

import com.offbynull.portmapper.common.ByteBufferUtils;
import com.offbynull.portmapper.common.NetworkUtils;
import com.offbynull.portmapper.common.TextUtils;
import com.offbynull.portmapper.common.UdpCommunicator;
import com.offbynull.portmapper.common.UdpCommunicatorListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility class used to discover UPNP-IGD-enabled routers.
 * @author Kasra Faghihi
 */
public final class UpnpIgdDiscovery {

    private static final int MAX_WAIT = 3;
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?i)LOCATION:\\s*(.*)\\s*");
    private static final Pattern SERVER_PATTERN = Pattern.compile("(?i)SERVER:\\s*(.*)\\s*");
    private static final String IPV4_SEARCH_QUERY = "M-SEARCH * HTTP/1.1\r\n"
            + "HOST: 239.255.255.250:1900\r\n"
            + "MAN: ssdp:discover\r\n"
            + "MX: " + MAX_WAIT + "\r\n" // server should send response in rand(0, MX)
            + "ST: ssdp:all\r\n"
            + "\r\n";
    private static final String IPV6_SEARCH_QUERY = "M-SEARCH * HTTP/1.1\r\n"
            + "HOST: [FF02::C]:1900\r\n"
            + "MAN: ssdp:discover\r\n"
            + "MX: " + MAX_WAIT + "\r\n" // server should send response in rand(0, MX)
            + "ST: ssdp:all\r\n"
            + "\r\n";
    
    private static final Set<String> EXPECTED_ACTION_NAMES; // unmodifiable
    static {
        Set<String> set = new HashSet<>();
        
        set.add("GetExternalIPAddress");
        set.add("GetSpecificPortMappingEntry");
        set.add("DeletePortMapping");
        set.add("AddPortMapping");
        
        EXPECTED_ACTION_NAMES = Collections.unmodifiableSet(set);
    }

    private UpnpIgdDiscovery() {
        // do nothing
    }

    /**
     * Discovers UPNP-IGD routers.
     * @return a collection of UPNP-IGD routers that were discovered
     * @throws InterruptedException if interrupted
     * @throws IOException if IO error occurs
     */
    public static Set<UpnpIgdService> discover() throws IOException, InterruptedException {
        Set<UpnpIgdDevice> devices = new HashSet<>();
        devices.addAll(findIpv4Devices());
        devices.addAll(findIpv6Devices());
        
        Map<UpnpIgdDevice, byte[]> rootXmls = getRootXmlForEachDevice(devices);
        Set<UpnpIgdServiceReference> services = parseServiceReferences(rootXmls);
        Map<UpnpIgdServiceReference, byte[]> scpds = getServiceDescriptions(services);
        Set<UpnpIgdService> serviceDescs = parseServiceDescriptions(scpds);
        
        return serviceDescs;
    }
    
    private static Set<UpnpIgdDevice> findIpv4Devices() throws IOException, InterruptedException {
        InetSocketAddress multicastSocketAddress;
        try {
            multicastSocketAddress = new InetSocketAddress(InetAddress.getByName("239.255.255.250"), 1900); // NOPMD
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe);
        }

        Set<InetAddress> localIpv4Addresses = NetworkUtils.getAllLocalIpv4Addresses();
        return scanForDevices(multicastSocketAddress, localIpv4Addresses, IPV4_SEARCH_QUERY);
    }

    private static Set<UpnpIgdDevice> findIpv6Devices() throws IOException, InterruptedException {
        InetSocketAddress multicastSocketAddress;
        try {
            multicastSocketAddress = new InetSocketAddress(InetAddress.getByName("ff02::c"), 1900); // NOPMD
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(uhe);
        }

        Set<InetAddress> localIpv6Addresses = NetworkUtils.getAllLocalIpv6Addresses();
        return scanForDevices(multicastSocketAddress, localIpv6Addresses, IPV6_SEARCH_QUERY);
    }

    private static Set<UpnpIgdDevice> scanForDevices(InetSocketAddress multicastSocketAddress, Set<InetAddress> localAddresses,
            String searchQuery) throws IOException, InterruptedException {

        final Set<UpnpIgdDevice> ret = Collections.synchronizedSet(new HashSet<UpnpIgdDevice>());
        final Map<Channel, InetAddress> bindMap = Collections.synchronizedMap(new HashMap<Channel, InetAddress>());

        UdpCommunicatorListener listener = new UdpCommunicatorListener() {

            @Override
            public void incomingPacket(InetSocketAddress sourceAddress, DatagramChannel channel, ByteBuffer packet) {
                byte[] inPacket = ByteBufferUtils.copyContentsToArray(packet);

                String inStr;
                try {
                    inStr = new String(inPacket, 0, inPacket.length, "US-ASCII");
                } catch (UnsupportedEncodingException uee) {
                    return;
                }

                Matcher matcher;

                URI url;
                if ((matcher = LOCATION_PATTERN.matcher(inStr)).find()) {
                    String urlStr = matcher.group(1);
                    try {
                        url = new URI(urlStr);
                    } catch (URISyntaxException urise) {
                        return;
                    }
                } else {
                    return;
                }

                String name = null;
                if ((matcher = SERVER_PATTERN.matcher(inStr)).find()) {
                    name = matcher.group(1);
                }
                
                InetAddress localAddress = bindMap.get(channel);

                UpnpIgdDevice device = new UpnpIgdDevice(localAddress, sourceAddress.getAddress(), name, url);
                ret.add(device);
            }
        };

        UdpCommunicator comm = null;
        try {
            List<DatagramChannel> channels = new ArrayList<>();

            for (InetAddress localAddr : localAddresses) {
                DatagramChannel channel = DatagramChannel.open();
                channel.configureBlocking(false);
                channel.bind(new InetSocketAddress(localAddr, 0));
                channels.add(channel);
                
                bindMap.put(channel, localAddr);
            }

            comm = new UdpCommunicator(channels);
            comm.startAsync().awaitRunning();
            comm.addListener(listener);

            ByteBuffer searchQueryBuffer = ByteBuffer.wrap(searchQuery.getBytes("US-ASCII")).asReadOnlyBuffer();
            for (int i = 0; i < 3; i++) {
                for (DatagramChannel channel : channels) {
                    comm.send(channel, multicastSocketAddress, searchQueryBuffer.asReadOnlyBuffer());
                }

                Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT + 1));
            }

            return new HashSet<>(ret);
        } finally {
            if (comm != null) {
                try {
                    comm.stopAsync().awaitTerminated(); // this stop should handle closing all the datagram channels
                } catch (IllegalStateException ise) { // NOPMD
                    // do nothing
                }
            }
        }
    }
    
    private static Map<UpnpIgdDevice, byte[]> getRootXmlForEachDevice(Set<UpnpIgdDevice> devices) throws InterruptedException {
        Map<UpnpIgdDevice, byte[]> serviceRoots = new HashMap();
        
        ExecutorService executorService = null;
        try {
            int maximumPoolSize = (int) ((double) Runtime.getRuntime().availableProcessors() / (1.0 - 0.95));
            executorService = new ThreadPoolExecutor(0, maximumPoolSize, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
            
            List<HttpRequestCallable<UpnpIgdDevice>> tasks = new LinkedList<>();
            for (UpnpIgdDevice device : devices)  {
                tasks.add(new HttpRequestCallable<>(device.getUrl(), device));
            }
            
            List<Future<Pair<UpnpIgdDevice, byte[]>>> results = executorService.invokeAll(tasks);
            
            for (Future<Pair<UpnpIgdDevice, byte[]>> result : results) {
                try {
                    Pair<UpnpIgdDevice, byte[]> data = result.get();
                    serviceRoots.put(data.getKey(), data.getValue());
                } catch (InterruptedException | ExecutionException | CancellationException e) { // NOPMD
                    // do nothing, skip
                }
            }
        } finally {
            if (executorService != null) {
                executorService.shutdownNow();
            }
        }
        
        return serviceRoots;
    }

    private static Set<UpnpIgdServiceReference> parseServiceReferences(Map<UpnpIgdDevice, byte[]> rootBuffers) {
        Set<UpnpIgdServiceReference> services = new HashSet<>();
        for (Entry<UpnpIgdDevice, byte[]> rootBufferEntry : rootBuffers.entrySet()) {
            try {
                byte[] rootBuf = rootBufferEntry.getValue();
                String rootBufStr = new String(rootBuf, Charset.forName("US-ASCII"));

                List<String> serviceBlocks = TextUtils.findAllBlocks(rootBufStr, "<service>", "</service>", true);
                for (String serviceBlock : serviceBlocks) {
                    String serviceType = TextUtils.findFirstBlock(serviceBlock, "<serviceType>", "</serviceType>", true);
                    String serviceId = TextUtils.findFirstBlock(serviceBlock, "<serviceId>", "</serviceId>", true);
                    String controlUrl = TextUtils.findFirstBlock(serviceBlock, "<controlURL>", "</controlURL>", true);
                    //String eventSubUrl = TextUtils.findFirstBlock(serviceBlock, "<eventSubURL>", "</eventSubURL>");
                    String scpdUrl = TextUtils.findFirstBlock(serviceBlock, "<SCPDURL>", "</SCPDURL>", true);

                    UpnpIgdServiceReference service = new UpnpIgdServiceReference(
                            rootBufferEntry.getKey(),
                            StringUtils.trim(serviceType),
                            StringUtils.trim(serviceId),
                            StringUtils.trim(controlUrl),
                            StringUtils.trim(scpdUrl));
                    services.add(service);
                }
            } catch (MalformedURLException e) {
                // do nothing, just skip
            }
        }
        return services;
    }

    private static Map<UpnpIgdServiceReference, byte[]> getServiceDescriptions(Set<UpnpIgdServiceReference> services)
            throws InterruptedException {
        Map<UpnpIgdServiceReference, byte[]> serviceXmls = new HashMap();
        
        ExecutorService executorService = null;
        try {
            int maximumPoolSize = (int) ((double) Runtime.getRuntime().availableProcessors() / (1.0 - 0.95));
            executorService = new ThreadPoolExecutor(0, maximumPoolSize, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
            
            List<HttpRequestCallable<UpnpIgdServiceReference>> tasks = new LinkedList<>();
            for (UpnpIgdServiceReference service : services)  {
                tasks.add(new HttpRequestCallable<>(service.getScpdUrl(), service));
            }
            
            List<Future<Pair<UpnpIgdServiceReference, byte[]>>> results = executorService.invokeAll(tasks);
            
            for (Future<Pair<UpnpIgdServiceReference, byte[]>> result : results) {
                try {
                    Pair<UpnpIgdServiceReference, byte[]> data = result.get();
                    serviceXmls.put(data.getKey(), data.getValue());
                } catch (InterruptedException | ExecutionException | CancellationException e) { // NOPMD
                    // do nothing, skip
                }
            }
        } finally {
            if (executorService != null) {
                executorService.shutdownNow();
            }
        }
        
        return serviceXmls;
    }
    
    private static Set<UpnpIgdService> parseServiceDescriptions(Map<UpnpIgdServiceReference, byte[]> scpds) {
        Set<UpnpIgdService> descriptions = new HashSet<>();
        
        for (Entry<UpnpIgdServiceReference, byte[]> scpdEntry : scpds.entrySet()) {
            byte[] scpdBuf = scpdEntry.getValue();
            String scpdBufStr = new String(scpdBuf, Charset.forName("US-ASCII"));
            
            
            // Get action names
            List<String> actions = TextUtils.findAllBlocks(scpdBufStr, "<actions>", "</actions>", true);
            Set<String> missingActionNames = new HashSet<>(EXPECTED_ACTION_NAMES);
            for (String action : actions) {
                String name = TextUtils.findFirstBlock(action, "<name>", "</name>", true);
                if (name == null) {
                    continue;
                }
                
                Iterator<String> it = missingActionNames.iterator();
                while (it.hasNext()) {
                    String missingActionName = it.next();
                    if (missingActionName.equalsIgnoreCase(name)) {
                        it.remove();
                        break;
                    }
                }
            }
            
            
            // Make sure no action names are missing
            if (!missingActionNames.isEmpty()) {
                continue;
            }


            // Get state variables
            Range<Long> leaseDurationRange = Range.between(0x0L, 0xFFFFFFFFL);
            Range<Long> externalPortRange = Range.between(0x0L, 0xFFFFL);
            List<String> stateVars = TextUtils.findAllBlocks(scpdBufStr, "<stateVariable>", "</stateVariable>", true);
            for (String stateVar : stateVars) {
                String name = TextUtils.findFirstBlock(stateVar, "<name>", "</name>", true);
                if (name == null) {
                    continue;
                }
                name = name.trim();
                
                if (name.equalsIgnoreCase("PortMappingLeaseDuration")) {
                    leaseDurationRange = extractRange(stateVar, 0L, 0xFFFFFFFFL);
                } else if (name.equalsIgnoreCase("ExternalPort")) {
                    externalPortRange = extractRange(stateVar, 0L, 0xFFFFL);
                }
            }


            // Generate service
            UpnpIgdService desc = new UpnpIgdService(scpdEntry.getKey(), leaseDurationRange, externalPortRange);
            descriptions.add(desc);
        }
        
        return descriptions;
    }
    
    private static Range<Long> extractRange(String block, long absoluteMin, long absoluteMax) {
        Validate.validState(block != null);
        Validate.validState(absoluteMin < absoluteMax);
        
        String allowedRangeBlock = TextUtils.findFirstBlock(block, "<allowedValueRange>", "</allowedValueRange>", true);
        if (allowedRangeBlock == null) {
            return null;
        }
        allowedRangeBlock = allowedRangeBlock.trim();

        String minStr = TextUtils.findFirstBlock(allowedRangeBlock, "<minimum>", "</minimum>", true);
        long min;
        if (minStr == null) {
            min = absoluteMin;
        } else {
            try {
                min = Long.valueOf(minStr.trim());
                min = Math.max(absoluteMin, min);
            } catch (NumberFormatException nfe) {
                min = absoluteMin;
            }
        }

        String maxStr = TextUtils.findFirstBlock(allowedRangeBlock, "<maximum>", "</maximum>", true);
        long max;
        if (maxStr == null) {
            max = absoluteMax;
        } else {
            try {
                max = Long.valueOf(maxStr.trim());
                max = Math.max(absoluteMax, max);
            } catch (NumberFormatException nfe) {
                max = absoluteMax;
            }
        }

        Range<Long> ret = Range.between(min, max);
        
        return ret;
    }

    private static final class HttpRequestCallable<T> implements Callable<Pair<T, byte[]>> {
        private static final int CONNECT_TIMEOUT = 1000;
        private static final int READ_TIMEOUT = 3000;
        
        private final URI uri;
        private final T reference;

        public HttpRequestCallable(URI uri, T reference) {
            this.uri = uri;
            this.reference = reference;
        }

        @Override
        public Pair<T, byte[]> call() throws Exception {
            URLConnection connection = uri.toURL().openConnection(Proxy.NO_PROXY);
            
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            try (InputStream is = connection.getInputStream()) {
                return new ImmutablePair<>(reference, IOUtils.toByteArray(is));
            } finally {
                IOUtils.close(connection);
            }
        }
    }
}
