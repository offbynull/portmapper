# Port Mapper (beta)

<p align="center"><img src ="../gh-pages/logo.png" alt="Portmapper logo" /></p>

 * [Introduction](#introduction)
 * [Quick-start Guide](#quick-start-guide)
 * [FAQ](#faq)

## Introduction
Port mapper is a Java library, originally developed as part of the the [Peernetic](https://github.com/offbynull/peernetic) project, that allows you to open up UDP and TCP ports on NAT-enabled routers. The Port Mapper project provides several distinct advantages over existing Java libraries that provide port mapping functinoality:

* Tested on all major platforms: Android, Windows, Linux, and Mac
* Supports UPnP-IGD (Universal Plug-and-Play Internet Gateway Device) -- both IGD v1.0 and IGD v2.0
* Supports NAT-PMP (NAT Port Mapping Protocol)
* Supports PCP (Port Control Protocol)
* Supports both IPv4 and IPv6
* Fault-tolerant -- works around malformed responses and faulty routers
* Light-weight -- very few dependencies and easy to port to other languages

## Quick-start Guide

If you're using Maven, include portmapper as a dependency.

```xml
<dependency>
    <groupId>com.offbynull.portmapper</groupId>
    <artifactId>portmapper</artifactId>
    <version>2.0.0</version>
</dependency>
```


The following example attempts to map a port on the first port mapper it finds.

```java
// Start up a network gateway
Gateway network = NetworkGateway.create();
Bus networkBus = network.getBus();

// Start up a process gateway
Gateway process = ProcessGateway.create();
Bus processBus = process.getBus();

// Discover router's with port mapping functionality
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

#### Why is Port Mapper considered light-weight

The Port Mapper project doesn't require any special parsing libraries. All parsing is done as US-ASCII text. XML/SOAP/
#### Does Port Mapper support PCP authentication or UPnP-IGD device protection

Not at this time. Support may be added in the future.

#### What alternatives are available?

Alternatives to the Port Mapper project include:

* [weupnp](https://github.com/bitletorg/weupnp)
* [SBBI](https://sourceforge.net/projects/upnplibmobile/)
* [Cling](http://4thline.org/projects/cling/)
* [jNAT-PMPlib](http://sourceforge.net/projects/jnat-pmplib/)

If you know of any other projects please let me know and I'll update this section.