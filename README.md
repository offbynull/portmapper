# Port Mapper (beta)

<p align="center"><img src ="../gh-pages/logo.png" alt="Portmapper logo" /></p>

 * [Introduction](#introduction)
 * [Quick-start Guide](#quick-start-guide)
 * [FAQ](#faq)

## Introduction
Port mapper is a Java library that allows you to open up ports on NAT-enabled routers. Why use Port Mapper over other Java port mapping libraries? The Port Mapper project provides several distinct advantages:

* Tested on all major platforms: Android, Windows, Linux, and Mac
* Supports UPnP-IGD (Universal Plug-and-Play Internet Gateway Device) -- both IGD v1.0 and IGD v2.0
* Supports NAT-PMP (NAT Port Mapping Protocol)
* Supports PCP (Port Control Protocol)
* Supports both IPv4 and IPv6
* Light-weight implementation -- very few dependencies and easy to port to other languages
* Fault-tolerant implementation -- works around malformed responses and faulty routers
* 

## Quick-start Guide

If you're using Maven, include portmapper as a dependency.

```xml
<dependency>
    <groupId>com.offbynull.portmapper</groupId>
    <artifactId>portmapper</artifactId>
    <version>2.0.0</version>
</dependency>
```


The following example attempts to discover a router with at least one of the supported protocols and map internal port 12345 to some external port.

```java
// Start up a network gateway
Gateway network = NetworkGateway.create();
Bus networkBus = network.getBus();

// Start up a process gateway
Gateway process = ProcessGateway.create();
Bus processBus = process.getBus();

// Try to auto-discover router's IP as well as the protocol supported
// by the router for port mapping
List<PortMapper> mappers = PortMapperFactory.discover(networkBus, processBus);

// Kill the process gateway after discovery finishes -- no longer required
processBus.send(new KillProcessRequest());

// Map internal port 12345 to some external port (55555 preferred)
MappedPort mappedPort = portMapper.mapPort(PortType.TCP, 12345, 55555, 60);
System.out.println("Port mapping added: " + mappedPort);

// Refresh mapping half-way through the lifetime of the mapping (for example,
// if the mapping is available for 40 seconds, refresh it every 20 seconds)
while(!shutdown) {
    mappedPort = portMapper.refreshPort(mappedPort,
            mappedPort.getLifetime() / 2L);
    System.out.println("Port mapping refreshed: " + mappedPort);
    Thread.sleep(mappedPort.getLifetime() * 1000L);
}

// Unmap port 12345
portMapper.unmapPort(mappedPort);


// Kill the network gateway once your application completes
networkBus.send(new KillNetworkRequest());
```

## FAQ

#### How much overhead am I adding?