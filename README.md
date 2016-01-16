# Port Mapper (beta)

<p align="center"><img src ="../gh-pages/logo.png" alt="Peernetic logo" /></p>

 * [Introduction](#introduction)

The port mapper is a Java library that provides facilities to open up ports on home routers. Why use Port Mapper over other Java port mapping libraries? The Port Mapper project provides several distinct advantages:

* Support for all major Java platforms: Android, Windows, Linux, and Mac
* Support for all major port mapping protocols: UPnP-IGD (Universal Plug-and-Play Internet Gateway Device), NAT-PMP (NAT Port Mapping Protocol), and PCP (Port Control Protocol).
* Resilent implementation that works around faulty or otherwise non-compliant routers

## Quick-start guide

If you're using Maven, include portmapper as a dependency.

```xml
<dependency>
    <groupId>com.offbynull.portmapper</groupId>
    <artifactId>portmapper</artifactId>
    <version>2.0.0</version>
</dependency>
```

Otherwise, compile the source manually and include in your project.


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

// Map internal port 12345 to external port 55555
MappedPort mappedPort = portMapper.mapPort(PortType.TCP, 12345, 55555, 60);
System.out.println("Port mapping added: " + mappedPort);

// Refresh mapping half-way through the lifetime of the mapping (for
// example, if the mapping is available for 40 seconds, refresh it 
// every 20 seconds)
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