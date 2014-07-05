portmapper
==========

A Java library that provides facilities to open up ports on home routers. Supported protocols are UPnP-IGD (Universal Plug-and-Play Internet Gateway Device), NAT-PMP (NAT Port Mapping Protocol), and PCP (Port Control Protocol).

## Quick-start guide

If you're using Maven, include portmapper as a dependency.

```xml
<dependency>
    <groupId>com.offbynull.portmapper</groupId>
    <artifactId>portmapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

Otherwise, compile the source manually and include in your project.


The following example attempts to discover a router with at least one of the supported protocols and map internal port 12345 to some external port.

```java
PortMapperEventListener listener = x -> System.err.println(
        "Port mappings may have been lost or expired! >> " + x);

// Try to auto-discover router's IP as well as the protocol supported
// by the router for port mapping
try (PortMapper portMapper = PortMapperFactory.create(listener)) {

    // Map port 12345 to a random external port
    MappedPort mappedPort = portMapper.mapPort(PortType.TCP, 12345, 60);
    System.out.println("Port mapping added: "
            + mappedPort.getInternalPort() + " to "
            + mappedPort.getExternalPort() + " on "
            + mappedPort.getExternalAddress());

    // Refresh mapping half-way through the lifetime of the mapping (for
    // example, if the mapping is available for 40 seconds, refresh it 
    // every 20 seconds)
    while(!shutdown) {
        mappedPort = portMapper.refreshPort(mappedPort,
                mappedPort.getLifetime() / 2L);
        System.out.println("Port mapping refreshed: "
                + mappedPort.getInternalPort() + " to "
                + mappedPort.getExternalPort() + " on "
                + mappedPort.getExternalAddress());
        Thread.sleep(mappedPort.getLifetime() * 1000L);
    }

    // Unmap port 12345
    portMapper.unmapPort(mappedPort);
```

If you want to use a specific port mapping protocol, you can instantiate and use PortMapper implementations directly: UpnpIgdPortMapper, NatPmpPortMapper, and PcpPortMapper.

If you want to more control over how a port mapping protocol is used, you can instantiate and use Controller implementations directly: UpnpIgdController, NatPmpController, and PcpController.

## Missing Features / Desired Improvements

* IPv6 support -- IPv6 support is still missing for UPnP-IGD and PCP. If you have a router that uses IPv6 internally or externally, please send me a message.
* Device discovery -- NAT-PMP and PCP require a better device-discovery mechanism.
* Further testing -- Everything here has been tested with miniupnpd and Apple's Airport router. If you have access to a router that uses an implementation other than miniupnpd, please send me a message.
* Remove Guava as a dependency.