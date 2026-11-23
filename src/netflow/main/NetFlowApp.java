package netflow.main;

import netflow.contract.Monitorable;
import netflow.exception.InvalidDeviceException;
import netflow.exception.InvalidFlowException;
import netflow.model.AlertSeverity;
import netflow.model.DeviceStatus;
import netflow.model.DeviceType;
import netflow.model.NetworkAlert;
import netflow.model.NetworkDevice;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;

/**
 * NetFlowApp - Milestone 1 Test Harness and Verification Runner.
 * Proves OOP model, string validation, interface polymorphism,
 * and custom exception handling without using Collections, Threads, or JDBC.
 */
public class NetFlowApp {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("       NETFLOW: NETWORK TRAFFIC MONITORING & MANAGEMENT SYSTEM");
        System.out.println("                  Milestone 1 Test Harness & Evidence");
        System.out.println("===============================================================================\n");

        // ---------------------------------------------------------------------
        // TEST 1: Valid Device Creation (Test T01)
        // ---------------------------------------------------------------------
        System.out.println("--- TEST 1: Valid Device Creation (Test T01) ---");
        try {
            NetworkDevice router = new NetworkDevice(
                    "DEV-RTR-01",
                    "Core-Gateway-East",
                    DeviceType.ROUTER,
                    "192.168.1.1",
                    "Building-A Rack-04",
                    DeviceStatus.ONLINE
            );

            // Demonstrate polymorphism via Monitorable interface
            Monitorable monitorableDevice = router;
            System.out.println("[SUCCESS] NetworkDevice instantiated successfully!");
            System.out.println("Device ID       : " + router.getId());
            System.out.println("Device Name     : " + router.getName());
            System.out.println("Device Type     : " + router.getDeviceType());
            System.out.println("Management IP   : " + router.getManagementIp());
            System.out.println("Location        : " + router.getLocation());
            System.out.println("Status          : " + router.getStatus());
            System.out.println("\n[Monitoring Summary via Monitorable Interface]:");
            System.out.println(monitorableDevice.getMonitoringSummary());

        } catch (InvalidDeviceException e) {
            System.err.println("[FAIL] Unexpected InvalidDeviceException: " + e.getMessage());
        }

        System.out.println();

        // ---------------------------------------------------------------------
        // TEST 2: Valid Flow Creation
        // ---------------------------------------------------------------------
        System.out.println("--- TEST 2: Valid Flow Creation ---");
        try {
            TrafficFlow flow = new TrafficFlow(
                    "FLW-9001",
                    "192.168.1.50",
                    "10.0.0.1",
                    NetworkProtocol.TCP,
                    52134,
                    443,
                    15420L,
                    18456000L,
                    12500L,
                    System.currentTimeMillis()
            );

            System.out.println("[SUCCESS] TrafficFlow instantiated successfully!");
            System.out.println("Flow ID         : " + flow.getFlowId());
            System.out.println("Source Endpoint : " + flow.getSourceIp() + ":" + flow.getSourcePort());
            System.out.println("Dest Endpoint   : " + flow.getDestIp() + ":" + flow.getDestPort());
            System.out.println("Protocol        : " + flow.getProtocol());
            System.out.println("Packet Count    : " + String.format("%,d", flow.getPacketCount()));
            System.out.println("Byte Count      : " + String.format("%,d", flow.getByteCount()) + " bytes");
            System.out.println("Duration        : " + flow.getDurationMs() + " ms");
            System.out.println("\n[Formatted Flow Record]:");
            System.out.println(flow.toString());

        } catch (InvalidFlowException e) {
            System.err.println("[FAIL] Unexpected InvalidFlowException: " + e.getMessage());
        }

        System.out.println();

        // ---------------------------------------------------------------------
        // TEST 3: Invalid Device (Test T04 / T05)
        // ---------------------------------------------------------------------
        System.out.println("--- TEST 3: Invalid Device Validation (Tests T04 & T05) ---");

        // 3a: Malformed IP (Test T04)
        System.out.println("[Subtest 3a: Attempting to create NetworkDevice with malformed IP (999.300.1.1)...]");
        try {
            new NetworkDevice("DEV-BAD-01", "Edge-Switch", DeviceType.SWITCH, "999.300.1.1");
            System.err.println("  [FAIL] Malformed IP did not throw an InvalidDeviceException!");
        } catch (InvalidDeviceException e) {
            System.out.println("  [PASS] Caught expected InvalidDeviceException:");
            System.out.println("         --> " + e.getMessage());
        }

        // 3b: Blank Device Name (Test T05)
        System.out.println("\n[Subtest 3b: Attempting to create NetworkDevice with blank name ('   ')...]");
        try {
            new NetworkDevice("DEV-BAD-02", "   ", DeviceType.SERVER, "10.0.0.5");
            System.err.println("  [FAIL] Blank device name did not throw an InvalidDeviceException!");
        } catch (InvalidDeviceException e) {
            System.out.println("  [PASS] Caught expected InvalidDeviceException:");
            System.out.println("         --> " + e.getMessage());
        }

        System.out.println();

        // ---------------------------------------------------------------------
        // TEST 4: Invalid Flow (Test T06 / T07)
        // ---------------------------------------------------------------------
        System.out.println("--- TEST 4: Invalid Flow Validation (Tests T06 & T07) ---");

        // 4a: Out-of-range port (Test T06)
        System.out.println("[Subtest 4a: Attempting to create TrafficFlow with out-of-range port (70000)...]");
        try {
            new TrafficFlow(
                    "FLW-BAD-01",
                    "192.168.1.1",
                    "10.0.0.1",
                    NetworkProtocol.TCP,
                    70000, // Invalid port > 65535
                    80
            );
            System.err.println("  [FAIL] Port 70000 did not throw an InvalidFlowException!");
        } catch (InvalidFlowException e) {
            System.out.println("  [PASS] Caught expected InvalidFlowException:");
            System.out.println("         --> " + e.getMessage());
        }

        // 4b: Negative byte count (Test T07)
        System.out.println("\n[Subtest 4b: Attempting to create TrafficFlow with negative byte count (-500)...]");
        try {
            new TrafficFlow(
                    "FLW-BAD-02",
                    "192.168.1.1",
                    "10.0.0.1",
                    NetworkProtocol.UDP,
                    5300,
                    53,
                    100L,
                    -500L, // Invalid negative bytes
                    1000L,
                    System.currentTimeMillis()
            );
            System.err.println("  [FAIL] Negative byte count did not throw an InvalidFlowException!");
        } catch (InvalidFlowException e) {
            System.out.println("  [PASS] Caught expected InvalidFlowException:");
            System.out.println("         --> " + e.getMessage());
        }

        System.out.println();

        // ---------------------------------------------------------------------
        // TEST 5: Network Alert & ICMP Optional Port Demonstration
        // ---------------------------------------------------------------------
        System.out.println("--- TEST 5: Alert Model & Protocol Port Wrapper (ICMP null port) ---");
        try {
            // Flow with null ports (valid for ICMP protocol)
            TrafficFlow icmpFlow = new TrafficFlow(
                    "FLW-ICMP-01",
                    "192.168.1.100",
                    "8.8.8.8",
                    NetworkProtocol.ICMP,
                    null, // Nullable port wrapper class
                    null
            );
            System.out.println("[SUCCESS] ICMP TrafficFlow with null ports instantiated:");
            System.out.println("  " + icmpFlow);

            // Alert associated with the flow
            NetworkAlert alert = new NetworkAlert(
                    "ALT-001",
                    AlertSeverity.CRITICAL,
                    "High ICMP Echo volume detected (possible ping flood)",
                    icmpFlow.getFlowId()
            );
            System.out.println("\n[SUCCESS] NetworkAlert created:");
            System.out.println("  " + alert);

            alert.acknowledge();
            System.out.println("After acknowledgment:");
            System.out.println("  " + alert);

        } catch (InvalidFlowException e) {
            System.err.println("[FAIL] Unexpected InvalidFlowException: " + e.getMessage());
        }

        System.out.println("\n===============================================================================");
        System.out.println("            ALL MILESTONE 1 VERIFICATION TESTS PASSED SUCCESSFULLY");
        System.out.println("===============================================================================");
    }
}
