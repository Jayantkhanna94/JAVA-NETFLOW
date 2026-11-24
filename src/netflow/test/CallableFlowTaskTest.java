package netflow.test;

import netflow.concurrency.CallableFlowTask;
import netflow.exception.InvalidFlowException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link CallableFlowTask} execution via ExecutorService
 * and Future result retrieval (Test T14).
 */
class CallableFlowTaskTest {

    @Test
    @DisplayName("T14: Future.get() exactly matches the flow byte count from CallableFlowTask")
    void testCallableFlowTaskWithExecutor() throws InvalidFlowException, ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            long expectedBytes = 10485760L; // 10 MB
            TrafficFlow flow = new TrafficFlow(
                    "FLW-CALL-01",
                    "192.168.1.50",
                    "10.0.0.1",
                    NetworkProtocol.TCP,
                    52134,
                    443,
                    8000L,
                    expectedBytes,
                    3000L,
                    System.currentTimeMillis()
            );

            CallableFlowTask task = new CallableFlowTask(flow);
            Future<Long> future = executor.submit(task);

            assertNotNull(future);
            Long actualBytes = future.get(); // Blocking get

            assertEquals(expectedBytes, actualBytes,
                    "Future result must match the flow's byte count exactly.");

        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS),
                    "ExecutorService should terminate cleanly.");
        }
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException when flow is null")
    void testNullFlowConstructorThrows() {
        assertThrows(IllegalArgumentException.class, () -> new CallableFlowTask(null));
    }
}
