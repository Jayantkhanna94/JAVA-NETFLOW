package netflow.concurrency;

import netflow.model.TrafficFlow;

/**
 * A Runnable task that simulates the parsing and processing of an individual
 * {@link TrafficFlow} telemetry record and thread-safely updates the shared
 * {@link MonitoringStats} critical section.
 */
public class FlowProcessingTask implements Runnable {

    // Injected shared resource and flow unit of work
    private final MonitoringStats stats;
    private final TrafficFlow flow;

    /**
     * Constructs a task targeting a specific flow and shared stats accumulator.
     *
     * @param stats The shared {@link MonitoringStats} resource.
     * @param flow The specific {@link TrafficFlow} to process.
     */
    public FlowProcessingTask(MonitoringStats stats, TrafficFlow flow) {
        if (stats == null) {
            throw new IllegalArgumentException("MonitoringStats cannot be null");
        }
        if (flow == null) {
            throw new IllegalArgumentException("TrafficFlow cannot be null");
        }
        this.stats = stats;
        this.flow = flow;
    }

    /**
     * Executes the simulated telemetry processing:
     *   1. Simulates latency via Thread.sleep(50).
     *   2. Atomically updates shared MonitoringStats inside its synchronized critical section.
     *   3. Prints diagnostic output with the executing Thread's name and processed flow ID.
     */
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            // Simulate processing / inspection latency
            Thread.sleep(50);

            // Update shared stats inside synchronized critical section
            long bytes = (flow.getByteCount() != null) ? flow.getByteCount() : 0L;
            stats.addFlowStats(bytes);

            // Thread identification logging
            System.out.printf("[%s] Successfully processed flow: %s (%,d bytes)%n",
                    threadName, flow.getFlowId(), bytes);

        } catch (InterruptedException e) {
            System.err.printf("[%s] Processing interrupted for flow: %s%n", threadName, flow.getFlowId());
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    public MonitoringStats getStats() {
        return stats;
    }

    public TrafficFlow getFlow() {
        return flow;
    }
}
