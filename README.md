portmapper
==========

A Java library that provides facilities to open up ports on routers / Gateway Devices. Supported protocols include UPnP-IGD, NAT-PMP, and PCP.

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

// Try to automatically detect the Gateway Device (router) as well as the
// protocol to use for port mapping
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

If you wish to talk directly to your router rather than using PortMapperFactory, you can use the Controller classes: UpnpIgdController, NatPmpController, and PcpController.

## Desired improvements

* IPv6 support -- Do you have a router that uses IPv6? Please get in touch.
* Further testing -- Everything here has been tested with miniupnpd and Apple's Airport router. If you have access to a router that uses something other than miniupnpd, please get get in touch.