package simulator;

/**
 * A basic FIFO queue representing a simple network buffer.
 * <p>This queue models packet accumulation and servicing in a network node.
 * Traffic (measured in "packets" or abstract load units) may arrive each
 * simulation step, and the queue drains at a constant service rate.
 *
 * <p>If incoming traffic exceeds available buffer capacity, the excess is
 * counted as dropped. This allows the simulator to observe congestion behaviour
 * such as queue build-up and packet loss.
 */
public class SimpleQueue {

    private final int capacity;           // Maximum number of packets in buffer
    private final double serviceRate;     // Packets drained per time unit
    private double currentSize = 0;       // Current number of packets in queue
    private double totalDropped = 0;      // Total packets dropped

    /**
     * Constructs a new {@code SimpleQueue} with a given capacity and service rate.
     * @param capacity The maximum number of packets the queue can store.
     * @param serviceRate The number of packets drained per simulation step.
     */
    public SimpleQueue(int capacity, double serviceRate) {
        this.capacity = capacity;
        this.serviceRate = serviceRate;
    }

    /**
     * Adds incoming traffic to the queue
     *
     * <p>If there is enough space, all arriving packets are stored. Otherwise,
     * the queue fills to capacity and any remaining packets are counted as dropped.
     *
     * @param packetsArriving the amount of traffic (in packets) arriving this step.
     */
    public void addTraffic(double packetsArriving) {
        double space = capacity - currentSize;

        if (packetsArriving <= space) {
            currentSize += packetsArriving;
        } else {
            currentSize = capacity;
            totalDropped += (packetsArriving - space);
        }
    }

    /**
     * Drains the queue according to the configured service rate.
     * <p>The number of packets removed is the minimum of:</p>
     * <ul>
     *     <li>The current queue size</li>
     *     <li>The service Rate</li>
     * </ul>
     */
    public void service() {
        currentSize -= Math.min(currentSize, serviceRate);
    }

    /**
     * Returns the current queue occupancy (measured in packets)
     * @return Current Buffer Size
     */
    public double getQueueLength() {
        return currentSize;
    }

    /**
     * Returns the total number of packets that have been dropped since the queue was created.
     * @return Cumulative dropped packet count
     */
    public double getDroppedPackets() {
        return totalDropped;
    }
}
