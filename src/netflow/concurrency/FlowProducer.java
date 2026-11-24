package netflow.concurrency;

import netflow.exception.InvalidFlowException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;

/**
 * Producer Runnable that simulates incoming network telemetry streams
 * and deposits {@link TrafficFlow} records into a bounded {@link FlowQueue}.
 */
public class FlowProducer implements Runnable {

    private final FlowQueue queue;
    private final int flowCount;

    /**
     * Default constructor producing 8 flow records.
     */
    public FlowProducer(FlowQueue queue) {
        this(queue, 8);
    }

    /**
     * Parameterized constructor specifying flow production quantity.
     *
     * @param queue The bounded queue to feed flows into.
     * @param flowCount Total number of flows to produce.
     */
    public FlowProducer(FlowQueue queue, int flowCount) {
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
                String flowId = String.format("FLOW-PC-%03d", i);
                TrafficFlow flow = new TrafficFlow(
                        flowId,
                        "192.168.1." + (100 + i),
                        "10.0.0." + (200 + i),
                        (i % 2 == 0) ? NetworkProtocol.TCP : NetworkProtocol.UDP,
                        5000 + i,
                        80,
                        20L * i,
                        1024L * i,
                        100L * i,
                        System.currentTimeMillis()
                );

                // Simulate network capture arrival delay
                Thread.sleep(40);

                // Enqueue flow into bounded buffer (blocks if full)
                queue.produce(flow);
            }
            System.out.printf("[%s] Producer finished producing %d flows.%n", threadName, flowCount);

        } catch (InterruptedException e) {
            System.err.printf("[%s] FlowProducer interrupted while enqueuing.%n", threadName);
            Thread.currentThread().interrupt();
        } catch (InvalidFlowException e) {
            System.err.printf("[%s] Error creating simulated flow: %s%n", threadName, e.getMessage());
        }
    }
}
