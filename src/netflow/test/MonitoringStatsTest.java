package netflow.test;

import netflow.concurrency.MonitoringStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link MonitoringStats} synchronized critical sections,
 * thread-safety, and lack of race conditions under heavy concurrent load (Test T11).
 */
class MonitoringStatsTest {

    @Test
    @DisplayName("T11: Concurrent threads updating MonitoringStats with zero lost updates")
    void testConcurrentStatsAccumulation() throws InterruptedException {
        MonitoringStats stats = new MonitoringStats();

        int threadCount = 30; // 30 concurrent threads
        long bytesPerThread = 2500L;
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Thread worker = new Thread(() -> stats.addFlowStats(bytesPerThread), "StatsWorker-" + i);
            threads.add(worker);
            worker.start();
        }

        // Wait for all threads to terminate
        for (Thread worker : threads) {
            worker.join();
        }

        // Verify mathematical totals
        long expectedTotalBytes = threadCount * bytesPerThread;
        int expectedTotalFlows = threadCount;

        assertEquals(expectedTotalFlows, stats.getTotalFlowsProcessed(),
                "Total flows processed should exactly match the number of worker threads.");
        assertEquals(expectedTotalBytes, stats.getTotalBytesProcessed(),
                "Total bytes processed should match the mathematical total without race conditions.");
    }

    @Test
    @DisplayName("Resetting stats restores counters to zero")
    void testResetStats() {
        MonitoringStats stats = new MonitoringStats();
        stats.addFlowStats(5000L);
        assertEquals(1, stats.getTotalFlowsProcessed());
        assertEquals(5000L, stats.getTotalBytesProcessed());

        stats.reset();
        assertEquals(0, stats.getTotalFlowsProcessed());
        assertEquals(0L, stats.getTotalBytesProcessed());
    }
}
