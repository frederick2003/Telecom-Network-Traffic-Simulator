package simulator;

/**
 * A timestamped instruction telling the simulator what action to run and when.
 */
public class Event implements Comparable<Event>{
    private final double time; // When
    private final int sourceId; // Who.
    private final EventType type; // What

    /**
     * Creates a new event with a specific time, type, and ID
     * @param time tells the simulator when to process the event.
     * @param sourceId tells the simulator which traffic source it belongs to.
     * @param type tells the simulator what the event represents.
     */
    public Event(double time, int sourceId, EventType type){
        this.time = time;
        this.sourceId = sourceId;
        this.type = type;
    }

    /**
     * Allows event to easily be sorted by their timestamp, so that the simulator can reliably process the earliest event first.
     * @param other Event object to compare against the current Event instance. (Each objects time field is compared)
     * @return 0 if (this.time == other.time), -1 if (this.time < other.time), or 1 if (this.time > other.time)
     */
    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }

    /**
     * A debugging method to output event fields.
     * @return a human-readable description of the event, for debugging.
     */
    @Override
    public String toString() {
        return "Event{time=" + time + ", type=" + type + ", sourceId=" + sourceId + "}";
    }

    // Getter methods
    public double getTime() { return time; }
    public EventType getType() { return type; }
    public int getSourceId() { return sourceId; }
}
