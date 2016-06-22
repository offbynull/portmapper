package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import com.offbynull.portmapper.mappers.upnpigd.externalmessages.RootUpnpIgdResponse.ServiceReference;
import java.net.URL;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RootUpnpIgdResponseTest {

    // taken from http://ftp.icpdas.com/pub/beta_version/VHM/wince600/at91sam9g45m10ek_armv4i/cesysgen/oak/files/igd.xml -- modified
    private static final String FAULTY_BUFFER
            = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/xml\r\n"
            + "\r\n"
            + "eList>\n"
            + "        <service>\n"
            + "			<serviceType>urn:schemas-microsoft-com:service:OSInfo:1</serviceType>\n"
            + "			<serviceId>urn:microsoft-com:serviceId:OSInfo1</serviceId>\n"
            + "			<eventSubURL></eventSubURL>\n"
            + "			<SCPDURL>IGD-OSInfo.xml</SCPDURL>\n"
            + "		</service>\n"
            + "    </serviceList>\n"
            + "    <deviceList>\n"
            + "      <device>\n"
            + "        <deviceType>urn:schemas-upnp-org:device:WANDevice:1</deviceType>\n"
            + "        <friendlyName>WANDevice</friendlyName>\n"
            + "        <manufacturer>Microsoft</manufacturer>\n"
            + "        <manufacturerURL>http://www.microsoft.com/</manufacturerURL>\n"
            + "        <modelDescription>Residential Gateway</modelDescription>\n"
            + "		<modelName>Internet Connection Sharing</modelName>\n"
            + "		<modelNufffffffffffffffffffffffffffffist>\n"
            + "          <service>\n"
            + "            <serviceId>urn:upnp-org:serviceId:WANCommonIFC1</serviceId>\n"
            + "            <SCPDURL>IGD-WANCommonInterfaceConfig.xml</SCPDURL>\n"
            + "            <controlURL></controlURL>\n"
            + "            <eventSubURL></eventSubURL>\n"
            + "          </service>\n"
            + "        </serviceList>\n"
            + "        <deviceList>>>>>sadads\n"
            + "	      <device>\n"
            + "            <deviceType>urn:schemas-upnp-org:device:WANConnectionDevice:1</deviceType>\n"
            + "            <friendlyName>WANConnectionDevice</friendlyName>\n"
            + "            <manufacturer>Microsoft</manufacturer>\n"
            + "            <manufacturerURL>http://www.microsoft.com/</manufacturerURL>\n"
            + "			<modelDescription>Residential Gateway</modelDescription>\n"
            + "			<modelName>Internet Connection Sharing</modelName>\n"
            + "			<modelNumber>1</modelNumber>\n"
            + "			<modelURL>http://www.microsoft.com/</modelURL>\n"
            + "			<serialNumber>0000001</serialNumber>\n"
            + "            <UDN>uuid:WANConnectionDevice1</UDN>\n"
            + "			<UPC>00000-00001</UPC>\n"
            + "            <serviceList>\n"
            + "              <service>\n"
            + "                <serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType>\n"
            + "                <serviceId>urn:upnp-org:serviceId:WANIPConn1</serviceId>\n"
            + "                <SCPDURL>IGD-WANIPConnection.xml</SCPDURL>\n"
            + "                <eventSubURL></eventSubURL>\n"
            + "              </service>\n"
            + "       ";

    // https://github.com/nmaier/simpleDLNA/blob/master/server/Resources/description.xml
    private static final String GOOD_BUFFER_WITHOUT_IGD
            = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/xml\r\n"
            + "\r\n"
            + "<?xml version=\"1.0\"?>\n"
            + "<root xmlns=\"urn:schemas-upnp-org:device-1-0\" xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\""
            + " xmlns:sec=\"http://www.sec.co.kr/dlna\">\n"
            + "  <specVersion>\n"
            + "    <major>1</major>\n"
            + "    <minor>0</minor>\n"
            + "  </specVersion>\n"
            + "  <device>\n"
            + "    <dlna:X_DLNACAP/>\n"
            + "    <dlna:X_DLNADOC>DMS-1.50</dlna:X_DLNADOC>\n"
            + "    <UDN></UDN>\n"
            + "    <dlna:X_DLNADOC>M-DMS-1.50</dlna:X_DLNADOC>\n"
            + "    <friendlyName/>\n"
            + "    <deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType>\n"
            + "    <manufacturer>tn123.org</manufacturer>\n"
            + "    <manufacturerURL>https://tn123.org/</manufacturerURL>\n"
            + "    <modelName>sdlna Media Server</modelName>\n"
            + "    <modelDescription></modelDescription>\n"
            + "    <modelNumber></modelNumber>\n"
            + "    <modelURL>https://tn123.org/</modelURL>\n"
            + "    <serialNumber></serialNumber>\n"
            + "    <sec:ProductCap>smi,DCM10,getMediaInfo.sec,getCaptionInfo.sec</sec:ProductCap>\n"
            + "    <sec:X_ProductCap>smi,DCM10,getMediaInfo.sec,getCaptionInfo.sec</sec:X_ProductCap>\n"
            + "    <iconList>\n"
            + "      <icon>\n"
            + "        <mimetype>image/jpeg</mimetype>\n"
            + "        <width>48</width>\n"
            + "        <height>48</height>\n"
            + "        <depth>24</depth>\n"
            + "        <url>/icon/smallJPEG</url>\n"
            + "      </icon>\n"
            + "      <icon>\n"
            + "        <mimetype>image/png</mimetype>\n"
            + "        <width>48</width>\n"
            + "        <height>48</height>\n"
            + "        <depth>24</depth>\n"
            + "        <url>/icon/smallPNG</url>\n"
            + "      </icon>\n"
            + "      <icon>\n"
            + "        <mimetype>image/png</mimetype>\n"
            + "        <width>120</width>\n"
            + "        <height>120</height>\n"
            + "        <depth>24</depth>\n"
            + "        <url>/icon/largePNG</url>\n"
            + "      </icon>\n"
            + "      <icon>\n"
            + "        <mimetype>image/jpeg</mimetype>\n"
            + "        <width>120</width>\n"
            + "        <height>120</height>\n"
            + "        <depth>24</depth>\n"
            + "        <url>/icon/largeJPEG</url>\n"
            + "      </icon>\n"
            + "    </iconList>\n"
            + "    <serviceList>\n"
            + "      <service>\n"
            + "        <serviceType>urn:schemas-upnp-org:service:ContentDirectory:1</serviceType>\n"
            + "        <serviceId>urn:upnp-org:serviceId:ContentDirectory</serviceId>\n"
            + "        <SCPDURL>/contentDirectory.xml</SCPDURL>\n"
            + "        <controlURL>/serviceControl</controlURL>\n"
            + "        <eventSubURL></eventSubURL>\n"
            + "      </service>\n"
            + "      <service>\n"
            + "        <serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>\n"
            + "        <serviceId>urn:upnp-org:serviceId:ConnectionManager</serviceId>\n"
            + "        <SCPDURL>/connectionManager.xml</SCPDURL>\n"
            + "        <controlURL>/serviceControl</controlURL>\n"
            + "        <eventSubURL></eventSubURL>\n"
            + "      </service>\n"
            + "      <service>\n"
            + "        <serviceType>urn:schemas-upnp-org:service:X_MS_MediaReceiverRegistrar:1</serviceType>\n"
            + "        <serviceId>urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar</serviceId>\n"
            + "        <SCPDURL>/MSMediaReceiverRegistrar.xml</SCPDURL>\n"
            + "        <controlURL>/serviceControl</controlURL>\n"
            + "        <eventSubURL></eventSubURL>\n"
            + "      </service>\n"
            + "    </serviceList>\n"
            + "  </device>\n"
            + "</root>";

    // taken from https://github.com/syncthing/syncthing/issues/1187
    private static final String GOOD_BUFFER_1
            = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/xml\r\n"
            + "\r\n"
            + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<root>\n"
            + "    <specVersion>\n"
            + "        <major>1</major>\n"
            + "        <minor>0</minor>\n"
            + "    </specVersion>\n"
            + "    <URLBase>http://192.168.243.1</URLBase>\n"
            + "    <device>\n"
            + "        <deviceType>urn:schemas-upnp-org:device:InternetGatewayDevice:1</deviceType>\n"
            + "        <friendlyName>Linksys WRT120N</friendlyName>\n"
            + "        <manufacturer>Linksys Inc.</manufacturer>\n"
            + "        <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "        <modelDescription>Internet Access Server</modelDescription>\n"
            + "        <modelName>WRT120N</modelName>\n"
            + "        <modelNumber>v1.0.04</modelNumber>\n"
            + "        <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "        <serialNumber>JUT00L209293</serialNumber>\n"
            + "        <UDN>uuid:00000000-0000-0001-0000-98fc11bd6774</UDN>\n"
            + "        <UPC>494125014380</UPC>\n"
            + "        <serviceList>\n"
            + "            <service>\n"
            + "                <serviceType>urn:schemas-upnp-org:service:Layer3Forwarding:1</serviceType>\n"
            + "                <serviceId>urn:upnp-org:serviceId:L3Forwarding1</serviceId>\n"
            + "                <SCPDURL>/igd_l3f.xml</SCPDURL>\n"
            + "                <controlURL>http://192.168.243.1:80/upnp/control?Layer3Forwarding</controlURL>\n"
            + "                <eventSubURL>http://192.168.243.1:80/upnp/event?Layer3Forwarding</eventSubURL>\n"
            + "            </service>\n"
            + "        </serviceList>\n"
            + "        <deviceList>\n"
            + "            <device>\n"
            + "                <deviceType>urn:schemas-upnp-org:device:WANDevice:1</deviceType>\n"
            + "                <friendlyName>Linksys WRT120N (WAN)</friendlyName>\n"
            + "                <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                <modelDescription>Internet Access Server (WAN Interface Device)</modelDescription>\n"
            + "                <modelName>WRT120N WAN Interface</modelName>\n"
            + "                <modelNumber>v1.0.04</modelNumber>\n"
            + "                <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                <serialNumber>JUT00L209293</serialNumber>\n"
            + "                <UDN>uuid:00000000-0000-0001-0001-98fc11bd6774</UDN>\n"
            + "                <UPC>494125014380</UPC>\n"
            + "                <serviceList>\n"
            + "                    <service>\n"
            + "                        <serviceType>urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1</serviceType>\n"
            + "                        <serviceId>urn:upnp-org:serviceId:WANCommonIFC1</serviceId>\n"
            + "                        <SCPDURL>/igd_wcic.xml</SCPDURL>\n"
            + "                        <controlURL>http://192.168.243.1:80/upnp/control?WANCommonInterfaceConfig</controlURL>\n"
            + "                        <eventSubURL>http://192.168.243.1:80/upnp/event?WANCommonInterfaceConfig</eventSubURL>\n"
            + "                    </service>\n"
            + "                </serviceList>\n"
            + "                <deviceList>\n"
            + "                    <device>\n"
            + "                        <deviceType>urn:schemas-upnp-org:device:WANConnectionDevice:1</deviceType>\n"
            + "                        <friendlyName>Linksys WRT120N (WAN Con)</friendlyName>\n"
            + "                        <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                        <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                        <modelDescription>Internet Access Server (WAN Connection Device)</modelDescription>\n"
            + "                        <modelName>WRT120N WAN Connector</modelName>\n"
            + "                        <modelNumber>v1.0.04</modelNumber>\n"
            + "                        <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                        <serialNumber>JUT00L209293</serialNumber>\n"
            + "                        <UDN>uuid:00000000-0000-0001-0002-98fc11bd6774</UDN>\n"
            + "                        <UPC>494125014380</UPC>\n"
            + "                        <serviceList>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANEthernetLinkConfig:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANEthLinkC1</serviceId>\n"
            + "                                <SCPDURL>/igd_wec.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANEthernetLinkConfig</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANEthernetLinkConfig</eventSubURL>\n"
            + "                            </service>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANPPPConnection:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANPPPConn1</serviceId>\n"
            + "                                <SCPDURL>/igd_wpc.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANPPPConnection</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANPPPConnection</eventSubURL>\n"
            + "                            </service>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANIPConn1</serviceId>\n"
            + "                                <SCPDURL>/igd_wic.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANIPConnection</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANIPConnection</eventSubURL>\n"
            + "                            </service>\n"
            + "                        </serviceList>\n"
            + "                    </device>\n"
            + "                </deviceList>\n"
            + "            </device>\n"
            + "            <device>\n"
            + "                <deviceType>urn:schemas-wifialliance-org:device:WFADevice:1</deviceType>\n"
            + "                <friendlyName>Linksys WRT120N (WFA)</friendlyName>\n"
            + "                <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                <modelDescription>Internet Access Server</modelDescription>\n"
            + "                <modelName>WRT120N</modelName>\n"
            + "                <modelNumber>v1.0.04</modelNumber>\n"
            + "                <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                <serialNumber>JUT00L209293</serialNumber>\n"
            + "                <UDN>uuid:00000000-0000-0001-1000-98fc11bd6774</UDN>\n"
            + "                <UPC>494125014380</UPC>\n"
            + "                <serviceList>\n"
            + "                    <service>\n"
            + "                        <serviceType>urn:schemas-wifialliance-org:service:WFAWLANConfig:1</serviceType>\n"
            + "                        <serviceId>urn:wifialliance-org:serviceId:WFAWLANConfig1</serviceId>\n"
            + "                        <SCPDURL>/igd_WSC_UPnP.xml</SCPDURL>\n"
            + "                        <controlURL>/upnp/control?WFAWLANConfig</controlURL>\n"
            + "                        <eventSubURL>/upnp/event?WFAWLANConfig</eventSubURL>\n"
            + "                    </service>\n"
            + "                </serviceList>\n"
            + "            </device>\n"
            + "        </deviceList>\n"
            + "        <presentationURL>/</presentationURL>\n"
            + "    </device>\n"
            + "</root>";

    // http://homepage.ntlworld.com/tjfs/upnp/igd.xml
    private static final String GOOD_BUFFER_2
            = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/xml\r\n"
            + "\r\n"
            + "<?xml version=\"1.0\"?>\n"
            + "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n"
            + "\n"
            + "<specVersion>\n"
            + "	<major>1</major>\n"
            + "	<minor>0</minor>\n"
            + "</specVersion>\n"
            + "<URLBase>http://192.168.2.1</URLBase>\n"
            + "\n"
            + "<device>\n"
            + "	<deviceType>urn:schemas-upnp-org:device:InternetGatewayDevice:1</deviceType>\n"
            + "	<friendlyName>SMC 7004VWBR Barricade Router</friendlyName>\n"
            + "	<manufacturer>SMC Inc.</manufacturer>\n"
            + "	<manufacturerURL>http://www.smc.com/</manufacturerURL>\n"
            + "	<modelDescription>Internet Gateway Device with UPnP support</modelDescription>\n"
            + "	<modelName>7004VWBR</modelName>\n"
            + "	<modelNumber>1.232</modelNumber>\n"
            + "	<modelURL>http://www.smc.com/</modelURL>\n"
            + "	<serialNumber>A230058142</serialNumber>\n"
            + "	<UDN>uuid:00000000-0000-0001-0000-0004e24c11f4</UDN>\n"
            + "	<UPC>494125014380</UPC>\n"
            + "\n"
            + "	<serviceList>\n"
            + "	<service>\n"
            + "		<serviceType>urn:schemas-microsoft-com:service:OSInfo:1</serviceType>\n"
            + "		<serviceId>urn:microsoft-com:serviceId:OSInfo1</serviceId>\n"
            + "		<SCPDURL>/igd_osf.xml</SCPDURL>\n"
            + "		<controlURL>http://192.168.2.1:5440/upnp/control?OSInfo1</controlURL>\n"
            + "		<eventSubURL>http://192.168.2.1:5440/upnp/event?OSInfo1</eventSubURL>\n"
            + "	</service>\n"
            + "\n"
            + "	<service>\n"
            + "		<serviceType>urn:schemas-upnp-org:service:Layer3Forwarding:1</serviceType>\n"
            + "		<serviceId>urn:upnp-org:serviceId:L3Forwarding1</serviceId>\n"
            + "		<SCPDURL>/igd_l3f.xml</SCPDURL>\n"
            + "		<controlURL>http://192.168.2.1:5440/upnp/control?Layer3Forwarding</controlURL>\n"
            + "		<eventSubURL>http://192.168.2.1:5440/upnp/event?Layer3Forwarding</eventSubURL>\n"
            + "	</service>\n"
            + "	</serviceList>\n"
            + "	\n"
            + "	<deviceList>\n"
            + "	<device>\n"
            + "		<deviceType>urn:schemas-upnp-org:device:WANDevice:1</deviceType>\n"
            + "		<friendlyName>SMC 7004VWBR Barricade Router(WAN)</friendlyName>\n"
            + "		<manufacturer>SMC Inc.</manufacturer>\n"
            + "		<manufacturerURL>http://www.smc.com/</manufacturerURL>\n"
            + "		<modelDescription>Internet Gateway Device with UPnP support (WAN Interface Device)</modelDescription>\n"
            + "		<modelName>7004VWBR WAN Interface</modelName>\n"
            + "		<modelNumber>1.232</modelNumber>\n"
            + "		<modelURL>http://www.smc.com/</modelURL>\n"
            + "		<serialNumber>A230058142</serialNumber>\n"
            + "    	<UDN>uuid:00000000-0000-0001-0001-0004e24c11f4</UDN>\n"
            + "    	<UPC>494125014380</UPC>\n"
            + "		<serviceList>\n"
            + "		<service>\n"
            + "			<serviceType>urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1</serviceType>\n"
            + "			<serviceId>urn:upnp-org:serviceId:WANCommonIFC1</serviceId>\n"
            + "			<SCPDURL>/igd_wcic.xml</SCPDURL>\n"
            + "			<controlURL>http://192.168.2.1:5440/upnp/control?WANCommonInterfaceConfig</controlURL>\n"
            + "			<eventSubURL>http://192.168.2.1:5440/upnp/event?WANCommonInterfaceConfig</eventSubURL>\n"
            + "		</service>\n"
            + "		</serviceList>\n"
            + "		\n"
            + "		<deviceList>\n"
            + "		<device>\n"
            + "			<deviceType>urn:schemas-upnp-org:device:WANConnectionDevice:1</deviceType>\n"
            + "			<friendlyName>SMC 7004VWBR Barricade Router(WAN Con)</friendlyName>\n"
            + "			<manufacturer>SMC Inc.</manufacturer>\n"
            + "			<manufacturerURL>http://www.smc.com/</manufacturerURL>\n"
            + "			<modelDescription>Internet Gateway Device with UPnP support (WAN Connection Device)</modelDescription>\n"
            + "			<modelName>7004VWBR WAN Connector</modelName>\n"
            + "			<modelNumber>1.232</modelNumber>\n"
            + "			<modelURL>http://www.smc.com/</modelURL>\n"
            + "			<serialNumber>A230058142</serialNumber>\n"
            + "        	<UDN>uuid:00000000-0000-0001-0002-0004e24c11f4</UDN>\n"
            + "        	<UPC>494125014380</UPC>\n"
            + "			\n"
            + "			<serviceList>\n"
            + "			<service>\n"
            + "				<serviceType>urn:schemas-upnp-org:service:WANEthernetLinkConfig:1</serviceType>\n"
            + "				<serviceId>urn:upnp-org:serviceId:WANEthLinkC1</serviceId>\n"
            + "				<SCPDURL>/igd_wec.xml</SCPDURL>\n"
            + "				<controlURL>http://192.168.2.1:5440/upnp/control?WANEthernetLinkConfig</controlURL>\n"
            + "				<eventSubURL>http://192.168.2.1:5440/upnp/event?WANEthernetLinkConfig</eventSubURL>\n"
            + "			</service>\n"
            + "\n"
            + "			<service>\n"
            + "				<serviceType>urn:schemas-upnp-org:service:WANPPPConnection:1</serviceType>\n"
            + "				<serviceId>urn:upnp-org:serviceId:WANPPPConn1</serviceId>\n"
            + "				<SCPDURL>/igd_wpc.xml</SCPDURL>\n"
            + "				<controlURL>http://192.168.2.1:5440/upnp/control?WANPPPConnection</controlURL>\n"
            + "				<eventSubURL>http://192.168.2.1:5440/upnp/event?WANPPPConnection</eventSubURL>\n"
            + "			</service>\n"
            + "			<service>\n"
            + "				<serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType>\n"
            + "				<serviceId>urn:upnp-org:serviceId:WANIPConn1</serviceId>\n"
            + "				<SCPDURL>/igd_wic.xml</SCPDURL>\n"
            + "				<controlURL>http://192.168.2.1:5440/upnp/control?WANIPConnection</controlURL>\n"
            + "				<eventSubURL>http://192.168.2.1:5440/upnp/control?WANIPConnection</eventSubURL>\n"
            + "			</service>\n"
            + "			</serviceList>\n"
            + "			\n"
            + "			<presentationURL>/igd_wc.html</presentationURL>\n"
            + "		</device>\n"
            + "		</deviceList>\n"
            + "		\n"
            + "		<presentationURL>/igd_w.html</presentationURL>\n"
            + "	</device>\n"
            + "	</deviceList>\n"
            + "	\n"
            + "	<presentationURL>/</presentationURL>\n"
            + "</device>\n"
            + "</root>\n"
            + "\n"
            + "<!-- \n"
            + "	File		: igd.xml\n"
            + "	Descripte	: internet gateway device description page\n"
            + "	Copyright	: \n"
            + "	Authors		: Pert\n"
            + "	Date		: 03/12/2002\n"
            + "	Last Date	: 03/??/2002\n"
            + "	Version		: v1.0\n"
            + "-->";

    // taken from https://github.com/syncthing/syncthing/issues/1187 -- with modifications
    private static final String GOOD_BUFFER_3
            = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/xml\r\n"
            + "\r\n"
            + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<root>\n"
            + "    <specVersion>\n"
            + "        <major>1</major>\n"
            + "        <minor>0</minor>\n"
            + "    </specVersion>\n"
            + "    <device>\n"
            + "        <deviceType>urn:schemas-upnp-org:device:InternetGatewayDevice:1</deviceType>\n"
            + "        <friendlyName>Linksys WRT120N</friendlyName>\n"
            + "        <manufacturer>Linksys Inc.</manufacturer>\n"
            + "        <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "        <modelDescription>Internet Access Server</modelDescription>\n"
            + "        <modelName>WRT120N</modelName>\n"
            + "        <modelNumber>v1.0.04</modelNumber>\n"
            + "        <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "        <serialNumber>JUT00L209293</serialNumber>\n"
            + "        <UDN>uuid:00000000-0000-0001-0000-98fc11bd6774</UDN>\n"
            + "        <UPC>494125014380</UPC>\n"
            + "        <serviceList>\n"
            + "            <service>\n"
            + "                <serviceType>urn:schemas-upnp-org:service:Layer3Forwarding:1</serviceType>\n"
            + "                <serviceId>urn:upnp-org:serviceId:L3Forwarding1</serviceId>\n"
            + "                <SCPDURL>/igd_l3f.xml</SCPDURL>\n"
            + "                <controlURL>http://192.168.243.1:80/upnp/control?Layer3Forwarding</controlURL>\n"
            + "                <eventSubURL>http://192.168.243.1:80/upnp/event?Layer3Forwarding</eventSubURL>\n"
            + "            </service>\n"
            + "        </serviceList>\n"
            + "        <deviceList>\n"
            + "            <device>\n"
            + "                <deviceType>urn:schemas-upnp-org:device:WANDevice:1</deviceType>\n"
            + "                <friendlyName>Linksys WRT120N (WAN)</friendlyName>\n"
            + "                <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                <modelDescription>Internet Access Server (WAN Interface Device)</modelDescription>\n"
            + "                <modelName>WRT120N WAN Interface</modelName>\n"
            + "                <modelNumber>v1.0.04</modelNumber>\n"
            + "                <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                <serialNumber>JUT00L209293</serialNumber>\n"
            + "                <UDN>uuid:00000000-0000-0001-0001-98fc11bd6774</UDN>\n"
            + "                <UPC>494125014380</UPC>\n"
            + "                <serviceList>\n"
            + "                    <service>\n"
            + "                        <serviceType>urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1</serviceType>\n"
            + "                        <serviceId>urn:upnp-org:serviceId:WANCommonIFC1</serviceId>\n"
            + "                        <SCPDURL>igd_wcic.xml</SCPDURL>\n"
            + "                        <controlURL>http://192.168.243.1:80/upnp/control?WANCommonInterfaceConfig</controlURL>\n"
            + "                        <eventSubURL>http://192.168.243.1:80/upnp/event?WANCommonInterfaceConfig</eventSubURL>\n"
            + "                    </service>\n"
            + "                </serviceList>\n"
            + "                <deviceList>\n"
            + "                    <device>\n"
            + "                        <deviceType>urn:schemas-upnp-org:device:WANConnectionDevice:1</deviceType>\n"
            + "                        <friendlyName>Linksys WRT120N (WAN Con)</friendlyName>\n"
            + "                        <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                        <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                        <modelDescription>Internet Access Server (WAN Connection Device)</modelDescription>\n"
            + "                        <modelName>WRT120N WAN Connector</modelName>\n"
            + "                        <modelNumber>v1.0.04</modelNumber>\n"
            + "                        <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                        <serialNumber>JUT00L209293</serialNumber>\n"
            + "                        <UDN>uuid:00000000-0000-0001-0002-98fc11bd6774</UDN>\n"
            + "                        <UPC>494125014380</UPC>\n"
            + "                        <serviceList>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANEthernetLinkConfig:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANEthLinkC1</serviceId>\n"
            + "                                <SCPDURL>/igd_wec.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANEthernetLinkConfig</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANEthernetLinkConfig</eventSubURL>\n"
            + "                            </service>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANPPPConnection:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANPPPConn1</serviceId>\n"
            + "                                <SCPDURL>/igd_wpc.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANPPPConnection</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANPPPConnection</eventSubURL>\n"
            + "                            </service>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANIPConn1</serviceId>\n"
            + "                                <SCPDURL>/igd_wic.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANIPConnection</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANIPConnection</eventSubURL>\n"
            + "                            </service>\n"
            + "                        </serviceList>\n"
            + "                    </device>\n"
            + "                </deviceList>\n"
            + "            </device>\n"
            + "            <device>\n"
            + "                <deviceType>urn:schemas-wifialliance-org:device:WFADevice:1</deviceType>\n"
            + "                <friendlyName>Linksys WRT120N (WFA)</friendlyName>\n"
            + "                <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                <modelDescription>Internet Access Server</modelDescription>\n"
            + "                <modelName>WRT120N</modelName>\n"
            + "                <modelNumber>v1.0.04</modelNumber>\n"
            + "                <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                <serialNumber>JUT00L209293</serialNumber>\n"
            + "                <UDN>uuid:00000000-0000-0001-1000-98fc11bd6774</UDN>\n"
            + "                <UPC>494125014380</UPC>\n"
            + "                <serviceList>\n"
            + "                    <service>\n"
            + "                        <serviceType>urn:schemas-wifialliance-org:service:WFAWLANConfig:1</serviceType>\n"
            + "                        <serviceId>urn:wifialliance-org:serviceId:WFAWLANConfig1</serviceId>\n"
            + "                        <SCPDURL>/igd_WSC_UPnP.xml</SCPDURL>\n"
            + "                        <controlURL>/upnp/control?WFAWLANConfig</controlURL>\n"
            + "                        <eventSubURL>/upnp/event?WFAWLANConfig</eventSubURL>\n"
            + "                    </service>\n"
            + "                </serviceList>\n"
            + "            </device>\n"
            + "        </deviceList>\n"
            + "        <presentationURL>/</presentationURL>\n"
            + "    </device>\n"
            + "</root>";

    // taken from https://github.com/syncthing/syncthing/issues/1187
    private static final String GOOD_BUFFER_WITH_BAD_RESPONSE_CODE
            = "HTTP/1.1 500 Bad Response\r\n"
            + "Content-Type: text/xml\r\n"
            + "\r\n"
            + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<root>\n"
            + "    <specVersion>\n"
            + "        <major>1</major>\n"
            + "        <minor>0</minor>\n"
            + "    </specVersion>\n"
            + "    <URLBase>http://192.168.243.1</URLBase>\n"
            + "    <device>\n"
            + "        <deviceType>urn:schemas-upnp-org:device:InternetGatewayDevice:1</deviceType>\n"
            + "        <friendlyName>Linksys WRT120N</friendlyName>\n"
            + "        <manufacturer>Linksys Inc.</manufacturer>\n"
            + "        <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "        <modelDescription>Internet Access Server</modelDescription>\n"
            + "        <modelName>WRT120N</modelName>\n"
            + "        <modelNumber>v1.0.04</modelNumber>\n"
            + "        <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "        <serialNumber>JUT00L209293</serialNumber>\n"
            + "        <UDN>uuid:00000000-0000-0001-0000-98fc11bd6774</UDN>\n"
            + "        <UPC>494125014380</UPC>\n"
            + "        <serviceList>\n"
            + "            <service>\n"
            + "                <serviceType>urn:schemas-upnp-org:service:Layer3Forwarding:1</serviceType>\n"
            + "                <serviceId>urn:upnp-org:serviceId:L3Forwarding1</serviceId>\n"
            + "                <SCPDURL>/igd_l3f.xml</SCPDURL>\n"
            + "                <controlURL>http://192.168.243.1:80/upnp/control?Layer3Forwarding</controlURL>\n"
            + "                <eventSubURL>http://192.168.243.1:80/upnp/event?Layer3Forwarding</eventSubURL>\n"
            + "            </service>\n"
            + "        </serviceList>\n"
            + "        <deviceList>\n"
            + "            <device>\n"
            + "                <deviceType>urn:schemas-upnp-org:device:WANDevice:1</deviceType>\n"
            + "                <friendlyName>Linksys WRT120N (WAN)</friendlyName>\n"
            + "                <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                <modelDescription>Internet Access Server (WAN Interface Device)</modelDescription>\n"
            + "                <modelName>WRT120N WAN Interface</modelName>\n"
            + "                <modelNumber>v1.0.04</modelNumber>\n"
            + "                <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                <serialNumber>JUT00L209293</serialNumber>\n"
            + "                <UDN>uuid:00000000-0000-0001-0001-98fc11bd6774</UDN>\n"
            + "                <UPC>494125014380</UPC>\n"
            + "                <serviceList>\n"
            + "                    <service>\n"
            + "                        <serviceType>urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1</serviceType>\n"
            + "                        <serviceId>urn:upnp-org:serviceId:WANCommonIFC1</serviceId>\n"
            + "                        <SCPDURL>/igd_wcic.xml</SCPDURL>\n"
            + "                        <controlURL>http://192.168.243.1:80/upnp/control?WANCommonInterfaceConfig</controlURL>\n"
            + "                        <eventSubURL>http://192.168.243.1:80/upnp/event?WANCommonInterfaceConfig</eventSubURL>\n"
            + "                    </service>\n"
            + "                </serviceList>\n"
            + "                <deviceList>\n"
            + "                    <device>\n"
            + "                        <deviceType>urn:schemas-upnp-org:device:WANConnectionDevice:1</deviceType>\n"
            + "                        <friendlyName>Linksys WRT120N (WAN Con)</friendlyName>\n"
            + "                        <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                        <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                        <modelDescription>Internet Access Server (WAN Connection Device)</modelDescription>\n"
            + "                        <modelName>WRT120N WAN Connector</modelName>\n"
            + "                        <modelNumber>v1.0.04</modelNumber>\n"
            + "                        <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                        <serialNumber>JUT00L209293</serialNumber>\n"
            + "                        <UDN>uuid:00000000-0000-0001-0002-98fc11bd6774</UDN>\n"
            + "                        <UPC>494125014380</UPC>\n"
            + "                        <serviceList>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANEthernetLinkConfig:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANEthLinkC1</serviceId>\n"
            + "                                <SCPDURL>/igd_wec.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANEthernetLinkConfig</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANEthernetLinkConfig</eventSubURL>\n"
            + "                            </service>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANPPPConnection:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANPPPConn1</serviceId>\n"
            + "                                <SCPDURL>/igd_wpc.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANPPPConnection</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANPPPConnection</eventSubURL>\n"
            + "                            </service>\n"
            + "                            <service>\n"
            + "                                <serviceType>urn:schemas-upnp-org:service:WANIPConnection:1</serviceType>\n"
            + "                                <serviceId>urn:upnp-org:serviceId:WANIPConn1</serviceId>\n"
            + "                                <SCPDURL>/igd_wic.xml</SCPDURL>\n"
            + "                                <controlURL>http://192.168.243.1:80/upnp/control?WANIPConnection</controlURL>\n"
            + "                                <eventSubURL>http://192.168.243.1:80/upnp/event?WANIPConnection</eventSubURL>\n"
            + "                            </service>\n"
            + "                        </serviceList>\n"
            + "                    </device>\n"
            + "                </deviceList>\n"
            + "            </device>\n"
            + "            <device>\n"
            + "                <deviceType>urn:schemas-wifialliance-org:device:WFADevice:1</deviceType>\n"
            + "                <friendlyName>Linksys WRT120N (WFA)</friendlyName>\n"
            + "                <manufacturer>Linksys Inc.</manufacturer>\n"
            + "                <manufacturerURL>http://www.linksysbycisco.com/</manufacturerURL>\n"
            + "                <modelDescription>Internet Access Server</modelDescription>\n"
            + "                <modelName>WRT120N</modelName>\n"
            + "                <modelNumber>v1.0.04</modelNumber>\n"
            + "                <modelURL>http://www.linksysbycisco.com/international/</modelURL>\n"
            + "                <serialNumber>JUT00L209293</serialNumber>\n"
            + "                <UDN>uuid:00000000-0000-0001-1000-98fc11bd6774</UDN>\n"
            + "                <UPC>494125014380</UPC>\n"
            + "                <serviceList>\n"
            + "                    <service>\n"
            + "                        <serviceType>urn:schemas-wifialliance-org:service:WFAWLANConfig:1</serviceType>\n"
            + "                        <serviceId>urn:wifialliance-org:serviceId:WFAWLANConfig1</serviceId>\n"
            + "                        <SCPDURL>/igd_WSC_UPnP.xml</SCPDURL>\n"
            + "                        <controlURL>/upnp/control?WFAWLANConfig</controlURL>\n"
            + "                        <eventSubURL>/upnp/event?WFAWLANConfig</eventSubURL>\n"
            + "                    </service>\n"
            + "                </serviceList>\n"
            + "            </device>\n"
            + "        </deviceList>\n"
            + "        <presentationURL>/</presentationURL>\n"
            + "    </device>\n"
            + "</root>";

    @Test
    public void mustProperlyParseGoodBuffer1() throws Exception {
        byte[] buffer = GOOD_BUFFER_1.getBytes("US-ASCII");
        RootUpnpIgdResponse resp = new RootUpnpIgdResponse(new URL("http://fake:80/IGD.xml"), buffer);

        List<ServiceReference> services = resp.getServices();
        assertEquals(6, services.size());

        assertEquals("urn:schemas-upnp-org:service:Layer3Forwarding:1", services.get(0).getServiceType());
        assertEquals(new URL("http://192.168.243.1/igd_l3f.xml"), services.get(0).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?Layer3Forwarding"), services.get(0).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1", services.get(1).getServiceType());
        assertEquals(new URL("http://192.168.243.1/igd_wcic.xml"), services.get(1).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANCommonInterfaceConfig"), services.get(1).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANEthernetLinkConfig:1", services.get(2).getServiceType());
        assertEquals(new URL("http://192.168.243.1/igd_wec.xml"), services.get(2).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANEthernetLinkConfig"), services.get(2).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", services.get(3).getServiceType());
        assertEquals(new URL("http://192.168.243.1/igd_wpc.xml"), services.get(3).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANPPPConnection"), services.get(3).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANIPConnection:1", services.get(4).getServiceType());
        assertEquals(new URL("http://192.168.243.1/igd_wic.xml"), services.get(4).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANIPConnection"), services.get(4).getControlUrl());

        assertEquals("urn:schemas-wifialliance-org:service:WFAWLANConfig:1", services.get(5).getServiceType());
        assertEquals(new URL("http://192.168.243.1/igd_WSC_UPnP.xml"), services.get(5).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1/upnp/control?WFAWLANConfig"), services.get(5).getControlUrl());
    }

    @Test
    public void mustProperlyParseGoodBuffer2() throws Exception {
        byte[] buffer = GOOD_BUFFER_2.getBytes("US-ASCII");
        RootUpnpIgdResponse resp = new RootUpnpIgdResponse(new URL("http://fake:80/IGD.xml"), buffer);

        List<ServiceReference> services = resp.getServices();
        assertEquals(6, services.size());

        assertEquals("urn:schemas-microsoft-com:service:OSInfo:1", services.get(0).getServiceType());
        assertEquals(new URL("http://192.168.2.1/igd_osf.xml"), services.get(0).getScpdUrl());
        assertEquals(new URL("http://192.168.2.1:5440/upnp/control?OSInfo1"), services.get(0).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:Layer3Forwarding:1", services.get(1).getServiceType());
        assertEquals(new URL("http://192.168.2.1/igd_l3f.xml"), services.get(1).getScpdUrl());
        assertEquals(new URL("http://192.168.2.1:5440/upnp/control?Layer3Forwarding"), services.get(1).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1", services.get(2).getServiceType());
        assertEquals(new URL("http://192.168.2.1/igd_wcic.xml"), services.get(2).getScpdUrl());
        assertEquals(new URL("http://192.168.2.1:5440/upnp/control?WANCommonInterfaceConfig"), services.get(2).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANEthernetLinkConfig:1", services.get(3).getServiceType());
        assertEquals(new URL("http://192.168.2.1/igd_wec.xml"), services.get(3).getScpdUrl());
        assertEquals(new URL("http://192.168.2.1:5440/upnp/control?WANEthernetLinkConfig"), services.get(3).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", services.get(4).getServiceType());
        assertEquals(new URL("http://192.168.2.1/igd_wpc.xml"), services.get(4).getScpdUrl());
        assertEquals(new URL("http://192.168.2.1:5440/upnp/control?WANPPPConnection"), services.get(4).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANIPConnection:1", services.get(5).getServiceType());
        assertEquals(new URL("http://192.168.2.1/igd_wic.xml"), services.get(5).getScpdUrl());
        assertEquals(new URL("http://192.168.2.1:5440/upnp/control?WANIPConnection"), services.get(5).getControlUrl());
    }

    @Test
    public void mustProperlyParseGoodBuffer3() throws Exception {
        byte[] buffer = GOOD_BUFFER_3.getBytes("US-ASCII");
        RootUpnpIgdResponse resp = new RootUpnpIgdResponse(new URL("http://fake:80/IGD.xml"), buffer);

        List<ServiceReference> services = resp.getServices();
        assertEquals(6, services.size());

        assertEquals("urn:schemas-upnp-org:service:Layer3Forwarding:1", services.get(0).getServiceType());
        assertEquals(new URL("http://fake:80/igd_l3f.xml"), services.get(0).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?Layer3Forwarding"), services.get(0).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1", services.get(1).getServiceType());
        assertEquals(new URL("http://fake:80/igd_wcic.xml"), services.get(1).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANCommonInterfaceConfig"), services.get(1).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANEthernetLinkConfig:1", services.get(2).getServiceType());
        assertEquals(new URL("http://fake:80/igd_wec.xml"), services.get(2).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANEthernetLinkConfig"), services.get(2).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANPPPConnection:1", services.get(3).getServiceType());
        assertEquals(new URL("http://fake:80/igd_wpc.xml"), services.get(3).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANPPPConnection"), services.get(3).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:WANIPConnection:1", services.get(4).getServiceType());
        assertEquals(new URL("http://fake:80/igd_wic.xml"), services.get(4).getScpdUrl());
        assertEquals(new URL("http://192.168.243.1:80/upnp/control?WANIPConnection"), services.get(4).getControlUrl());

        assertEquals("urn:schemas-wifialliance-org:service:WFAWLANConfig:1", services.get(5).getServiceType());
        assertEquals(new URL("http://fake:80/igd_WSC_UPnP.xml"), services.get(5).getScpdUrl());
        assertEquals(new URL("http://fake:80/upnp/control?WFAWLANConfig"), services.get(5).getControlUrl());
    }
    
    @Test
    public void mustProperlyParseGoodBuffer4() throws Exception {
        byte[] buffer = GOOD_BUFFER_WITHOUT_IGD.getBytes("US-ASCII");
        RootUpnpIgdResponse resp = new RootUpnpIgdResponse(new URL("http://fake:80/IGD.xml"), buffer);

        List<ServiceReference> services = resp.getServices();
        assertEquals(3, services.size());

        assertEquals("urn:schemas-upnp-org:service:ContentDirectory:1", services.get(0).getServiceType());
        assertEquals(new URL("http://fake:80/contentDirectory.xml"), services.get(0).getScpdUrl());
        assertEquals(new URL("http://fake:80/serviceControl"), services.get(0).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:ConnectionManager:1", services.get(1).getServiceType());
        assertEquals(new URL("http://fake:80/connectionManager.xml"), services.get(1).getScpdUrl());
        assertEquals(new URL("http://fake:80/serviceControl"), services.get(1).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:X_MS_MediaReceiverRegistrar:1", services.get(2).getServiceType());
        assertEquals(new URL("http://fake:80/MSMediaReceiverRegistrar.xml"), services.get(2).getScpdUrl());
        assertEquals(new URL("http://fake:80/serviceControl"), services.get(2).getControlUrl());
    }

    public void mustIgnoreHttpError() throws Exception {
        byte[] buffer = GOOD_BUFFER_WITH_BAD_RESPONSE_CODE.getBytes("US-ASCII");
        RootUpnpIgdResponse resp = new RootUpnpIgdResponse(new URL("http://fake:80/IGD.xml"), buffer);
        
        List<ServiceReference> services = resp.getServices();
        assertEquals(3, services.size());

        assertEquals("urn:schemas-upnp-org:service:ContentDirectory:1", services.get(0).getServiceType());
        assertEquals(new URL("http://fake:80/contentDirectory.xml"), services.get(0).getScpdUrl());
        assertEquals(new URL("http://fake:80/serviceControl"), services.get(0).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:ConnectionManager:1", services.get(1).getServiceType());
        assertEquals(new URL("http://fake:80/connectionManager.xml"), services.get(1).getScpdUrl());
        assertEquals(new URL("http://fake:80/serviceControl"), services.get(1).getControlUrl());

        assertEquals("urn:schemas-upnp-org:service:X_MS_MediaReceiverRegistrar:1", services.get(2).getServiceType());
        assertEquals(new URL("http://fake:80/MSMediaReceiverRegistrar.xml"), services.get(2).getScpdUrl());
        assertEquals(new URL("http://fake:80/serviceControl"), services.get(2).getControlUrl());
    }

    @Test
    public void mustNotReturnAnyFaultyServices() throws Exception {
        byte[] buffer = FAULTY_BUFFER.getBytes("US-ASCII");
        RootUpnpIgdResponse resp = new RootUpnpIgdResponse(new URL("http://fake:80/IGD.xml"), buffer);

        List<ServiceReference> services = resp.getServices();
        assertEquals(0, services.size());
    }
}
