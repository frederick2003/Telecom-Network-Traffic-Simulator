package main.java.simulator;

import java.util.Random;

/**
 * Class to represent a single independent traffic generator.
 */
public class TrafficSource {
    private final int id; // Identifier.
    private boolean isOn; // Current state.
    private final double onRate; // Traffic rate when ON. (How fast it sends data)
    private final double alphaOn, xmOn; // Pareto parameters for ON duration.
    private final double alphaOff, xmOff; // Pareto parameters for OFF duration.
    private final Random rng; // Random number generator.

    public TrafficSource(int id, double onRate,
                         double alphaOn, double xmOn,
                         double alphaOff, double xmOff,
                         long seed) {
        this.id = id;
        this.onRate = onRate;
        this.alphaOn = alphaOn;
        this.xmOn = xmOn;
        this.alphaOff = alphaOff;
        this.xmOff = xmOff;
        this.rng = new Random(seed);
        this.isOn = false;
    }

    /**
     * Schedules the first event for this source.
     * @param now current time
     * @return A new Event object with an OFF duration created by sampling the pareto distribution.
     */
    public Event scheduleInitialEvent(double now) {
        // Start OFF -> schedule first ON
        double dt = sampleOffDuration(); // Samples a random OFF duration
        return new Event(now + dt, id, EventType.SOURCE_ON);
    }

    /**
     * Schedules the next event for this source.
     * @param now current time.
     * @return a new Event object with an ON or OFF duration created by sampling the pareto distribution.
     */
    public Event scheduleNextEvent(double now) {
        isOn = !isOn; // Toggle state and schedule opposite
        double dt = isOn ? sampleOnDuration() : sampleOffDuration(); // Samples a random ON or OFF duration based on the sources current state.
        return new Event(now + dt,  id, isOn ? EventType.SOURCE_OFF : EventType.SOURCE_ON);
    }

    /**
     * Gets a random ON duration by Inverse transform sampling the pareto distribution.
     * @return a random heavy-tailed ON duration.
     */
    public double sampleOnDuration() {
        return ParetoSampler.sample(alphaOn, xmOn, rng);
    }

    /**
     * Gets a random OFF duration by Inverse transform sampling the pareto distribution.
     * @return a random heavy-tailed OFF duration.
     */
    public double sampleOffDuration() {
        return ParetoSampler.sample(alphaOff, xmOff, rng);
    }

    // Getter methods
    public int getId() { return id; }
    public boolean getIsOn() { return isOn; }
    public double getOnRate() { return onRate; }
}
