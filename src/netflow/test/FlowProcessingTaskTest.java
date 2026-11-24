package netflow.test;

import netflow.concurrency.FlowProcessingTask;
import netflow.concurrency.MonitoringStats;
import netflow.exception.InvalidFlowException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link FlowProcessingTask} lifecycle and thread interruption handling (Test T12).
 */
class FlowProcessingTaskTest {

    @Test
    @DisplayName("T12: Interrupted thread cleanly catches InterruptedException without crashing")
    void testThreadInterruptionHandledSafely() throws InvalidFlowException, InterruptedException {
        MonitoringStats stats = new MonitoringStats();
        TrafficFlow flow = new TrafficFlow(
                "FLW-INTR-01",
                "192.168.1.10",
                "10.0.0.1",
                NetworkProtocol.TCP,
                5000,
                80,
                100L,
                15000L,
                1000L,
                System.currentTimeMillis()
        );

        FlowProcessingTask task = new FlowProcessingTask(stats, flow);
        Thread worker = new Thread(task, "InterruptibleWorker");

        worker.start();
        // Immediately interrupt while in Thread.sleep(50)
        worker.interrupt();

        // Join should complete gracefully without throwing an uncaught exception
        assertDoesNotThrow(() -> worker.join(2000));
        assertFalse(worker.isAlive(), "Worker thread should terminate cleanly after interruption.");

        // An interrupted flow should not increment the shared statistics
        assertEquals(0, stats.getTotalFlowsProcessed(),
                "Interrupted task must not record stats.");
        assertEquals(0L, stats.getTotalBytesProcessed(),
                "Interrupted task must not record bytes.");
    }

    @Test
    @DisplayName("Normal execution of FlowProcessingTask updates MonitoringStats")
    void testNormalTaskExecution() throws InvalidFlowException, InterruptedException {
        MonitoringStats stats = new MonitoringStats();
        TrafficFlow flow = new TrafficFlow(
                "FLW-NORM-01",
                "192.168.1.20",
                "10.0.0.2",
                NetworkProtocol.UDP,
                5300,
                53,
                50L,
                8000L,
                500L,
                System.currentTimeMillis()
        );

        FlowProcessingTask task = new FlowProcessingTask(stats, flow);
        Thread worker = new Thread(task, "NormalWorker");
        worker.start();
        worker.join();

        assertEquals(1, stats.getTotalFlowsProcessed());
        assertEquals(8000L, stats.getTotalBytesProcessed());
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException for null arguments")
    void testConstructorNullValidation() throws InvalidFlowException {
        MonitoringStats stats = new MonitoringStats();
        TrafficFlow flow = new TrafficFlow("F1", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 80, 80);

        assertThrows(IllegalArgumentException.class, () -> new FlowProcessingTask(null, flow));
        assertThrows(IllegalArgumentException.class, () -> new FlowProcessingTask(stats, null));
    }
}
