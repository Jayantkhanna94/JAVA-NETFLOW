package netflow.main;

import netflow.dao.DeviceDAO;
import netflow.dao.TrafficFlowDAO;
import netflow.exception.InvalidDeviceException;
import netflow.exception.InvalidFlowException;
import netflow.model.DeviceStatus;
import netflow.model.DeviceType;
import netflow.model.NetworkDevice;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;
import netflow.concurrency.FlowQueue;
import netflow.concurrency.FlowProducer;
import netflow.concurrency.FlowConsumer;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class NetFlowApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DeviceDAO deviceDAO = new DeviceDAO();
    private static final TrafficFlowDAO flowDAO = new TrafficFlowDAO();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n========================================================");
            System.out.println("   NETFLOW: NETWORK TRAFFIC MONITORING SYSTEM");
            System.out.println("========================================================");
            System.out.println("1. Register New Network Device");
            System.out.println("2. View a Network Device by ID");
            System.out.println("3. Simulate & Save Traffic Flow");
            System.out.println("4. View All Saved Traffic Flows");
            System.out.println("5. Run Multithreading Demo");
            System.out.println("0. Exit Application");
            System.out.println("========================================================");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        registerDevice();
                        break;
                    case "2":
                        viewDevice();
                        break;
                    case "3":
                        simulateTrafficFlow();
                        break;
                    case "4":
                        viewAllFlows();
                        break;
                    case "5":
                        runConcurrencyDemo();
                        break;
                    case "0":
                        running = false;
                        System.out.println("Exiting NetFlow System. Goodbye!");
                        break;
                    default:
                        System.out.println("[ERROR] Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] An unexpected error occurred: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void registerDevice() {
        System.out.println("\n--- REGISTER NEW DEVICE ---");
        System.out.print("Enter Device ID (e.g., DEV-RTR-01): ");
        String id = scanner.nextLine();
        System.out.print("Enter Device Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter IP Address: ");
        String ip = scanner.nextLine();

        try {
            // Moved inside try block to handle InvalidDeviceException
            NetworkDevice device = new NetworkDevice(id, name, DeviceType.ROUTER, ip, "Main Datacenter",
                    DeviceStatus.ONLINE);
            deviceDAO.addDevice(device);
            System.out.println("[SUCCESS] Device registered in database successfully!");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to save device: " + e.getMessage());
        }
    }

    private static void viewDevice() {
        System.out.println("\n--- VIEW DEVICE ---");
        System.out.print("Enter Device ID to search: ");
        String id = scanner.nextLine();

        try {
            NetworkDevice device = deviceDAO.getDevice(id);
            System.out.println("[RESULT] " + device.getMonitoringSummary());
        } catch (Exception e) {
            System.out.println("[ERROR] Device not found or error occurred: " + e.getMessage());
        }
    }

    private static void simulateTrafficFlow() {
        System.out.println("\n--- SIMULATE TRAFFIC FLOW ---");
        System.out.print("Enter Source IP (e.g., 192.168.1.10): ");
        String srcIp = scanner.nextLine();
        System.out.print("Enter Destination IP (e.g., 8.8.8.8): ");
        String destIp = scanner.nextLine();
        System.out.print("Enter Bytes Transferred: ");
        long bytes = Long.parseLong(scanner.nextLine());

        String flowId = "FLW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try {
            // Moved inside try block to handle InvalidFlowException
            TrafficFlow flow = new TrafficFlow(flowId, srcIp, destIp, NetworkProtocol.TCP, 443, 443, 150L, bytes, 1200L,
                    System.currentTimeMillis());
            flowDAO.addFlow(flow);
            System.out.println("[SUCCESS] Traffic Flow '" + flowId + "' saved to database!");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to save flow: " + e.getMessage());
        }
    }

    private static void viewAllFlows() {
        System.out.println("\n--- SAVED TRAFFIC FLOWS ---");
        try {
            List<TrafficFlow> flows = flowDAO.getAllFlows();
            if (flows.isEmpty()) {
                System.out.println("No flows found in the database.");
            } else {
                for (TrafficFlow flow : flows) {
                    System.out.println("Flow ID: " + flow.getFlowId() + " | " + flow.getSourceIp() + " -> "
                            + flow.getDestIp() + " | Bytes: " + flow.getByteCount());
                }
                System.out.println("Total Flows: " + flows.size());
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Could not fetch flows: " + e.getMessage());
        }
    }

    private static void runConcurrencyDemo() {
        System.out.println("\n--- RUNNING MULTITHREADING DEMO (PRODUCER-CONSUMER) ---");
        try {
            FlowQueue queue = new FlowQueue(5);
            Thread producer = new Thread(new FlowProducer(queue), "Data-Collector");
            Thread consumer = new Thread(new FlowConsumer(queue), "Data-Analyzer");

            producer.start();
            consumer.start();

            producer.join();
            consumer.join();
            System.out.println("[SUCCESS] Multithreading demo completed.");
        } catch (Exception e) {
            System.out.println("[ERROR] Concurrency error: " + e.getMessage());
        }
    }
}