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

import com.offbynull.portmapper.PortType;
import com.offbynull.portmapper.common.ResponseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.DOMException;

/**
 * UPNP-IGD controller.
 * @author Kasra Faghihi
 */
public final class UpnpIgdController implements Closeable {

    private static final long RANDOM_PORT_TEST_SLEEP = 5L;
    
    private InetAddress selfAddress;
    private URI controlUrl;
    private String serviceType;
    private Range<Long> externalPortRange;
    private Range<Long> leaseDurationRange;
    
    private Lock activePortsLock;
    private Map<Integer, PortMappingInfo> activePorts; // external port to mapping info
    private ScheduledExecutorService scheduledPortTester;

    /**
     * Constructs a UPNP-IGD controller.
     * @param service UPNP-IGD port mapping service
     * @param listener event listener
     */
    public UpnpIgdController(UpnpIgdService service, UpnpIgdControllerListener listener) {
        this(service.getServiceReference().getDevice().getSelfAddress(),
                service.getServiceReference().getControlUrl(),
                service.getServiceReference().getServiceType(),
                listener);
        externalPortRange = service.getExternalPortRange();
        leaseDurationRange = service.getLeaseDurationRange();
    }
    
    /**
     * Constructs a UPNP-IGD controller.
     * @param selfAddress address of this machine.
     * @param controlUrl control URL
     * @param serviceType service type
     * @param listener event listener
     * @throws NullPointerException if any argument other than {@code listener} is {@code null}
     */
    public UpnpIgdController(InetAddress selfAddress, URI controlUrl, String serviceType, final UpnpIgdControllerListener listener) {
        Validate.notNull(selfAddress);
        Validate.notNull(controlUrl);
        Validate.notNull(serviceType);
        this.selfAddress = selfAddress;
        this.controlUrl = controlUrl;
        this.serviceType = serviceType;
        
        activePortsLock = new ReentrantLock();
        activePorts = new HashMap<>();
        scheduledPortTester = Executors.newSingleThreadScheduledExecutor(
                new BasicThreadFactory.Builder().daemon(false).namingPattern("upnp-port-tester").build());
        
        if (listener != null) {
            scheduledPortTester.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    // get random port mapping
                    List<PortMappingInfo> ports;        
                    activePortsLock.lock();
                    try {
                        ports = new ArrayList<>(activePorts.values());
                    } finally {
                        activePortsLock.unlock();
                    }
                    
                    if (ports.isEmpty()) {
                        return;
                    }
                    
                    Random random = new Random();
                    PortMappingInfo oldPmi = ports.get(random.nextInt(ports.size()));
                    
                    
                    // check to see if its still active
                    boolean mappingFailed;
                    try {
                        PortMappingInfo newPmi = getMappingDetails(oldPmi.getExternalPort(), oldPmi.getPortType());
                        
                        mappingFailed = !newPmi.getInternalClient().equals(UpnpIgdController.this.selfAddress)
                                || newPmi.getInternalPort() != oldPmi.getInternalPort()
                                || newPmi.getPortType() != oldPmi.getPortType();
                    } catch (Exception e) {
                        mappingFailed = true;
                    }
                    
                    // if it isn't, check to see that the user didn't remove it while we were testing it and notify
                    if (mappingFailed) {
                        activePortsLock.lock();
                        try {
                            PortMappingInfo testPmi = activePorts.get(oldPmi.getExternalPort());
                            if (testPmi == null) {
                                return;
                            }
                            
                            if (testPmi.getInternalClient().equals(UpnpIgdController.this.selfAddress)
                                    && testPmi.getInternalPort() == oldPmi.getInternalPort()
                                    && testPmi.getPortType() == oldPmi.getPortType()) {
                                activePorts.remove(oldPmi.externalPort);
                                listener.mappingExpired(oldPmi);
                            }
                        } finally {
                            activePortsLock.unlock();
                        }
                    }
                }
            }, RANDOM_PORT_TEST_SLEEP, RANDOM_PORT_TEST_SLEEP, TimeUnit.SECONDS);
        }
    }

    // CHECKSTYLE:OFF custom exception in javadoc not being recognized
    /**
     * Get mapping detail for some exposed port.
     * @param externalPort external port
     * @param portType port type
     * @return port mapping information for that external port
     * @throws NullPointerException if portType is {@code null}
     * @throws IllegalArgumentException if {@code externalPort} isn't between {@code 0} to {@code 65535}, or if the {@code externalPort}
     * isn't between the external port range specified by the service (if one was specified)
     * @throws ResponseException if the router responds with an error
     */
    public PortMappingInfo getMappingDetails(int externalPort, PortType portType) {
        // CHECKSTYLE:ON
        Validate.inclusiveBetween(0, 65535, externalPort); // 0 = wildcard, any unassigned port? may not be supported according to docs
        Validate.notNull(portType);
        
        if (externalPortRange != null) {
            Validate.inclusiveBetween((long) externalPortRange.getMinimum(), (long) externalPortRange.getMaximum(), (long) externalPort);
        }
        
        Map<String, String> respParams = performRequest("GetSpecificPortMappingEntry",
                ImmutablePair.of("NewRemoteHost", ""),
                ImmutablePair.of("NewExternalPort", "" + externalPort),
                ImmutablePair.of("NewProtocol", portType.name()));
        
        try {
            int internalPort = NumberUtils.createInteger(respParams.get("NewInternalPort"));
            InetAddress internalClient = InetAddress.getByName(respParams.get("NewInternalClient"));
            long remainingDuration = NumberUtils.createInteger(respParams.get("NewLeaseDuration"));
            
            return new PortMappingInfo(internalPort, externalPort, portType, internalClient, remainingDuration);
        } catch (UnknownHostException | NumberFormatException | NullPointerException e) {
            throw new ResponseException(e);
        }
    }
    
    /**
     * Port mapping information.
     */
    public static final class PortMappingInfo {
        private int internalPort;
        private int externalPort;
        private PortType portType;
        private InetAddress internalClient;
        private long remainingDuration;

        private PortMappingInfo(int internalPort, int externalPort, PortType portType, InetAddress internalClient, long remainingDuration) {
            this.internalPort = internalPort;
            this.externalPort = externalPort;
            this.portType = portType;
            this.internalClient = internalClient;
            this.remainingDuration = remainingDuration;
        }

        /**
         * Get internal port.
         * @return internal port
         */
        public int getInternalPort() {
            return internalPort;
        }

        /**
         * Get external port.
         * @return external port
         */
        public int getExternalPort() {
            return externalPort;
        }

        /**
         * Get port type.
         * @return port type
         */
        public PortType getPortType() {
            return portType;
        }

        /**
         * Get internal client address.
         * @return internal client address
         */
        public InetAddress getInternalClient() {
            return internalClient;
        }

        /**
         * Get remaining duration for mapping.
         * @return remaining duration for mapping
         */
        public long getRemainingDuration() {
            return remainingDuration;
        }

        @Override
        public String toString() {
            return "PortMappingInfo{" + "internalPort=" + internalPort + ", externalPort=" + externalPort + ", portType=" + portType
                    + ", internalClient=" + internalClient + ", remainingDuration=" + remainingDuration + '}';
        }
        
    }
    
    // CHECKSTYLE:OFF custom exception in javadoc not being recognized
    /**
     * Add a port mapping.
     * @param externalPort external port
     * @param internalPort internal port
     * @param portType port type
     * @param duration mapping duration (0 = indefinite, may or may not be supported by router)
     * @return port mapping information for the new mapping
     * @throws NullPointerException if portType is {@code null}
     * @throws IllegalArgumentException if {@code externalPort} isn't between {@code 0} to {@code 65535}, or if {@code externalPort}
     * isn't between the external port range specified by the service (if one was specified), or if {@code internalPort} isn't between
     * {@code 1} to {@code 65535}, or if {@code duration} isn't between the duration range specified by the service (if one was specified)
     * @throws ResponseException if the router responds with an error, or if {@code duration} is negative
     */
    public PortMappingInfo addPortMapping(int externalPort, int internalPort, PortType portType, long duration) {
        // CHECKSTYLE:ON
        Validate.inclusiveBetween(0, 65535, externalPort); // 0 = wildcard, any unassigned port? may not be supported according to docs
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.notNull(portType);
        Validate.inclusiveBetween(0L, Long.MAX_VALUE, duration);

        if (externalPortRange != null) {
            Validate.inclusiveBetween((long) externalPortRange.getMinimum(), (long) externalPortRange.getMaximum(), (long) externalPort);
        }

        if (leaseDurationRange != null) {
            if (duration == 0L) {
                Validate.isTrue(leaseDurationRange.getMinimum() == 0, "Infinite duration not allowed");
            }
            duration = Math.max(leaseDurationRange.getMinimum(), duration);
            duration = Math.min(leaseDurationRange.getMaximum(), duration);
        }
        
        performRequest("AddPortMapping",
                ImmutablePair.of("NewRemoteHost", ""),
                ImmutablePair.of("NewExternalPort", "" + externalPort),
                ImmutablePair.of("NewProtocol", portType.name()),
                ImmutablePair.of("NewInternalPort", "" + internalPort),
                ImmutablePair.of("NewInternalClient", selfAddress.getHostAddress()),
                ImmutablePair.of("NewEnabled", "1"),
                ImmutablePair.of("NewPortMappingDescription", ""),
                ImmutablePair.of("NewLeaseDuration", "" + duration));
        
        
        PortMappingInfo info = getMappingDetails(externalPort, portType);
        
        activePortsLock.lock();
        try {
            activePorts.put(externalPort, info);
        } finally {
            activePortsLock.unlock();
        }

        return info;
    }

    // CHECKSTYLE:OFF custom exception in javadoc not being recognized
    /**
     * Delete a port mapping.
     * @param externalPort external port
     * @param portType port type
     * @throws NullPointerException if portType is {@code null}
     * @throws IllegalArgumentException if {@code externalPort} isn't between {@code 0} to {@code 65535}, or if the {@code externalPort}
     * isn't between the external port range specified by the service (if one was specified)
     * @throws ResponseException if the router responds with an error
     */
    public void deletePortMapping(int externalPort, PortType portType) {
        // CHECKSTYLE:ON
        Validate.inclusiveBetween(1, 65535, externalPort);
        Validate.notNull(portType);
        
        if (externalPortRange != null) {
            Validate.inclusiveBetween((long) externalPortRange.getMinimum(), (long) externalPortRange.getMaximum(), (long) externalPort);
        }
        
        /*PortMappingInfo info = */getMappingDetails(externalPort, portType);
        
        performRequest("DeletePortMapping",
                ImmutablePair.of("NewRemoteHost", ""),
                ImmutablePair.of("NewExternalPort", "" + externalPort),
                ImmutablePair.of("NewProtocol", portType.name()));
        
        activePortsLock.lock();
        try {
            activePorts.remove(externalPort);
        } finally {
            activePortsLock.unlock();
        }
    }

    // CHECKSTYLE:OFF custom exception in javadoc not being recognized
    /**
     * Get the external IP address.
     * @return external IP address
     * @throws ResponseException if the router responds with an error
     */
    public InetAddress getExternalIp() {
        // CHECKSTYLE:ON
        Map<String, String> responseParams = performRequest("GetExternalIPAddress");
        
        try {
            return InetAddress.getByName(responseParams.get("NewExternalIPAddress"));
        } catch (UnknownHostException | NullPointerException e) {
            throw new ResponseException(e);
        }
    }
    
    private Map<String, String> performRequest(String action, ImmutablePair<String, String> ... params) {
        byte[] outgoingData = createRequestXml(action, params);
        
        HttpURLConnection conn = null;
                
        try {
            URL url = controlUrl.toURL();
            conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);

            conn.setRequestMethod("POST");
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("SOAPAction", serviceType + "#" + action);
            conn.setRequestProperty("Connection", "Close");
        } catch (IOException ex) {
            throw new IllegalStateException(ex); // should never happen
        }
        
        
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(outgoingData);
        } catch (IOException ex) {
            IOUtils.close(conn);
            throw new ResponseException(ex);
        }

        byte[] incomingData;
        int respCode;
        try {
            respCode = conn.getResponseCode();
        } catch (IOException ioe) {
            IOUtils.close(conn);
            throw new ResponseException(ioe);
        }
        
        if (respCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            try (InputStream is = conn.getErrorStream()) {
                incomingData = IOUtils.toByteArray(is);
            } catch (IOException ex) {
                IOUtils.close(conn);
                throw new ResponseException(ex);
            }
        } else {
            try (InputStream is = conn.getInputStream()) {
                incomingData = IOUtils.toByteArray(is);
            } catch (IOException ex) {
                IOUtils.close(conn);
                throw new ResponseException(ex);
            }
        }
        
        return parseResponseXml(action + "Response", incomingData);
    }

    private Map<String, String> parseResponseXml(String expectedTagName, byte[] data) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(data));
            
            if (soapMessage.getSOAPBody().hasFault()) {
                StringWriter writer = new StringWriter();
                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    transformer.transform(new DOMSource(soapMessage.getSOAPPart()), new StreamResult(writer));
                } catch (IllegalArgumentException | TransformerException | TransformerFactoryConfigurationError e) {
                    writer.append("Failed to dump fault: " + e);
                }
                
                throw new ResponseException(writer.toString());
            }
            
            Iterator<SOAPBodyElement> responseBlockIt = soapMessage.getSOAPBody().getChildElements(
                    new QName(serviceType, expectedTagName));
            if (!responseBlockIt.hasNext()) {
                throw new ResponseException(expectedTagName + " tag missing");
            }
            
            Map<String, String> ret = new HashMap<>();
            
            SOAPBodyElement responseNode = responseBlockIt.next();
            Iterator<SOAPBodyElement> responseChildrenIt = responseNode.getChildElements();
            while (responseChildrenIt.hasNext()) {
                SOAPBodyElement param = responseChildrenIt.next();
                String name = StringUtils.trim(param.getLocalName().trim());
                String value = StringUtils.trim(param.getValue().trim());
                
                ret.put(name, value);
            }
            
            return ret;
        } catch (IllegalArgumentException | IOException | SOAPException | DOMException e) {
            throw new IllegalStateException(e); // should never happen
        }
    }
    
    private byte[] createRequestXml(String action, ImmutablePair<String, String> ... params) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage soapMessage = factory.createMessage();
            
            SOAPBodyElement actionElement = soapMessage.getSOAPBody().addBodyElement(new QName(null, action, "m"));
            actionElement.addNamespaceDeclaration("m", serviceType);
            
            for (Pair<String, String> param : params) {
                SOAPElement paramElement = actionElement.addChildElement(QName.valueOf(param.getKey()));
                paramElement.setValue(param.getValue());
            }

            soapMessage.getSOAPPart().setXmlStandalone(true);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(soapMessage.getSOAPPart()), new StreamResult(baos));

            return baos.toByteArray();
        } catch (IllegalArgumentException | SOAPException | TransformerException | DOMException e) {
            throw new IllegalStateException(e); // should never happen
        }
    }

    @Override
    public void close() throws IOException {
        scheduledPortTester.shutdownNow();
    }
}
