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
    private final TrafficModelType modelType;
    private final double hurst;

    public TrafficSource(int id, double onRate,
                         double alphaOn, double xmOn,
                         double alphaOff, double xmOff,
                         long seed,
                         TrafficModelType modelType,
                         double hurst
                         ) {
        this.id = id;
        this.onRate = onRate;
        this.alphaOn = alphaOn;
        this.xmOn = xmOn;
        this.alphaOff = alphaOff;
        this.xmOff = xmOff;
        this.rng = new Random(seed);
        this.isOn = false;
        this.modelType=modelType;
        this.hurst=hurst;
    }

    /**
     * Schedules the first event for this source.
     * @param now current time
     * @return A new Event object with an OFF duration created by sampling the pareto distribution.
     */
    public Event scheduleInitialEvent(double now) {
        // Start OFF -> schedule first ON
        double dt = sampleDuration(false);
        return new Event(now + dt, id, EventType.SOURCE_ON);
    }

    /**
     * Schedules the next event for this source.
     * @param now current time.
     * @return a new Event object with an ON or OFF duration created by sampling the pareto distribution.
     */
    public Event scheduleNextEvent(double now) {
        // 1. Toggle state.
        isOn = !isOn;

        // 2. Sample new event duration.
        double dt = sampleDuration(isOn);

        // Returns a new event.
        return new Event(now + dt,  id, isOn ? EventType.SOURCE_OFF : EventType.SOURCE_ON);
    }

    private double sampleDuration(boolean isOn){
        switch (modelType){
            case PARETO -> {
                return isOn ? sampleOnDuration() : sampleOffDuration();
            }
            case FRACTIONAL_GAUSSIAN_NOISE -> {
                return sampleFGNDuration(isOn);
            }
            default -> throw new IllegalStateException("Unknown Model Type" + modelType);
        }
    }

    /**
     *
     * @param isOn
     * @return
     */
    private double sampleFGNDuration(boolean isOn) {
        // Placeholder: simple Gaussian-based duration
        double val = Math.abs(rng.nextGaussian());

        // Scale based on ON/OFF
        return isOn ? (1 + val * 5) : (1 + val * 2);
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
