package simulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleQueueTest {

    @Test
    void testAddTrafficWithoutOverflow() {
        SimpleQueue queue = new SimpleQueue(10, 3);

        queue.addTraffic(4); // fits easily
        assertEquals(4, queue.getQueueLength());
        assertEquals(0, queue.getDroppedPackets());
    }

    @Test
    void testAddTrafficExactCapacity() {
        SimpleQueue queue = new SimpleQueue(10, 3);

        queue.addTraffic(10);
        assertEquals(10, queue.getQueueLength());
        assertEquals(0, queue.getDroppedPackets());
    }

    @Test
    void testAddTrafficOverflow() {
        SimpleQueue queue = new SimpleQueue(10, 3);

        queue.addTraffic(8);
        queue.addTraffic(5); // overflow: only space for 2 more

        assertEquals(10, queue.getQueueLength()); // queue full
        assertEquals(3, queue.getDroppedPackets()); // 5 - space(2) = 3 dropped
    }

    @Test
    void testServiceReducesQueue() {
        SimpleQueue queue = new SimpleQueue(10, 3);

        queue.addTraffic(8);
        queue.service();

        // Service rate = 3, queue 8 -> 5
        assertEquals(5, queue.getQueueLength());
    }

    @Test
    void testServiceCannotGoNegative() {
        SimpleQueue queue = new SimpleQueue(10, 5);

        queue.addTraffic(3); // queue = 3
        queue.service();     // should drain all 3

        assertEquals(0, queue.getQueueLength());
    }

    @Test
    void testMultipleServiceSteps() {
        SimpleQueue queue = new SimpleQueue(10, 2);

        queue.addTraffic(7); // queue 7

        queue.service(); // -> 5
        assertEquals(5, queue.getQueueLength());

        queue.service(); // -> 3
        assertEquals(3, queue.getQueueLength());

        queue.service(); // -> 1
        assertEquals(1, queue.getQueueLength());

        queue.service(); // -> 0
        assertEquals(0, queue.getQueueLength());
    }

    @Test
    void testMixedAddAndService() {
        SimpleQueue queue = new SimpleQueue(10, 4);

        queue.addTraffic(6);  // queue 6
        queue.service();      // -> 2
        assertEquals(2, queue.getQueueLength());

        queue.addTraffic(5);  // -> 7
        assertEquals(7, queue.getQueueLength());

        queue.service();      // -> 3
        assertEquals(3, queue.getQueueLength());
    }

    @Test
    void testDroppedPacketsAccumulation() {
        SimpleQueue queue = new SimpleQueue(10, 1);

        queue.addTraffic(12);  // space 10 -> dropped 2
        queue.addTraffic(5);   // no space (queue already full) -> dropped 5

        assertEquals(7, queue.getDroppedPackets());
    }
}