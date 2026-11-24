package netflow.main;

import netflow.concurrency.FlowProcessingTask;
import netflow.concurrency.MonitoringStats;
import netflow.exception.InvalidFlowException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Milestone3App - Test Harness for Concurrency, Threads, and Synchronization.
 * Demonstrates:
 *   1. Thread Creation & Lifecycle: Instantiating Runnable tasks and calling Thread.start().
 *   2. Synchronization & Critical Section (Test T11): Multiple threads mutating shared MonitoringStats without race conditions.
 *   3. Thread Interruption Handling (Test T12): Handling InterruptedException gracefully.
 *   4. Thread Coordination: Calling Thread.join() to synchronize main execution.
 */
public class Milestone3App {

    public static void main(String[] args) {
        printBanner("NETFLOW: MILESTONE 3 - MULTITHREADING & SYNCHRONIZATION TEST HARNESS");

        // ---------------------------------------------------------------------
        // STEP 1: Initialize Shared State (MonitoringStats)
        // ---------------------------------------------------------------------
        printSectionHeader("--- MILESTONE 3: MULTITHREADING TEST (T11 & T12) ---");
        System.out.println("[Step 1] Initializing shared MonitoringStats resource...");
        MonitoringStats sharedStats = new MonitoringStats();
        System.out.println("  Initial State: " + sharedStats);

        // ---------------------------------------------------------------------
        // STEP 2: Setup Test Data (10 TrafficFlow objects, 1000 bytes each)
        // ---------------------------------------------------------------------
        System.out.println("\n[Step 2] Generating 10 synthetic TrafficFlow records (1,000 bytes each)...");
        List<TrafficFlow> testFlows = new ArrayList<>();
        int flowCount = 10;
        long bytesPerFlow = 1000L;
        long expectedTotalBytes = flowCount * bytesPerFlow; // 10,000 bytes

        try {
            for (int i = 1; i <= flowCount; i++) {
                String flowId = String.format("FLW-SIM-%02d", i);
                TrafficFlow flow = new TrafficFlow(
                        flowId,
                        "192.168.1." + (10 + i),
                        "10.0.0." + (20 + i),
                        (i % 2 == 0) ? NetworkProtocol.TCP : NetworkProtocol.UDP,
                        5000 + i,
                        80,
                        25L,
                        bytesPerFlow,
                        150L,
                        System.currentTimeMillis()
                );
                testFlows.add(flow);
            }
            System.out.printf("  [SUCCESS] 10 flows generated. Expected Total Bytes: %,d bytes%n", expectedTotalBytes);

        } catch (InvalidFlowException e) {
            System.err.println("[FAIL] Failed to construct test flows: " + e.getMessage());
            return;
        }

        // ---------------------------------------------------------------------
        // STEP 3: Launch 10 Worker Threads (Test T11)
        // ---------------------------------------------------------------------
        System.out.println("\n[Step 3] Launching 10 concurrent worker threads via Thread.start()...");
        List<Thread> workerThreads = new ArrayList<>();

        for (int i = 0; i < testFlows.size(); i++) {
            TrafficFlow flow = testFlows.get(i);
            FlowProcessingTask task = new FlowProcessingTask(sharedStats, flow);
            Thread thread = new Thread(task, "FlowWorker-" + (i + 1));

            // Explicitly call start() to initiate thread execution (T11)
            thread.start();
            workerThreads.add(thread);
        }

        // ---------------------------------------------------------------------
        // STEP 4: Thread Interruption Demonstration (Test T12)
        // ---------------------------------------------------------------------
        System.out.println("\n[Step 4] Launching extra thread and triggering Thread.interrupt() (Test T12)...");
        try {
            TrafficFlow interruptedFlow = new TrafficFlow(
                    "FLW-INTERRUPT-99",
                    "192.168.1.99",
                    "10.0.0.99",
                    NetworkProtocol.TCP,
                    9999,
                    443,
                    50L,
                    5000L,
                    200L,
                    System.currentTimeMillis()
            );

            FlowProcessingTask interruptedTask = new FlowProcessingTask(sharedStats, interruptedFlow);
            Thread interruptibleThread = new Thread(interruptedTask, "FlowWorker-Interruptible");

            // Start thread then immediately interrupt it while in Thread.sleep(50)
            interruptibleThread.start();
            interruptibleThread.interrupt();

            // Wait for interrupted thread to terminate
            interruptibleThread.join();
            System.out.println("  [SUCCESS] Interrupted thread terminated cleanly.");

        } catch (InvalidFlowException | InterruptedException e) {
            System.err.println("[FAIL] Interruption test failed: " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // STEP 5: Join Worker Threads
        // ---------------------------------------------------------------------
        System.out.println("\n[Step 5] Waiting for all 10 worker threads to complete via Thread.join()...");
        for (Thread worker : workerThreads) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted while waiting for worker: " + worker.getName());
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("  [SUCCESS] All 10 worker threads have completed execution.");

        // ---------------------------------------------------------------------
        // STEP 6: Verify Synchronization Integrity
        // ---------------------------------------------------------------------
        printSectionHeader("--- SYNCHRONIZATION INTEGRITY VERIFICATION ---");
        int actualFlows = sharedStats.getTotalFlowsProcessed();
        long actualBytes = sharedStats.getTotalBytesProcessed();

        System.out.printf("  Expected Flows Processed : %,d%n", flowCount);
        System.out.printf("  Actual Flows Processed   : %,d%n", actualFlows);
        System.out.printf("  Expected Total Bytes     : %,d bytes%n", expectedTotalBytes);
        System.out.printf("  Actual Total Bytes       : %,d bytes%n", actualBytes);

        if (actualFlows == flowCount && actualBytes == expectedTotalBytes) {
            System.out.println("\n  >>> VERIFICATION RESULT: PASS <<<");
            System.out.println("  Zero data loss or race conditions detected!");
            System.out.println("  The 'synchronized' critical section correctly protected shared state across all threads.");
        } else {
            System.err.println("\n  >>> VERIFICATION RESULT: FAIL <<<");
            System.err.println("  Discrepancy detected between expected and actual stats!");
        }

        printBanner("MILESTONE 3 MULTITHREADING TESTS COMPLETED SUCCESSFULLY");
    }

    private static void printBanner(String title) {
        String bar = "=".repeat(79);
        System.out.println(bar);
        System.out.println("  " + title);
        System.out.println(bar);
    }

    private static void printSectionHeader(String section) {
        System.out.println("\n" + section);
        System.out.println("-".repeat(79));
    }
}
