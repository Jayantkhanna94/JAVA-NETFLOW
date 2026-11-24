package netflow.main;

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

import java.util.List;

/**
 * Milestone2App - Test Harness for Collections and Record Management.
 * Demonstrates:
 *   1. DeviceService: CRUD operations, HashMap lookups, and HashSet duplicate IP prevention (T08, T09).
 *   2. TrafficFlowService: ArrayList repository, Stream-based protocol filtering, and Comparator sorting (T10).
 */
public class Milestone2App {

    public static void main(String[] args) {
        printBanner("NETFLOW: MILESTONE 2 - COLLECTIONS & RECORD MANAGEMENT TEST HARNESS");

        // =====================================================================
        // PART 1: DEVICE SERVICE (HashMap & HashSet Operations)
        // =====================================================================
        printSectionHeader("--- MILESTONE 2: DEVICE SERVICE TEST (T08 & T09) ---");

        DeviceService deviceService = new DeviceService();

        try {
            // 1.1 Adding 3 Valid Devices
            System.out.println("[Step 1.1] Registering 3 initial valid network devices...");

            NetworkDevice router = new NetworkDevice(
                    "DEV-RTR-01",
                    "Core-Router-East",
                    DeviceType.ROUTER,
                    "192.168.1.1",
                    "Building-A Rack-01",
                    DeviceStatus.ONLINE
            );

            NetworkDevice switchDev = new NetworkDevice(
                    "DEV-SW-01",
                    "Access-Switch-Floor1",
                    DeviceType.SWITCH,
                    "192.168.1.2",
                    "Building-A Rack-02",
                    DeviceStatus.ONLINE
            );

            NetworkDevice server = new NetworkDevice(
                    "DEV-SRV-01",
                    "Auth-LDAP-Server",
                    DeviceType.SERVER,
                    "192.168.1.10",
                    "Datacenter Row-03",
                    DeviceStatus.ONLINE
            );

            deviceService.addDevice(router);
            deviceService.addDevice(switchDev);
            deviceService.addDevice(server);

            System.out.println("  [SUCCESS] 3 devices added. Current Device Count: " + deviceService.getDeviceCount());
            for (NetworkDevice dev : deviceService.getAllDevices()) {
                System.out.println("    * " + dev.getMonitoringSummary());
            }

            // 1.2 Duplicate Management IP Prevention (HashSet Test T09)
            System.out.println("\n[Step 1.2] Attempting to add 4th device with DUPLICATE management IP (192.168.1.1)...");
            NetworkDevice duplicateIpDevice = new NetworkDevice(
                    "DEV-FW-01",
                    "Perimeter-Firewall",
                    DeviceType.FIREWALL,
                    "192.168.1.1", // Same IP as Core-Router-East
                    "Security-Zone-1",
                    DeviceStatus.ONLINE
            );

            try {
                deviceService.addDevice(duplicateIpDevice);
                System.err.println("  [FAIL] Duplicate IP was accepted without throwing DuplicateRecordException!");
            } catch (DuplicateRecordException e) {
                System.out.println("  [PASS] Successfully caught DuplicateRecordException:");
                System.out.println("         --> " + e.getMessage());
            }

            // 1.3 Fast O(1) Lookup by ID (HashMap Test T08)
            System.out.println("\n[Step 1.3] Retrieving device by ID 'DEV-SW-01' via HashMap...");
            NetworkDevice retrieved = deviceService.getDevice("DEV-SW-01");
            System.out.println("  [SUCCESS] Found device: " + retrieved.getName());
            System.out.println("  Summary: " + retrieved.getMonitoringSummary());

            // 1.4 Removing Device & IP Release
            System.out.println("\n[Step 1.4] Removing device 'DEV-SRV-01'...");
            NetworkDevice removed = deviceService.removeDevice("DEV-SRV-01");
            System.out.println("  [SUCCESS] Removed device: " + removed.getName() + " (IP: " + removed.getManagementIp() + ")");
            System.out.println("  Remaining Device Count: " + deviceService.getDeviceCount());
            System.out.println("  Verifying IP '192.168.1.10' is released: " + (!deviceService.containsIp("192.168.1.10") ? "YES (Released)" : "NO (Still present)"));

        } catch (InvalidDeviceException e) {
            System.err.println("[FAIL] Unexpected InvalidDeviceException: " + e.getMessage());
        } catch (RecordNotFoundException e) {
            System.err.println("[FAIL] Unexpected RecordNotFoundException: " + e.getMessage());
        }

        System.out.println();

        // =====================================================================
        // PART 2: TRAFFIC FLOW SERVICE (List, Streams & Comparator)
        // =====================================================================
        printSectionHeader("--- MILESTONE 2: TRAFFIC FLOW SERVICE TEST (T10) ---");

        TrafficFlowService flowService = new TrafficFlowService();

        try {
            // 2.1 Adding 5 Flows with diverse protocols and volumes
            System.out.println("[Step 2.1] Registering 5 telemetry flow records (2 TCP, 2 UDP, 1 ICMP)...");

            TrafficFlow flow1 = new TrafficFlow(
                    "FLW-TCP-01",
                    "192.168.1.50",
                    "10.0.0.1",
                    NetworkProtocol.TCP,
                    52134,
                    80,
                    1200L,
                    1500000L, // 1.5 MB
                    2500L,
                    System.currentTimeMillis()
            );

            TrafficFlow flow2 = new TrafficFlow(
                    "FLW-TCP-02",
                    "192.168.1.55",
                    "10.0.0.2",
                    NetworkProtocol.TCP,
                    54321,
                    443,
                    8500L,
                    12000000L, // 12.0 MB
                    6000L,
                    System.currentTimeMillis()
            );

            TrafficFlow flow3 = new TrafficFlow(
                    "FLW-UDP-01",
                    "192.168.1.60",
                    "8.8.8.8",
                    NetworkProtocol.UDP,
                    53000,
                    53,
                    300L,
                    45000L, // 45 KB
                    500L,
                    System.currentTimeMillis()
            );

            TrafficFlow flow4 = new TrafficFlow(
                    "FLW-UDP-02",
                    "192.168.1.70",
                    "10.0.0.5",
                    NetworkProtocol.UDP,
                    60000,
                    5004,
                    25000L,
                    35000000L, // 35.0 MB (Highest)
                    15000L,
                    System.currentTimeMillis()
            );

            TrafficFlow flow5 = new TrafficFlow(
                    "FLW-ICMP-01",
                    "192.168.1.80",
                    "192.168.1.1",
                    NetworkProtocol.ICMP,
                    null, // ICMP has no ports
                    null,
                    50L,
                    3200L, // 3.2 KB
                    100L,
                    System.currentTimeMillis()
            );

            flowService.addFlow(flow1);
            flowService.addFlow(flow2);
            flowService.addFlow(flow3);
            flowService.addFlow(flow4);
            flowService.addFlow(flow5);

            System.out.println("  [SUCCESS] 5 flows registered. Total Flow Count: " + flowService.getFlowCount());

            // 2.2 Protocol Filtering with Stream API
            System.out.println("\n[Step 2.2] Filtering flows by protocol (TCP) using Stream API...");
            List<TrafficFlow> tcpFlows = flowService.getFlowsByProtocol(NetworkProtocol.TCP);
            System.out.println("  Found " + tcpFlows.size() + " TCP flow(s):");
            for (TrafficFlow f : tcpFlows) {
                System.out.printf("    - [%s] %s:%d -> %s:%d | %s | %,d bytes%n",
                        f.getFlowId(), f.getSourceIp(), f.getSourcePort(),
                        f.getDestIp(), f.getDestPort(), f.getProtocol(), f.getByteCount());
            }

            // 2.3 Top High-Volume Flows via Comparator
            System.out.println("\n[Step 2.3] Slicing TOP 3 highest-volume flows via Comparator (descending byte count)...");
            List<TrafficFlow> top3 = flowService.getTopHighVolumeFlows(3);
            int rank = 1;
            for (TrafficFlow f : top3) {
                double mb = f.getByteCount() / (1024.0 * 1024.0);
                System.out.printf("    #%d [%s] %-5s | %,12d bytes (%6.2f MB) | %s -> %s%n",
                        rank++, f.getFlowId(), f.getProtocol(), f.getByteCount(), mb,
                        f.getSourceIp(), f.getDestIp());
            }

        } catch (InvalidFlowException e) {
            System.err.println("[FAIL] Unexpected InvalidFlowException: " + e.getMessage());
        } catch (DuplicateRecordException e) {
            System.err.println("[FAIL] Unexpected DuplicateRecordException: " + e.getMessage());
        }

        printBanner("MILESTONE 2 VERIFICATION TESTS COMPLETED SUCCESSFULLY");
    }

    private static void printBanner(String title) {
        String bar = "=".repeat(79);
        System.out.println(bar);
        System.out.println("  " + title);
        System.out.println(bar);
    }

    private static void printSectionHeader(String section) {
        System.out.println(section);
        System.out.println("-".repeat(79));
    }
}
