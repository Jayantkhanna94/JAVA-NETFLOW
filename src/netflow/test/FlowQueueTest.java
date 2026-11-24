package netflow.test;

import netflow.concurrency.FlowQueue;
import netflow.exception.InvalidFlowException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link FlowQueue} blocking, wait/notify coordination,
 * and element delivery (Test T13).
 */
class FlowQueueTest {

    @Test
    @DisplayName("T13.1: Sequential produce and consume correctly buffers and dequeues elements")
    void testSequentialProduceConsume() throws InvalidFlowException, InterruptedException {
        FlowQueue queue = new FlowQueue(3);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());

        TrafficFlow flow = new TrafficFlow("F-Q1", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 1000, 80);
        queue.produce(flow);

        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());

        TrafficFlow consumed = queue.consume();
        assertEquals(flow, consumed);
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("T13.2: Concurrent producer and consumer transfer elements across bounded capacity")
    void testConcurrentProduceAndConsume() throws InvalidFlowException, InterruptedException {
        int capacity = 2; // Tight capacity to force thread blocking
        int itemCount = 6;
        FlowQueue queue = new FlowQueue(capacity);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= itemCount; i++) {
                    TrafficFlow f = new TrafficFlow("F-" + i, "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 1000 + i, 80);
                    queue.produce(f);
                }
            } catch (Exception e) {
                fail("Producer threw unexpected exception: " + e.getMessage());
            }
        }, "TestProducer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= itemCount; i++) {
                    TrafficFlow f = queue.consume();
                    assertNotNull(f);
                }
            } catch (Exception e) {
                fail("Consumer threw unexpected exception: " + e.getMessage());
            }
        }, "TestConsumer");

        consumer.start();
        producer.start();

        producer.join(3000);
        consumer.join(3000);

        assertFalse(producer.isAlive(), "Producer thread should have terminated.");
        assertFalse(consumer.isAlive(), "Consumer thread should have terminated.");
        assertEquals(0, queue.size(), "Queue should be empty after all items are consumed.");
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException for capacity <= 0")
    void testInvalidCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FlowQueue(0));
        assertThrows(IllegalArgumentException.class, () -> new FlowQueue(-5));
    }

    @Test
    @DisplayName("Enqueuing null flow throws IllegalArgumentException")
    void testProduceNullThrows() {
        FlowQueue queue = new FlowQueue(5);
        assertThrows(IllegalArgumentException.class, () -> queue.produce(null));
    }
}
