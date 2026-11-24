package netflow.concurrency;

import netflow.model.TrafficFlow;

/**
 * Consumer Runnable that continuously dequeues {@link TrafficFlow} records
 * from a bounded {@link FlowQueue} and processes them.
 */
public class FlowConsumer implements Runnable {

    private final FlowQueue queue;
    private final int flowCount;

    /**
     * Default constructor consuming 8 flow records.
     */
    public FlowConsumer(FlowQueue queue) {
        this(queue, 8);
    }

    /**
     * Parameterized constructor specifying flow consumption quantity.
     *
     * @param queue The bounded queue to consume flows from.
     * @param flowCount Total number of flows to consume.
     */
    public FlowConsumer(FlowQueue queue, int flowCount) {
        if (queue == null) {
            throw new IllegalArgumentException("FlowQueue cannot be null");
        }
        if (flowCount <= 0) {
            throw new IllegalArgumentException("Flow count must be > 0");
        }
        this.queue = queue;
        this.flowCount = flowCount;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            for (int i = 1; i <= flowCount; i++) {
                // Dequeue flow record (blocks if empty)
                TrafficFlow flow = queue.consume();

                // Simulate deep packet inspection / analysis delay
                Thread.sleep(70);

                System.out.printf("[%s] Processed flow telemetry: %s [%s %s:%s -> %s:%s]%n",
                        threadName, flow.getFlowId(), flow.getProtocol(),
                        flow.getSourceIp(), flow.getSourcePort(),
                        flow.getDestIp(), flow.getDestPort());
            }
            System.out.printf("[%s] Consumer finished consuming %d flows.%n", threadName, flowCount);

        } catch (InterruptedException e) {
            System.err.printf("[%s] FlowConsumer interrupted while dequeuing.%n", threadName);
            Thread.currentThread().interrupt();
        }
    }
}
