# Port Mapper

<p align="center"><img src ="../gh-pages/logo.png" alt="Portmapper logo" /></p>

The Port Mapper project is a Java library that allows you to forward ports on NAT-enabled routers. Originally developed as part of the [Peernetic](https://github.com/offbynull/peernetic) project, Port Mapper has several distinct advantages over existing Java libraries that provide port forwarding functionality.

* Tested on all major platforms: Android, Windows, Linux, and Mac
* Supports UPnP-IGD (Universal Plug-and-Play Internet Gateway Device) -- both IGD v1.0 and IGD v2.0 services
* Supports NAT-PMP (Network Address Traversal Port Mapping Protocol)
* Supports PCP (Port Control Protocol)
* Supports both IPv4 and IPv6
* Fault-tolerant -- works around malformed responses and faulty devices
* Light-weight -- very few third-party dependencies and easy to port to other languages

## Table of Contents

 * [Introduction](#introduction)
 * [Quick-start Guide](#quick-start-guide)
 * [FAQ](#faq)
  * [What if I want to discover only one type of port forwarding device?](#what-if-i-want-to-discover-only-one-type-of-port-forwarding-device)
  * [How is this library considered light-weight?](#how-is-this-library-considered-light-weight)
  * [How is this library considered fault-tolerant?](#how-is-this-library-considered-fault-tolerant)
  * [How does this library discover NAT-PMP and PCP gateway devices?](#how-does-this-library-discover-nat-pmp-and-pcp-gateway-devices)
  * [Does this library support PCP authentication and/or UPnP-IGD device protection?](#does-this-library-support-pcp-authentication-andor-upnp-igd-device-protection)
  * [Does this library support unsolicited PCP ANNOUNCEs or UPnP eventing?](#does-this-library-support-unsolicited-pcp-announces-or-upnp-eventing)
  * [What alternatives are available?](#what-alternatives-are-available)
 * [Change Log](#change-log)

## Quick-start Guide

Port Mapper requires Java7 or later. In your Maven POM, add "portmapper" as a dependency.

```xml
<dependency>
    <groupId>com.offbynull.portmapper</groupId>
    <artifactId>portmapper</artifactId>
    <version>2.0.0</version>
</dependency>
```


The following example attempts to forward some external port (55555 preferred) to internal port 12345 on the first port forwarding device it finds.

```java
// Start gateways
Gateway network = NetworkGateway.create();
Gateway process = ProcessGateway.create();
Bus networkBus = network.getBus();
Bus processBus = process.getBus();

// Discover port forwarding devices and take the first one found
List<PortMapper> mappers = PortMapperFactory.discover(networkBus, processBus);
PortMapper mapper = mappers.get(0);

// Map internal port 12345 to some external port (55555 preferred)
//
// IMPORTANT NOTE: Many devices prevent you from mapping ports that are <= 1024
// (both internal and external ports). Be mindful of this when choosing which
// ports you want to map.
MappedPort mappedPort = mapper.mapPort(PortType.TCP, 12345, 55555, 60);
System.out.println("Port mapping added: " + mappedPort);

// Refresh mapping half-way through the lifetime of the mapping (for example,
// if the mapping is available for 40 seconds, refresh it every 20 seconds)
while(!shutdown) {
    mappedPort = mapper.refreshPort(mappedPort, mappedPort.getLifetime() / 2L);
    System.out.println("Port mapping refreshed: " + mappedPort);
    Thread.sleep(mappedPort.getLifetime() * 1000L);
}

// Unmap port 12345
mapper.unmapPort(mappedPort);

// Stop gateways
networkBus.send(new KillNetworkRequest());
processBus.send(new KillProcessRequest()); // can kill this after discovery
```

## FAQ

#### What if I want to discover only one type of port forwarding device?

You can use the identify method on PortMapper implementations directly.

```java
List<UpnpIgdPortMapper> upnpIgdMappers = UpnpIgdPortMapper.identify(networkBus);
        
List<NatPmpPortMapper> natPmpMappers = NatPmpPortMapper.identify(networkBus, processBus, additionalIps);
        
List<PcpPortMapper> pcpMappers = PcpPortMapper.identify(networkBus, processBus, additionalIps);
```

#### How is this library considered light-weight?

Several reasons. The Port Mapper project

1. has very few dependencies on third-party libraries.
1. doesn't require any special parsing libraries (e.g. XML/SOAP/HTTP/etc..) -- all parsing is done as US-ASCII text.
1. doesn't require any special networking libraries (e.g. Netty/MINA/etc..) -- all networking is done through standard NIO.
1. doesn't make use of regular expressions.

Because of this, the code should be easily portable to other languages -- especially languages that don't have the same robust ecosystem that Java does.

#### How is this library considered fault-tolerant?

The Port Mapper project aims to be resilient when it comes to faulty responses, especially when using UPnP-IGD. The code

1. parses XML as text, based on patterns/heuristics (works around issues such as invalid XML syntax/invalid XML structure/incorrect capitalization/etc..).
1. attempts requests multiple times when the device responds with a failure (works around temporary network failure and other temporary hiccups that cause bad response codes).

#### How does this library discover NAT-PMP and PCP gateway devices?

Unfortunately, Java doesn't provide a built-in way to grab gateway addresses from the OS, nor does it allow you to do ICMP probing to find devices on path (e.g. set TTL to 1 and ping, the first device is very likely the gateway). As such, the Port Mapper project makes use of various OS-specific commands to find gateway addresses. You can find out which commands are used by looking through the source code.

#### Does this library support PCP authentication and/or UPnP-IGD device protection?

Not at this time. Support may be added in the future.

#### Does this library support unsolicited PCP ANNOUNCEs or UPnP eventing?

Not at this time. Version 1 did support unsolicited PCP ANNOUNCEs, but it has since been removed because no devices seem to support it. If you're worried about not being notified of lost mappings, just make sure you refresh more often so that you catch the problem early (e.g. every 5 or 10 minutes).

#### What alternatives are available?

Alternatives to Port Mapper include:

* [weupnp](https://github.com/bitletorg/weupnp)
* [SBBI](https://sourceforge.net/projects/upnplibmobile/)
* [Cling](http://4thline.org/projects/cling/)
* [jNAT-PMPlib](http://sourceforge.net/projects/jnat-pmplib/)

If you know of any other projects please let me know and I'll update this section.

## Change Log
<sub>Template adapted from http://keepachangelog.com/</sub>

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [Unreleased][unreleased]

### [2.0.0] - 2016-01-17
- CHANGED: Refactored API and backend
- CHANGED: Updated README
- ADDED: UPnP-IGD IPv6 firewall service support
- ADDED: New logo made in Inkscape
- FIXED: Issue when scraping IPv4 address from output
- FIXED: Issue with Macs getting SocketExceptions on PCP discovery

### [1.0.0] - 2014-06-24
- Initial release.