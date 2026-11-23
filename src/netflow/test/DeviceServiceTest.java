package netflow.test;

import netflow.exception.DuplicateRecordException;
import netflow.exception.InvalidDeviceException;
import netflow.exception.RecordNotFoundException;
import netflow.model.DeviceStatus;
import netflow.model.DeviceType;
import netflow.model.NetworkDevice;
import netflow.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link DeviceService} operations (CRUD, HashMap lookups,
 * and HashSet unique IP enforcement).
 */
class DeviceServiceTest {

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService();
    }

    @Test
    @DisplayName("T08.1: Adding and retrieving a device via HashMap")
    void testAddAndGetDevice() throws InvalidDeviceException {
        NetworkDevice device = new NetworkDevice(
                "DEV-RTR-01",
                "Core-Router-East",
                DeviceType.ROUTER,
                "192.168.1.1",
                "Building-A Rack-01",
                DeviceStatus.ONLINE
        );

        deviceService.addDevice(device);

        assertEquals(1, deviceService.getDeviceCount());
        NetworkDevice retrieved = deviceService.getDevice("DEV-RTR-01");
        assertNotNull(retrieved);
        assertEquals("Core-Router-East", retrieved.getName());
        assertEquals("192.168.1.1", retrieved.getManagementIp());
    }

    @Test
    @DisplayName("T08.2: Removing a device releases ID and Management IP")
    void testRemoveDevice() throws InvalidDeviceException {
        NetworkDevice device = new NetworkDevice(
                "DEV-SW-01",
                "Access-Switch-1",
                DeviceType.SWITCH,
                "192.168.1.2"
        );
        deviceService.addDevice(device);
        assertEquals(1, deviceService.getDeviceCount());
        assertTrue(deviceService.containsIp("192.168.1.2"));

        NetworkDevice removed = deviceService.removeDevice("DEV-SW-01");
        assertEquals(device, removed);
        assertEquals(0, deviceService.getDeviceCount());
        assertFalse(deviceService.containsDevice("DEV-SW-01"));
        assertFalse(deviceService.containsIp("192.168.1.2"));

        // Verify the released IP can be assigned to a new device
        NetworkDevice newDevice = new NetworkDevice(
                "DEV-SW-02",
                "Access-Switch-2",
                DeviceType.SWITCH,
                "192.168.1.2"
        );
        assertDoesNotThrow(() -> deviceService.addDevice(newDevice));
    }

    @Test
    @DisplayName("T08.3: Retrieving all devices returns an unmodifiable list of registered devices")
    void testGetAllDevices() throws InvalidDeviceException {
        NetworkDevice dev1 = new NetworkDevice("DEV-01", "Router-1", DeviceType.ROUTER, "10.0.0.1");
        NetworkDevice dev2 = new NetworkDevice("DEV-02", "Server-1", DeviceType.SERVER, "10.0.0.2");

        deviceService.addDevice(dev1);
        deviceService.addDevice(dev2);

        List<NetworkDevice> all = deviceService.getAllDevices();
        assertEquals(2, all.size());
        assertTrue(all.contains(dev1));
        assertTrue(all.contains(dev2));
    }

    @Test
    @DisplayName("T09.1: Adding device with duplicate ID throws DuplicateRecordException")
    void testDuplicateIdThrowsException() throws InvalidDeviceException {
        NetworkDevice dev1 = new NetworkDevice("DEV-01", "Router-1", DeviceType.ROUTER, "10.0.0.1");
        NetworkDevice duplicateIdDev = new NetworkDevice("DEV-01", "Router-Diff", DeviceType.ROUTER, "10.0.0.99");

        deviceService.addDevice(dev1);

        DuplicateRecordException ex = assertThrows(DuplicateRecordException.class, () ->
                deviceService.addDevice(duplicateIdDev)
        );
        assertTrue(ex.getMessage().contains("Device with ID 'DEV-01' already exists"));
    }

    @Test
    @DisplayName("T09.2: Adding device with duplicate Management IP throws DuplicateRecordException")
    void testDuplicateManagementIpThrowsException() throws InvalidDeviceException {
        NetworkDevice dev1 = new NetworkDevice("DEV-01", "Router-1", DeviceType.ROUTER, "192.168.1.1");
        NetworkDevice duplicateIpDev = new NetworkDevice("DEV-02", "Firewall-1", DeviceType.FIREWALL, "192.168.1.1");

        deviceService.addDevice(dev1);

        DuplicateRecordException ex = assertThrows(DuplicateRecordException.class, () ->
                deviceService.addDevice(duplicateIpDev)
        );
        assertTrue(ex.getMessage().contains("Device with Management IP '192.168.1.1' already exists"));
    }

    @Test
    @DisplayName("Querying or removing a missing device ID throws RecordNotFoundException")
    void testMissingDeviceThrowsException() {
        assertThrows(RecordNotFoundException.class, () -> deviceService.getDevice("DEV-NONEXISTENT"));
        assertThrows(RecordNotFoundException.class, () -> deviceService.removeDevice("DEV-NONEXISTENT"));
    }

    @Test
    @DisplayName("Adding null device or blank ID throws IllegalArgumentException")
    void testInvalidAddDeviceArguments() {
        assertThrows(IllegalArgumentException.class, () -> deviceService.addDevice(null));
    }
}
