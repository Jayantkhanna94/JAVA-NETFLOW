package netflow.test;

import netflow.exception.DuplicateRecordException;
import netflow.exception.InvalidDeviceException;
import netflow.exception.InvalidFlowException;
import netflow.exception.RecordNotFoundException;
import netflow.model.DeviceStatus;
import netflow.model.DeviceType;
import netflow.model.NetworkDevice;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;
import netflow.service.DeviceService;
import netflow.service.TrafficFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Milestone 2 Collection-based Service Layer operations.
 */
class ServiceTest {

    private DeviceService deviceService;
    private TrafficFlowService flowService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService();
        flowService = new TrafficFlowService();
    }

    // =========================================================================
    // DeviceService Tests
    // =========================================================================

    @Test
    @DisplayName("DeviceService: Successfully add, get, and retrieve all devices")
    void testAddAndGetDevice() throws InvalidDeviceException {
        NetworkDevice dev1 = new NetworkDevice("DEV-01", "Gateway-1", DeviceType.ROUTER, "192.168.1.1");
        NetworkDevice dev2 = new NetworkDevice("DEV-02", "Switch-1", DeviceType.SWITCH, "192.168.1.2");

        deviceService.addDevice(dev1);
        deviceService.addDevice(dev2);

        assertEquals(2, deviceService.getDeviceCount());
        assertEquals(dev1, deviceService.getDevice("DEV-01"));
        assertEquals(dev2, deviceService.getDevice("DEV-02"));

        List<NetworkDevice> all = deviceService.getAllDevices();
        assertEquals(2, all.size());
        assertTrue(all.contains(dev1));
        assertTrue(all.contains(dev2));
    }

    @Test
    @DisplayName("DeviceService: Duplicate ID throws DuplicateRecordException")
    void testDuplicateDeviceIdThrows() throws InvalidDeviceException {
        NetworkDevice dev1 = new NetworkDevice("DEV-01", "Gateway-1", DeviceType.ROUTER, "192.168.1.1");
        NetworkDevice devDuplicateId = new NetworkDevice("DEV-01", "Gateway-Diff", DeviceType.ROUTER, "10.0.0.1");

        deviceService.addDevice(dev1);
        DuplicateRecordException ex = assertThrows(DuplicateRecordException.class, () ->
                deviceService.addDevice(devDuplicateId)
        );
        assertTrue(ex.getMessage().contains("Device with ID 'DEV-01' already exists"));
    }

    @Test
    @DisplayName("DeviceService: Duplicate Management IP throws DuplicateRecordException")
    void testDuplicateManagementIpThrows() throws InvalidDeviceException {
        NetworkDevice dev1 = new NetworkDevice("DEV-01", "Gateway-1", DeviceType.ROUTER, "192.168.1.1");
        NetworkDevice devDuplicateIp = new NetworkDevice("DEV-02", "Gateway-2", DeviceType.FIREWALL, "192.168.1.1");

        deviceService.addDevice(dev1);
        DuplicateRecordException ex = assertThrows(DuplicateRecordException.class, () ->
                deviceService.addDevice(devDuplicateIp)
        );
        assertTrue(ex.getMessage().contains("Device with Management IP '192.168.1.1' already exists"));
    }

    @Test
    @DisplayName("DeviceService: Querying missing device throws RecordNotFoundException")
    void testGetMissingDeviceThrows() {
        assertThrows(RecordNotFoundException.class, () -> deviceService.getDevice("NON-EXISTENT"));
    }

    @Test
    @DisplayName("DeviceService: Removing device releases ID and Management IP")
    void testRemoveDevice() throws InvalidDeviceException {
        NetworkDevice dev1 = new NetworkDevice("DEV-01", "Gateway-1", DeviceType.ROUTER, "192.168.1.1");
        deviceService.addDevice(dev1);

        NetworkDevice removed = deviceService.removeDevice("DEV-01");
        assertEquals(dev1, removed);
        assertEquals(0, deviceService.getDeviceCount());
        assertFalse(deviceService.containsDevice("DEV-01"));
        assertFalse(deviceService.containsIp("192.168.1.1"));

        // Now we should be able to re-register the same IP without conflict
        NetworkDevice newDev = new NetworkDevice("DEV-NEW", "Gateway-New", DeviceType.ROUTER, "192.168.1.1");
        assertDoesNotThrow(() -> deviceService.addDevice(newDev));
    }

    // =========================================================================
    // TrafficFlowService Tests
    // =========================================================================

    @Test
    @DisplayName("TrafficFlowService: Add, retrieve, and duplicate check")
    void testFlowAddAndDuplicate() throws InvalidFlowException {
        TrafficFlow f1 = new TrafficFlow("F1", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 1000, 80);
        flowService.addFlow(f1);

        assertEquals(1, flowService.getFlowCount());
        assertEquals(f1, flowService.getFlow("F1"));

        TrafficFlow fDuplicate = new TrafficFlow("F1", "10.0.0.3", "10.0.0.4", NetworkProtocol.UDP, 2000, 53);
        assertThrows(DuplicateRecordException.class, () -> flowService.addFlow(fDuplicate));
        assertThrows(RecordNotFoundException.class, () -> flowService.getFlow("F-MISSING"));
    }

    @Test
    @DisplayName("TrafficFlowService: Filter by NetworkProtocol")
    void testFilterByProtocol() throws InvalidFlowException {
        TrafficFlow fTcp1 = new TrafficFlow("F-TCP-1", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 1000, 80);
        TrafficFlow fTcp2 = new TrafficFlow("F-TCP-2", "10.0.0.1", "10.0.0.3", NetworkProtocol.TCP, 1001, 443);
        TrafficFlow fUdp = new TrafficFlow("F-UDP-1", "10.0.0.1", "10.0.0.4", NetworkProtocol.UDP, 1002, 53);

        flowService.addFlow(fTcp1);
        flowService.addFlow(fTcp2);
        flowService.addFlow(fUdp);

        List<TrafficFlow> tcpFlows = flowService.getFlowsByProtocol(NetworkProtocol.TCP);
        assertEquals(2, tcpFlows.size());
        assertTrue(tcpFlows.contains(fTcp1));
        assertTrue(tcpFlows.contains(fTcp2));

        List<TrafficFlow> udpFlows = flowService.getFlowsByProtocol(NetworkProtocol.UDP);
        assertEquals(1, udpFlows.size());
        assertTrue(udpFlows.contains(fUdp));

        List<TrafficFlow> icmpFlows = flowService.getFlowsByProtocol(NetworkProtocol.ICMP);
        assertTrue(icmpFlows.isEmpty());
    }

    @Test
    @DisplayName("TrafficFlowService: Sort descending by byteCount with Comparator")
    void testTopHighVolumeFlows() throws InvalidFlowException {
        TrafficFlow low = new TrafficFlow("F-LOW", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 1000, 80, 10L, 500L, 100L, 1000L);
        TrafficFlow med = new TrafficFlow("F-MED", "10.0.0.1", "10.0.0.3", NetworkProtocol.TCP, 1001, 80, 50L, 50000L, 200L, 1000L);
        TrafficFlow high = new TrafficFlow("F-HIGH", "10.0.0.1", "10.0.0.4", NetworkProtocol.TCP, 1002, 80, 200L, 1000000L, 500L, 1000L);

        flowService.addFlow(med);
        flowService.addFlow(low);
        flowService.addFlow(high);

        List<TrafficFlow> top2 = flowService.getTopHighVolumeFlows(2);
        assertEquals(2, top2.size());
        assertEquals("F-HIGH", top2.get(0).getFlowId());
        assertEquals("F-MED", top2.get(1).getFlowId());

        List<TrafficFlow> top10 = flowService.getTopHighVolumeFlows(10);
        assertEquals(3, top10.size());
        assertEquals("F-HIGH", top10.get(0).getFlowId());
        assertEquals("F-MED", top10.get(1).getFlowId());
        assertEquals("F-LOW", top10.get(2).getFlowId());
    }
}
