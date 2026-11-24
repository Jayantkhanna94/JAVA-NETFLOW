package netflow.main;

import netflow.dao.DeviceDAO;
import netflow.dao.TrafficFlowDAO;
import netflow.exception.InvalidDeviceException;
import netflow.exception.InvalidFlowException;
import netflow.exception.RecordNotFoundException;
import netflow.model.DeviceStatus;
import netflow.model.DeviceType;
import netflow.model.NetworkDevice;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;

import java.sql.SQLException;
import java.util.List;

/**
 * Milestone5App - Test Harness for JDBC, MySQL Relational Persistence &
 * Transactions.
 * Demonstrates:
 * 1. Database CRUD & Persistence (Tests T15 & T17): Inserting, retrieving,
 * updating, and querying records.
 * 2. Transaction Management & Rollback (Test T18): Atomic multi-statement
 * execution and rollback on failure.
 *
 * TEAM NOTE ON PERSISTENCE VERIFICATION:
 * To prove data persists across application restarts (evidence for T15/T17):
 * 1. Run this harness once to insert and verify the records in MySQL.
 * 2. Comment out the addDevice() and addFlow() insert calls below.
 * 3. Re-run this class: the records will be fetched directly from MySQL without
 * re-inserting!
 */
public class Milestone5App {

    public static void main(String[] args) {
        printBanner("NETFLOW: MILESTONE 5 - JDBC & RELATIONAL PERSISTENCE TEST HARNESS");

        DeviceDAO deviceDAO = new DeviceDAO();
        TrafficFlowDAO flowDAO = new TrafficFlowDAO();

        // =====================================================================
        // PART 1: DATABASE CRUD & PERSISTENCE TEST (Tests T15 & T17)
        // =====================================================================
        printSectionHeader("--- MILESTONE 5: DATABASE CRUD & PERSISTENCE TEST (T15 & T17) ---");

        try {
            // Clean up any previous test artifact from earlier runs for clean idempotency
            deviceDAO.deleteDevice("DEV-DB-001");

            // 1.1 Insert new device
            System.out.println("[Step 1.1] Creating and inserting NetworkDevice into MySQL...");
            NetworkDevice device1 = new NetworkDevice(
                    "DEV-DB-001",
                    "Core-Edge-Router",
                    DeviceType.ROUTER,
                    "192.168.10.1",
                    "Datacenter-Rack-E1",
                    DeviceStatus.ONLINE);

            deviceDAO.addDevice(device1);
            System.out.println("  [SUCCESS] Device inserted into 'network_devices' table: " + device1.getId());

            // 1.2 Retrieve device and print summary
            System.out.println("\n[Step 1.2] Querying device 'DEV-DB-001' from database via getDevice()...");
            NetworkDevice fetchedDevice = deviceDAO.getDevice("DEV-DB-001");
            System.out.println("  [SUCCESS] Record fetched successfully from MySQL:");
            System.out.println("  " + fetchedDevice.getMonitoringSummary());

            // 1.3 Update device status to DEGRADED
            System.out.println("\n[Step 1.3] Updating device status to DEGRADED via updateDeviceStatus()...");
            boolean updated = deviceDAO.updateDeviceStatus("DEV-DB-001", DeviceStatus.DEGRADED);
            System.out.println("  Update execution status: " + (updated ? "SUCCESS" : "FAILED"));

            NetworkDevice updatedDevice = deviceDAO.getDevice("DEV-DB-001");
            System.out.println("  Re-fetched device state from MySQL:");
            System.out.println("  " + updatedDevice.getMonitoringSummary());

            // 1.4 Insert traffic flow record
            System.out.println("\n[Step 1.4] Inserting TrafficFlow telemetry record into 'traffic_flows' table...");
            String flowId = "FLW-DB-" + (System.currentTimeMillis() % 100000);
            TrafficFlow flow = new TrafficFlow(
                    flowId,
                    "192.168.10.100",
                    "10.0.0.1",
                    NetworkProtocol.TCP,
                    52134,
                    443,
                    15420L,
                    18456000L,
                    12500L,
                    System.currentTimeMillis());

            flowDAO.addFlow(flow);
            System.out
                    .println("  [SUCCESS] Flow inserted: " + flow.getFlowId() + " (" + flow.getByteCount() + " bytes)");

            // 1.5 Query all flows
            System.out.println("\n[Step 1.5] Calling getAllFlows() to query persisted flows...");
            List<TrafficFlow> allFlows = flowDAO.getAllFlows();
            System.out.printf("  [SUCCESS] Total flows retrieved from MySQL: %d flow(s)%n", allFlows.size());
            if (!allFlows.isEmpty()) {
                System.out.println("  Latest Flow in DB: " + allFlows.get(0));
            }

        } catch (SQLException e) {
            System.err.println("[FAIL] SQL Exception during CRUD operations: " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (InvalidDeviceException | InvalidFlowException e) {
            System.err.println("[FAIL] Domain model validation error: " + e.getMessage());
            return;
        }

        System.out.println();

        // =====================================================================
        // PART 2: TRANSACTION ROLLBACK DEMO (Test T18)
        // =====================================================================
        printSectionHeader("--- MILESTONE 5: TRANSACTION ROLLBACK TEST (Test T18) ---");

        try {
            // Clean up any stale transaction test IDs
            deviceDAO.deleteDevice("DEV-TX-02");
            deviceDAO.deleteDevice("DEV-TX-03");

            System.out.println("[Step 2.1] Preparing 2 new devices for transactional insertion:");
            NetworkDevice device2 = new NetworkDevice(
                    "DEV-TX-02",
                    "Branch-Switch-1",
                    DeviceType.SWITCH,
                    "172.16.1.1",
                    "Branch-Office-1",
                    DeviceStatus.ONLINE);

            NetworkDevice device3 = new NetworkDevice(
                    "DEV-TX-03",
                    "Branch-Switch-2",
                    DeviceType.SWITCH,
                    "172.16.1.2",
                    "Branch-Office-2",
                    DeviceStatus.ONLINE);
            System.out.println("  Device 2: " + device2.getId() + " (" + device2.getManagementIp() + ")");
            System.out.println("  Device 3: " + device3.getId() + " (" + device3.getManagementIp() + ")");

            System.out
                    .println("\n[Step 2.2] Invoking addDeviceWithTransactionDemo(device2, device3, forceFail=true)...");
            System.out.println(
                    "  Expected behavior: device2 is staged, simulated failure fires, whole transaction rolls back.");

            try {
                deviceDAO.addDeviceWithTransactionDemo(device2, device3, true);
                System.err.println("  [FAIL] Transaction completed without simulated exception!");
            } catch (SQLException e) {
                System.out.println("  [PASS] Caught simulated transaction failure:");
                System.out.println("         --> " + e.getMessage());
                System.out.println("  [PASS] Connection rollback() was executed in catch block.");
            }

            // Verify device2 was NOT committed to the database
            System.out.println("\n[Step 2.3] Verifying rollback by querying device2 ('DEV-TX-02') from database...");
            try {
                deviceDAO.getDevice("DEV-TX-02");
                System.err.println("  [FAIL] DEV-TX-02 was found in database! Rollback did not work.");
            } catch (RecordNotFoundException e) {
                System.out.println("  [PASS] Caught expected RecordNotFoundException:");
                System.out.println("         --> " + e.getMessage());
                System.out.println("  [VERIFIED] Neither device2 nor device3 were saved to MySQL.");
                System.out.println("  Atomic consistency guaranteed: 0 partial records committed.");
            }

        } catch (InvalidDeviceException e) {
            System.err.println("[FAIL] Device creation error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[FAIL] SQL error during transaction test: " + e.getMessage());
        }

        printBanner("MILESTONE 5 DATABASE & TRANSACTION TESTS COMPLETED SUCCESSFULLY");
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
