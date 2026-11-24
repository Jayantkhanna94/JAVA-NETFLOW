package netflow.concurrency;

import netflow.model.TrafficFlow;

import java.util.LinkedList;

/**
 * Shared bounded blocking queue implementing the Producer-Consumer pattern.
 * Uses a {@link LinkedList} with intrinsic monitor locking, condition checks
 * in while loops, and explicit {@code wait()} and {@code notifyAll()} calls.
 */
public class FlowQueue {

    private final LinkedList<TrafficFlow> queue;
    private final int maxCapacity;

    /**
     * Default constructor with a standard bounded capacity of 5.
     */
    public FlowQueue() {
        this(5);
    }

    /**
     * Bounded queue constructor with user-defined capacity limit.
     *
     * @param maxCapacity Maximum number of flows the queue can buffer.
     */
    public FlowQueue(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be > 0");
        }
        this.maxCapacity = maxCapacity;
        this.queue = new LinkedList<>();
    }

    /**
     * Enqueues a flow record into the bounded buffer.
     * If the queue is at capacity, the producer thread releases the monitor
     * and blocks via {@code wait()} inside a while loop until space is freed.
     *
     * @param flow The {@link TrafficFlow} to enqueue.
     * @throws InterruptedException if interrupted while waiting for capacity.
     */
    public synchronized void produce(TrafficFlow flow) throws InterruptedException {
        if (flow == null) {
            throw new IllegalArgumentException("Cannot enqueue null flow");
        }

        // Guard condition in a while loop to handle spurious wakeups
        while (queue.size() == maxCapacity) {
            System.out.printf("[%s] Queue is FULL (%d/%d). Producer waiting...%n",
                    Thread.currentThread().getName(), queue.size(), maxCapacity);
            wait();
        }

        queue.addLast(flow);
        System.out.printf("[%s] PRODUCED: %s | Queue Size: %d/%d%n",
                Thread.currentThread().getName(), flow.getFlowId(), queue.size(), maxCapacity);

        // Notify waiting consumer threads that data is available
        notifyAll();
    }

    /**
     * Dequeues a flow record from the bounded buffer.
     * If the queue is empty, the consumer thread releases the monitor
     * and blocks via {@code wait()} inside a while loop until a flow is produced.
     *
     * @return The dequeued {@link TrafficFlow} record.
     * @throws InterruptedException if interrupted while waiting for data.
     */
    public synchronized TrafficFlow consume() throws InterruptedException {
        // Guard condition in a while loop to handle spurious wakeups
        while (queue.isEmpty()) {
            System.out.printf("[%s] Queue is EMPTY. Consumer waiting...%n",
                    Thread.currentThread().getName());
            wait();
        }

        TrafficFlow flow = queue.removeFirst();
        System.out.printf("[%s] CONSUMED: %s | Queue Size: %d/%d%n",
                Thread.currentThread().getName(), flow.getFlowId(), queue.size(), maxCapacity);

        // Notify waiting producer threads that buffer space has been released
        notifyAll();

        return flow;
    }

    /**
     * Returns the current number of flow records in the queue.
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * Checks if the queue is currently empty.
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns the maximum buffer capacity.
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }
}
