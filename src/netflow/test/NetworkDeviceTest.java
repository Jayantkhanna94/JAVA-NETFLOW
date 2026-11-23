package netflow.test;

import netflow.contract.Monitorable;
import netflow.exception.InvalidDeviceException;
import netflow.model.DeviceStatus;
import netflow.model.DeviceType;
import netflow.model.NetworkDevice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link NetworkDevice} creation, string validation,
 * Monitorable interface implementation, and exception handling.
 */
class NetworkDeviceTest {

    @Test
    @DisplayName("T01: Successful device creation with valid parameters")
    void testValidDeviceCreation() throws InvalidDeviceException {
        NetworkDevice device = new NetworkDevice(
                "DEV-RTR-01",
                "Core-Gateway-East",
                DeviceType.ROUTER,
                "192.168.1.1",
                "Building-A Rack-04",
                DeviceStatus.ONLINE
        );

        assertNotNull(device);
        assertEquals("DEV-RTR-01", device.getId());
        assertEquals("Core-Gateway-East", device.getName());
        assertEquals(DeviceType.ROUTER, device.getDeviceType());
        assertEquals("192.168.1.1", device.getManagementIp());
        assertEquals("Building-A Rack-04", device.getLocation());
        assertEquals(DeviceStatus.ONLINE, device.getStatus());

        // Verify Monitorable interface implementation
        assertInstanceOf(Monitorable.class, device);
        String summary = device.getMonitoringSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("DEV-RTR-01"));
        assertTrue(summary.contains("Core-Gateway-East"));
        assertTrue(summary.contains("192.168.1.1"));
        assertTrue(summary.contains("ONLINE"));
    }

    @Test
    @DisplayName("T04: Creation with malformed management IP throws InvalidDeviceException")
    void testInvalidIpThrowsException() {
        // Octet > 255
        InvalidDeviceException ex1 = assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("DEV-01", "Edge-Router", DeviceType.ROUTER, "999.300.1.1")
        );
        assertTrue(ex1.getMessage().contains("Invalid IPv4 address format"));

        // Incomplete IP string
        InvalidDeviceException ex2 = assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("DEV-02", "Edge-Router", DeviceType.ROUTER, "192.168.1")
        );
        assertTrue(ex2.getMessage().contains("Invalid IPv4 address format"));

        // Alphabetic characters in IP
        assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("DEV-03", "Edge-Router", DeviceType.ROUTER, "192.168.1.abc")
        );
    }

    @Test
    @DisplayName("T05: Creation with empty or blank device name throws InvalidDeviceException")
    void testEmptyNameThrowsException() {
        // Whitespace only
        InvalidDeviceException ex1 = assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("DEV-01", "   ", DeviceType.SERVER, "10.0.0.1")
        );
        assertTrue(ex1.getMessage().contains("Device name cannot be null or empty"));

        // Empty string
        InvalidDeviceException ex2 = assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("DEV-02", "", DeviceType.SERVER, "10.0.0.1")
        );
        assertTrue(ex2.getMessage().contains("Device name cannot be null or empty"));

        // Null string
        InvalidDeviceException ex3 = assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("DEV-03", null, DeviceType.SERVER, "10.0.0.1")
        );
        assertTrue(ex3.getMessage().contains("Device name cannot be null or empty"));
    }

    @Test
    @DisplayName("Creation with empty or blank device ID throws InvalidDeviceException")
    void testEmptyIdThrowsException() {
        assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("", "Switch-01", DeviceType.SWITCH, "10.0.0.2")
        );
        assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice(null, "Switch-01", DeviceType.SWITCH, "10.0.0.2")
        );
    }

    @Test
    @DisplayName("Creation with null deviceType throws InvalidDeviceException")
    void testNullDeviceTypeThrowsException() {
        assertThrows(InvalidDeviceException.class, () ->
                new NetworkDevice("DEV-01", "Switch-01", null, "10.0.0.2")
        );
    }
}
