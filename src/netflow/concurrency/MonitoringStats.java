package netflow.concurrency;

/**
 * Shared monitoring statistics resource tracking cumulative processed telemetry.
 * Uses synchronized critical sections and intrinsic object locking to ensure
 * thread-safe mutation and visibility across concurrent threads.
 */
public class MonitoringStats {

    // Shared mutable state protected by intrinsic lock
    private int totalFlowsProcessed;
    private long totalBytesProcessed;

    public MonitoringStats() {
        this.totalFlowsProcessed = 0;
        this.totalBytesProcessed = 0L;
    }

    /**
     * Critical section: Atomically increments flow counter and accumulates byte count.
     * Synchronized on 'this' intrinsic monitor to prevent data races.
     *
     * @param bytes Number of bytes in the processed flow.
     */
    public synchronized void addFlowStats(long bytes) {
        this.totalFlowsProcessed++;
        this.totalBytesProcessed += bytes;
    }

    /**
     * Synchronized getter ensuring atomic read and memory visibility across threads.
     *
     * @return Total number of flows processed so far.
     */
    public synchronized int getTotalFlowsProcessed() {
        return totalFlowsProcessed;
    }

    /**
     * Synchronized getter ensuring atomic 64-bit read and memory visibility across threads.
     *
     * @return Cumulative bytes processed across all threads.
     */
    public synchronized long getTotalBytesProcessed() {
        return totalBytesProcessed;
    }

    /**
     * Resets the cumulative statistics (thread-safe).
     */
    public synchronized void reset() {
        this.totalFlowsProcessed = 0;
        this.totalBytesProcessed = 0L;
    }

    @Override
    public synchronized String toString() {
        return String.format("MonitoringStats [Flows Processed: %,d | Total Bytes: %,d]",
                totalFlowsProcessed, totalBytesProcessed);
    }
}
