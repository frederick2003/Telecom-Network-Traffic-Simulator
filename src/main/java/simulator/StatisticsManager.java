package main.java.simulator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection, updating, and final reporting of summary statistics
 * throughout the telecom traffic simulation.
 *
 * <p>This class tracks metrics such as:
 * <ul>
 *     <li><b>Total events</b> processed</li>
 *     <li><b>Peak aggregate traffic</b> observed</li>
 *     <li><b>Time-weighted average traffic</b></li>
 *     <li><b>Hurst parameter</b> estimation based on aggregate rate samples</li>
 *     <li><b>Network queue behaviour</b> (maximum length and dropped packets)</li>
 * </ul>
 *
 * <p>The time-weighted average is computed by integrating the traffic rate
 * between successive events:
 *
 * <pre>
 *     trafficArea += aggregateRate * duration
 * </pre>
 *
 * and finalised at simulation end using:
 *
 * <pre>
 *     averageTraffic = trafficArea / totalSimulationTime
 * </pre>
 *
 * <p>The class also supports exporting summary statistics to a CSV file.</p>
 */
public class StatisticsManager {

    private int totalEvents;
    private double peakTraffic;
    private double averageTraffic;

    // For time-weighted average calculation
    private double lastEventTime;
    private double lastAggregateRate;
    private double trafficArea;
    private double totalSimulationTime;
    private double hurstParameter;

    private List<Double> timeSeriesRates = new ArrayList<>();

    // Network Queue Fields
    private double maxQueueLength = 0.0;
    private double totalDroppedPackets = 0.0;

    /**
     * Creates and initialises a new StatisticsManager with zeroed metrics.
     */
    public StatisticsManager() {
        this.totalEvents = 0;
        this.peakTraffic = 0.0;
        this.averageTraffic = 0.0;

        this.lastEventTime = 0.0;
        this.lastAggregateRate = 0.0;
        this.trafficArea = 0.0;
        this.totalSimulationTime = 0.0;
    }

    /**
     * Updates all relevant statistics for a newly processed event.
     *
     * <p>This includes:
     * <ul>
     *     <li>Advancing the time-weighted traffic integral</li>
     *     <li>Updating peak traffic</li>
     *     <li>Incrementing event count</li>
     * </ul>
     *
     * @param event                the event being processed
     * @param currentSource        the traffic source that triggered the event
     * @param currentAggregateRate the aggregate traffic rate immediately after
     *                             processing the event
     */
    public void updateSimulationStatistics(Event event,
                                           TrafficSource currentSource,
                                           double currentAggregateRate) {

        double eventTime = event.getTime();

        // Update the time-weighted traffic area using the rate that was in effect
        // from the previous event up to this event.
        double duration = eventTime - lastEventTime;
        if (duration > 0) {
            trafficArea += lastAggregateRate * duration;
        }

        // Update internal state for the next step
        lastEventTime = eventTime;
        lastAggregateRate = currentAggregateRate;

        updateTotalEvents();
        updatePeakTraffic(currentAggregateRate);
        // averageTraffic is finalised at the end of the simulation
    }

    /**
     * Finalises all statistics after the simulation ends.
     * This computes the final weighted average traffic based on
     * the remaining interval from the last event to the simulation end.
     *
     * @param simulationEndTime the final time of the simulation run
     */
    public void finaliseStatistics(double simulationEndTime) {
        // Capture the final interval from the last event to the end of the simulation
        double duration = simulationEndTime - lastEventTime;
        if (duration > 0) {
            trafficArea += lastAggregateRate * duration;
        }

        this.totalSimulationTime = simulationEndTime;

        if (simulationEndTime > 0) {
            this.averageTraffic = trafficArea / simulationEndTime;
        } else {
            this.averageTraffic = 0.0;
        }
    }

    /** Increments the total event counter. */
    private void updateTotalEvents() {
        this.totalEvents += 1;
    }


    /**
     * Updates the peak aggregate traffic measurement.
     *
     * @param currentAggregateRate the newly calculated aggregate rate
     */
    private void updatePeakTraffic(double currentAggregateRate) {
        if (currentAggregateRate > this.peakTraffic) {
            this.peakTraffic = currentAggregateRate;
        }
    }

    /**
     * Records a single aggregate traffic rate sample for later Hurst analysis.
     *
     * @param rate the aggregate traffic at a given time
     */
    public void recordAggregateRate(double rate){
        timeSeriesRates.add(rate);
    }

    /**
     * Computes the Hurst parameter using {@link HurstEstimator#estimateHurst(List)}.
     * Prints the estimated value to the console.
     */
    public void computeHurst(){
        hurstParameter = HurstEstimator.estimateHurst(timeSeriesRates);
        System.out.println("Hurst Parameter:" + hurstParameter);
    }

    /**
     * Updates statistics related to the network queue, including:
     * <ul>
     *     <li>Maximum queue length</li>
     *     <li>Total dropped packets</li>
     * </ul>
     *
     * @param queueLength    the current queue size
     * @param droppedPackets cumulative packets dropped so far
     */
    public void updateQueueStats(double queueLength, double droppedPackets) {
        if (queueLength > maxQueueLength) {
            maxQueueLength = queueLength;
        }
        totalDroppedPackets = droppedPackets;
    }

    /**
     * Writes a CSV file containing summary statistics from the simulation.
     *
     * <p>CSV Columns:
     * <pre>
     * Total Events,
     * Peak Traffic,
     * Average Traffic,
     * Hurst Parameter,
     * Max Queue Length,
     * Dropped Packets
     * </pre>
     *
     * @param filePath the output path for the CSV file
     */
    public void logSummaryStatsToCsv(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, false))) {

            // Write CSV header
            writer.println("Total Events,Peak Traffic,Average Traffic,Hurst Parameter,Max Queue Length,Dropped Packets");

            // Write statistics
            writer.println(totalEvents + "," +
                    peakTraffic + "," +
                    averageTraffic + "," +
                    hurstParameter + "," +
                    maxQueueLength + "," +
                    totalDroppedPackets);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Logged summary statistics to " + filePath);
    }

    // Getter Methods
    public int getTotalEvents() {
        return totalEvents;
    }
    public double getPeakTraffic() {
        return peakTraffic;
    }
    public double getAverageTraffic() {
        return averageTraffic;
    }
    public double getTotalSimulationTime() {
        return totalSimulationTime;
    }
    public double getMaxQueueLength() { return maxQueueLength; }
    public double getTotalDroppedPackets() { return totalDroppedPackets; }
}
