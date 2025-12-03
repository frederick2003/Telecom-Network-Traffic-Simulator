package main.java.simulator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects simple summary statistics over the course of a simulation:
 *
 *  - totalEvents:    total number of events processed
 *  - peakTraffic:    maximum aggregate traffic observed
 *  - averageTraffic: time-weighted average aggregate traffic
 *
 * The average traffic is computed using a time-weighted approach:
 * for each interval between two events, we accumulate
 *
 *      trafficArea += aggregateRate * duration
 *
 * and at the end of the simulation:
 *
 *      averageTraffic = trafficArea / totalSimulationTime
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
     * Called by the simulation loop whenever an event is processed.
     *
     * @param event                the event being processed
     * @param currentSource        the source associated with the event (if needed)
     * @param currentAggregateRate the aggregate traffic rate immediately
     *                             after this event has been applied
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
     * Call this once at the end of the simulation, after the last event has been
     * processed. This method finalises the time-weighted average traffic.
     *
     * @param simulationEndTime the final simulation time (e.g. totalTime)
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

    private void updateTotalEvents() {
        this.totalEvents += 1;
    }

    private void updatePeakTraffic(double currentAggregateRate) {
        if (currentAggregateRate > this.peakTraffic) {
            this.peakTraffic = currentAggregateRate;
        }
    }

    public void recordAggregateRate(double rate){
        timeSeriesRates.add(rate);
    }

    public void computeHurst(){
        hurstParameter = HurstEstimator.estimateHurst(timeSeriesRates);
        System.out.println("Hurst Parameter:" + hurstParameter);
    }

    public void updateQueueStats(double queueLength, double droppedPackets) {
        if (queueLength > maxQueueLength) {
            maxQueueLength = queueLength;
        }
        totalDroppedPackets = droppedPackets;
    }

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
