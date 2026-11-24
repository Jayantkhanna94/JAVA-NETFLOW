package netflow.main;

import netflow.concurrency.CallableFlowTask;
import netflow.concurrency.FlowConsumer;
import netflow.concurrency.FlowProducer;
import netflow.concurrency.FlowQueue;
import netflow.exception.InvalidFlowException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Milestone4App - Test Harness for Producer-Consumer Pattern and Executor Framework.
 * Demonstrates:
 *   1. Producer-Consumer (Test T13): FlowQueue wait/notify coordination with producer and consumer threads.
 *   2. ExecutorService & Future (Test T14): Fixed thread pool, Callable task submission, Future.get(), and graceful shutdown.
 */
public class Milestone4App {

    public static void main(String[] args) {
        printBanner("NETFLOW: MILESTONE 4 - PRODUCER-CONSUMER & EXECUTOR FRAMEWORK HARNESS");

        // =====================================================================
        // PART 1: PRODUCER-CONSUMER DEMO (Test T13)
        // =====================================================================
        printSectionHeader("--- MILESTONE 4: PRODUCER-CONSUMER TEST (Test T13) ---");
        System.out.println("[Step 1.1] Initializing FlowQueue with bounded capacity of 3...");
        FlowQueue boundedQueue = new FlowQueue(3); // Small capacity to visibly test blocking and wait/notify

        int flowCount = 6;
        FlowProducer producer = new FlowProducer(boundedQueue, flowCount);
        FlowConsumer consumer = new FlowConsumer(boundedQueue, flowCount);

        Thread producerThread = new Thread(producer, "ProducerThread-1");
        Thread consumerThread = new Thread(consumer, "ConsumerThread-1");

        System.out.printf("[Step 1.2] Starting producer and consumer threads (%d flows to transfer)...%n%n", flowCount);
        producerThread.start();
        consumerThread.start();

        try {
            // Wait for both worker threads to complete their transfer
            producerThread.join();
            consumerThread.join();
            System.out.printf("%n[SUCCESS] Producer and consumer threads completed successfully.%n");
            System.out.printf("  Remaining Queue Size: %d (Expected: 0)%n", boundedQueue.size());

        } catch (InterruptedException e) {
            System.err.println("[FAIL] Thread join interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        System.out.println();

        // =====================================================================
        // PART 2: EXECUTOR SERVICE & FUTURE DEMO (Test T14)
        // =====================================================================
        printSectionHeader("--- MILESTONE 4: EXECUTOR SERVICE TEST (Test T14) ---");

        System.out.println("[Step 2.1] Creating ExecutorService via Executors.newFixedThreadPool(2)...");
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            System.out.println("[Step 2.2] Instantiating valid TrafficFlow record for asynchronous processing...");
            TrafficFlow sampleFlow = new TrafficFlow(
                    "FLW-EXEC-501",
                    "192.168.1.100",
                    "10.0.0.50",
                    NetworkProtocol.TCP,
                    54321,
                    443,
                    12500L,
                    15728640L, // 15 MB
                    5000L,
                    System.currentTimeMillis()
            );
            System.out.println("  Flow Details: " + sampleFlow);

            System.out.println("\n[Step 2.3] Submitting CallableFlowTask to ExecutorService...");
            CallableFlowTask task = new CallableFlowTask(sampleFlow);
            Future<Long> futureResult = executor.submit(task);

            System.out.println("  Task submitted. Waiting on Future.get() to retrieve processed bytes...");
            Long processedBytes = futureResult.get(); // Blocking until computation finishes

            System.out.println("\n[SUCCESS] Future Result (Processed Bytes): " + String.format("%,d", processedBytes) + " bytes");
            System.out.printf("  Verification: %s%n",
                    processedBytes.equals(sampleFlow.getByteCount())
                            ? "MATCH (Callable calculated exact flow volume)"
                            : "MISMATCH");

        } catch (InvalidFlowException e) {
            System.err.println("[FAIL] Invalid flow parameters: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("[FAIL] Executor task execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("[FAIL] Execution exception thrown by Callable: " + e.getCause().getMessage());
        } finally {
            // =================================================================
            // Graceful Shutdown & Await Termination
            // =================================================================
            System.out.println("\n[Step 2.4] Initiating graceful shutdown of ExecutorService...");
            executor.shutdown();

            try {
                boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
                if (terminated) {
                    System.out.println("[SUCCESS] ExecutorService shut down cleanly. All thread resources released.");
                } else {
                    System.err.println("[WARN] ExecutorService did not terminate within timeout; forcing shutdown.");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("[FAIL] Await termination interrupted: " + e.getMessage());
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        printBanner("MILESTONE 4 CONCURRENCY TESTS COMPLETED SUCCESSFULLY");
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
