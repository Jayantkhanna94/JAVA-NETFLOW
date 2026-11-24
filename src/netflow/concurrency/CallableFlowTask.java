package netflow.concurrency;

import netflow.model.TrafficFlow;

import java.util.concurrent.Callable;

/**
 * A {@link Callable} task designed for submission to an {@link java.util.concurrent.ExecutorService}.
 * Simulates asynchronous inspection of a {@link TrafficFlow} and returns its cumulative
 * byte count for aggregation via a {@link java.util.concurrent.Future}.
 */
public class CallableFlowTask implements Callable<Long> {

    private final TrafficFlow flow;

    public CallableFlowTask(TrafficFlow flow) {
        if (flow == null) {
            throw new IllegalArgumentException("TrafficFlow cannot be null");
        }
        this.flow = flow;
    }

    /**
     * Computes the flow's byte volume asynchronously.
     *
     * @return The byte count of the processed traffic flow.
     * @throws Exception if processing is interrupted.
     */
    @Override
    public Long call() throws Exception {
        String threadName = Thread.currentThread().getName();

        // Simulate analytical compute / DPI inspection latency
        Thread.sleep(30);

        long bytes = (flow.getByteCount() != null) ? flow.getByteCount() : 0L;
        System.out.printf("[%s] Callable processed: %s | Bytes: %,d%n",
                threadName, flow.getFlowId(), bytes);

        return bytes;
    }

    public TrafficFlow getFlow() {
        return flow;
    }
}
