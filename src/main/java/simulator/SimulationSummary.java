package simulator;

/**
 * Collects and computes summary statistics for a simulation run.
 *
 * This class is responsible for aggregating high-level metrics
 * (e.g., average ON duration, total events, min/max rate, etc.).
 *
 * It is designed to work alongside TimeSeriesRecorder, not replace it.
 */
public class SimulationSummary {

    private int totalEvents;
    private double totalOnTime;
    private double totalOffTime;
    private double maxAggregateRate;
    private double minAggregateRate = Double.MAX_VALUE;
}
