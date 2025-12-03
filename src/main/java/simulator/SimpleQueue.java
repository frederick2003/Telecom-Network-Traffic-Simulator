package main.java.simulator;

/**
 * A basic FIFO queue representing a network buffer.
 * It allows traffic (packets) to arrive, and drains packets at a fixed service rate.
 */
public class SimpleQueue {

    private final int capacity;           // Maximum number of packets in buffer
    private final double serviceRate;     // Packets drained per time unit
    private double currentSize = 0;       // Current number of packets in queue
    private double totalDropped = 0;      // Total packets dropped

    public SimpleQueue(int capacity, double serviceRate) {
        this.capacity = capacity;
        this.serviceRate = serviceRate;
    }

    /** Add packets to the queue. Drop remainder if full. */
    public void addTraffic(double packetsArriving) {
        double space = capacity - currentSize;

        if (packetsArriving <= space) {
            currentSize += packetsArriving;
        } else {
            currentSize = capacity;
            totalDropped += (packetsArriving - space);
        }
    }

    /** Drain packets according to service rate. */
    public void service() {
        currentSize -= Math.min(currentSize, serviceRate);
    }

    public double getQueueLength() {
        return currentSize;
    }

    public double getDroppedPackets() {
        return totalDropped;
    }
}
