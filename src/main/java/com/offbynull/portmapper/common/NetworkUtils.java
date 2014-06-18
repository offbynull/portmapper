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
package com.offbynull.portmapper.common;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.channels.MulticastChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Network-related utility class.
 * @author Kasra Faghihi
 */
public final class NetworkUtils {
    /**
     * IPs grabbed from http://www.techspot.com/guides/287-default-router-ip-addresses/ + comments @ 1/19/2014.
     */
    @SuppressWarnings("PMD")
    private static final Set<String> PRESET_IPV4_GATEWAY_ADDRESSES = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
        "192.168.1.1", // 3Com
        "10.0.1.1", // Apple
        "192.168.1.1", "192.168.1.220", // Asus
        "192.168.2.1", "10.1.1.1", // Belkin
        "192.168.15.1", // Cisco
        "192.168.11.1", // Buffalo
        "192.168.1.1", // Dell
        "192.168.0.1", "192.168.0.30", "192.168.0.50", "192.168.1.1", "192.10.1.1.1", // D-Link
        "192.168.0.1", "192.168.1.1", // Linksys
        "192.168.2.1", // Microsoft
        "192.168.10.1", "192.168.20.1", "192.168.30.1", "192.168.62.1", "192.168.100.1", "192.168.102.1", "192.168.1.254", // Motorola
        "192.168.1.254", // MSI
        "192.168.0.1", "192.168.0.227", "192.168.1.1", // Netgear
        "192.168.0.1", // Senao
        "10.0.0.138", "192.168.1.254", // SpeedTouch
        "10.0.0.138", // Telstra
        "192.168.1.1", // TP-LINK
        "192.168.0.1", "192.168.1.1", "192.168.2.1", "192.168.10.1", // Trendnet
        "192.168.1.1", "192.168.2.1", "192.168.123.254", // U.S. Robotics
        "192.168.1.1", "192.168.2.1", "192.168.4.1", "192.168.10.1", "192.168.1.254", "10.0.0.2", "10.0.0.138" // Zyxel
    )));

    private NetworkUtils() {
        // do nothing
    }
    
    /**
     * Convert a IP address to a IPv6 address and dump as a byte array. Essentially, if the input is IPv4 it'll be converted to an
     * IPv4-to-IPv6 address. Otherwise, the IPv6 address will be dumped as-is.
     * @param address address to convert to a ipv6 byte array
     * @return ipv6 byte array
     * @throws NullPointerException if any argument is {@code null}
     */
    public static byte[] convertToIpv6Array(InetAddress address) {
        Validate.notNull(address);
        
        byte[] addrArr = address.getAddress();
        switch (addrArr.length) {
            case 4: {
                // convert ipv4 address to ipv4-mapped ipv6 address
                byte[] newAddrArr = new byte[16];
                newAddrArr[10] = (byte) 0xff;
                newAddrArr[11] = (byte) 0xff;
                System.arraycopy(addrArr, 0, newAddrArr, 12, 4);
                
                return newAddrArr;
            }
            case 16: {
                return addrArr;
            }
            default:
                throw new IllegalStateException();
        }
    }
    
    /**
     * Get IP addresses for all interfaces on this machine that are IPv4.
     * @return IPv4 addresses assigned to this machine
     * @throws IOException if there's an error
     * @throws NullPointerException if any argument is {@code null}
     */
    public static Set<InetAddress> getAllLocalIpv4Addresses() throws IOException {
        Set<InetAddress> ret = new HashSet<>();
        
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
            while (addrs.hasMoreElements()) { // make sure atleast 1 ipv4 addr bound to interface
                InetAddress addr = addrs.nextElement();

                if (addr instanceof Inet4Address && !addr.isAnyLocalAddress()) {
                    ret.add(addr);
                }
            }
        }
        
        return ret;
    }
    
    /**
     * Get IP addresses for all interfaces on this machine that are IPv6.
     * @return IPv6 addresses assigned to this machine
     * @throws IOException if there's an error
     * @throws NullPointerException if any argument is {@code null}
     */
    public static Set<InetAddress> getAllLocalIpv6Addresses() throws IOException {
        Set<InetAddress> ret = new HashSet<>();
        
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
            while (addrs.hasMoreElements()) { // make sure atleast 1 ipv4 addr bound to interface
                InetAddress addr = addrs.nextElement();

                if (addr instanceof Inet6Address && !addr.isAnyLocalAddress()) {
                    ret.add(addr);
                }
            }
        }
        
        return ret;
    }

    /**
     * Set a {@link MulticastChannel} to listen on all IPv4 interfaces.
     * @param channel multicast channel to listen on
     * @throws IOException if there's an error
     * @throws NullPointerException if any argument is {@code null}
     */
    public static void multicastListenOnAllIpv4InterfaceAddresses(MulticastChannel channel) throws IOException {
        Validate.notNull(channel);
        
        final InetAddress ipv4Group = InetAddress.getByName("224.0.0.1"); // NOPMD

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
            while (addrs.hasMoreElements()) { // make sure atleast 1 ipv4 addr bound to interface
                InetAddress addr = addrs.nextElement();

                try {
                    if (addr instanceof Inet4Address) {
                        channel.join(ipv4Group, networkInterface);
                    }
                } catch (IOException ioe) { // NOPMD
                    // occurs with certain interfaces
                    // do nothing
                }
            }
        }
    }

    /**
     * Set a {@link MulticastChannel} to listen on all IPv6 interfaces.
     * @param channel multicast channel to listen on
     * @throws IOException if there's an error
     * @throws NullPointerException if any argument is {@code null}
     */
    public static void multicastListenOnAllIpv6InterfaceAddresses(MulticastChannel channel) throws IOException {
        Validate.notNull(channel);
        
        final InetAddress ipv6Group = InetAddress.getByName("ff02::1"); // NOPMD

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
            while (addrs.hasMoreElements()) { // make sure atleast 1 ipv4 addr bound to interface
                InetAddress addr = addrs.nextElement();

                try {
                    if (addr instanceof Inet6Address) {
                        channel.join(ipv6Group, networkInterface);
                    }
                } catch (IOException ioe) { // NOPMD
                    // occurs with certain interfaces, do nothing
                }
            }
        }
    }
    
    /**
     * Attempts to put together a list of gateway addresses using pre-set values and running OS-specific processes.
     * @return a list of possible addresses for gateway device
     * @throws InterruptedException if interrupted
     */
    public static Set<InetAddress> getPotentialGatewayAddresses() throws InterruptedException {
        // Ask OS for gateway address
        String netstatOutput = "";
        try {
            netstatOutput = ProcessUtils.runProcessAndDumpOutput(5000L, "netstat", "-rn");
        } catch (IOException ioe) { // NOPMD
            // do nothing
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
        }
        LinkedHashSet<String> strAddresses = new LinkedHashSet<>(RegexUtils.findAllIpv4Addresses(netstatOutput));
        
        // Push in defaults
        strAddresses.addAll(PRESET_IPV4_GATEWAY_ADDRESSES);
        
        LinkedHashSet<InetAddress> addresses = new LinkedHashSet<>();
        for (String strAddress : strAddresses) {
            try {
                InetAddress addr = InetAddress.getByName(strAddress);
                if (!addr.isAnyLocalAddress()) {
                    addresses.add(addr);
                }
            } catch (UnknownHostException uhe) { // NOPMD
                // do nothing
            }
        }
        
        return addresses;
    }
}
