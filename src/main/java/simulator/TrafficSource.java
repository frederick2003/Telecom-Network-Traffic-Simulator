package simulator;

import java.util.Random;

/**
 * Represents a single independent ON/OFF traffic generator in the telecom simulator.
 *
 * <p>Each {@code TrafficSource} alternates between two states:
 * <ul>
 *     <li><b>OFF</b>: generating no traffic</li>
 *     <li><b>ON</b>: generating traffic at a fixed rate ({@code onRate})</li>
 * </ul>
 *
 * <p>The duration spent in each state is determined by the selected traffic model:
 * <ul>
 *     <li><b>Pareto ON/OFF model</b>: heavy-tailed durations sampled via inverse transform sampling</li>
 *     <li><b>Fractional Gaussian Noise (FGN)</b>: placeholder Gaussian-based durations scaled by Hurst parameter</li>
 * </ul>
 *
 * <p>Each source is responsible for scheduling its own ON → OFF and OFF → ON transitions
 * by generating {@link Event} objects with sampled delays.</p>
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

    /**
     * Constructs a new {@code TrafficSource} with ON/OFF distribution parameters.
     *
     * @param id        unique source identifier
     * @param onRate    traffic rate when ON
     * @param alphaOn   Pareto shape parameter for ON durations
     * @param xmOn      Pareto scale parameter for ON durations
     * @param alphaOff  Pareto shape parameter for OFF durations
     * @param xmOff     Pareto scale parameter for OFF durations
     * @param seed      seed for repeatable random sampling
     * @param modelType the traffic model to use (Pareto or FGN)
     * @param hurst     the Hurst parameter (used only for FGN model)
     */
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
     * Schedules the very first event for this source.
     *
     * <p>All sources begin in the OFF state. The first event is therefore always
     * a future {@code SOURCE_ON} event after sampling an OFF duration.</p>
     *
     * @param now the current simulation time
     * @return an {@link Event} representing the first ON transition
     */
    public Event scheduleInitialEvent(double now) {
        // Start OFF -> schedule first ON
        double dt = sampleDuration(false);
        return new Event(now + dt, id, EventType.SOURCE_ON);
    }

     /**
     * Toggles the source's ON/OFF state and schedules the next event accordingly.
     *
     * <p>If the source was OFF, it becomes ON and schedules a duration until OFF.
     * If the source was ON, it becomes OFF and schedules a duration until ON.</p>
     *
     * @param now the current simulation time
     * @return the next ON/OFF {@link Event} for this source
     */
    public Event scheduleNextEvent(double now) {
        // 1. Toggle state.
        isOn = !isOn;

        // 2. Sample new event duration.
        double dt = sampleDuration(isOn);

        // Returns a new event.
        return new Event(now + dt,  id, isOn ? EventType.SOURCE_OFF : EventType.SOURCE_ON);
    }

    /**
     * Samples a duration for either ON or OFF state depending on the model type.
     *
     * @param isOn whether the next duration corresponds to an ON state
     * @return a positive duration sampled from the configured distribution
     */
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
     * Samples a duration from the Fractional Gaussian Noise (FGN) model.
     *
     * <p>This is currently a placeholder implementation using an absolute Gaussian value
     * scaled differently for ON vs OFF durations. The Hurst parameter is stored but
     * not yet incorporated into the sampling process.</p>
     *
     * @param isOn whether the resulting duration is an ON period
     * @return a positive duration value
     */
    private double sampleFGNDuration(boolean isOn) {
        // Placeholder: simple Gaussian-based duration
        double val = Math.abs(rng.nextGaussian());

        // Scale based on ON/OFF
        return isOn ? (1 + val * 5) : (1 + val * 2);
    }


    /**
     * Samples a Pareto-distributed ON duration using inverse transform sampling.
     *
     * @return a heavy-tailed ON duration
     */
    public double sampleOnDuration() {
        return ParetoSampler.sample(alphaOn, xmOn, rng);
    }

    /**
     * Samples a Pareto-distributed OFF duration using inverse transform sampling.
     *
     * @return a heavy-tailed OFF duration
     */
    public double sampleOffDuration() {
        return ParetoSampler.sample(alphaOff, xmOff, rng);
    }

    // Getter methods
    public int getId() { return id; }
    public boolean getIsOn() { return isOn; }
    public double getOnRate() { return onRate; }
}
