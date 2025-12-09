package simulator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Records time-series data for the aggregate traffic rate and logs simulation events.
 *
 * <p>This class stores:
 * <ul>
 *     <li>A sequence of (time, aggregateRate) points representing the piecewise-constant
 *         evolution of traffic over the simulation.</li>
 *     <li>A log of events describing source state changes (ON/OFF) with timestamps.</li>
 * </ul>
 *
 * <p>The recorded data can be exported to CSV format for external plotting or analysis.</p>
 */
public class TimeSeriesRecorder {

    private final List<double[]> points = new ArrayList<>();
    private double lastTime = 0.0;
    private Double currentAggregateRate = null;
    private final List<String> eventsLog = new ArrayList<>();

    /**
     * Records a piecewise-constant segment of traffic from t0 to t1.
     *
     * <p>This method is called when an event occurs, marking the end of a constant-rate interval.
     * It stores a new time-series point at time t1 with the new aggregate rate in effect from that moment.</p>
     *
     * @param t0                  the beginning of the segment
     * @param t1                  the end of the segment (must be >= t0)
     * @param totalAggregateRate  the aggregate rate after the event at time t1
     */
    public void addSegment(double t0, double t1, double totalAggregateRate) {
        if (t1 < t0) throw new IllegalArgumentException("t1 >= t0");
        if (points.isEmpty() && t0 > 0) {
            // Ensure we start at 0
            points.add(new double[]{0.0, totalAggregateRate});
        }
        // Record at t1 with the new totalAggregateRate after segment
        points.add(new double[]{t1, totalAggregateRate});
        lastTime = t1;
        currentAggregateRate = totalAggregateRate;
    }

    /**
     * Records a time-series point directly, without assuming a segment.
     *
     * @param t                   the timestamp
     * @param totalAggregateRate  the aggregate traffic rate at time t
     */
    public void record(double t, double totalAggregateRate) {
        points.add(new double[]{t, totalAggregateRate});
        lastTime = t;
        currentAggregateRate = totalAggregateRate;
    }

    /**
     * Ensures the final simulation time is recorded with the last known aggregate rate.
     *
     * <p>If the recorder has not yet logged a point at time {@code now}, one is added.</p>
     *
     * @param now the final simulation time
     */
    public void finish(double now) {
        if (currentAggregateRate == null) return;
        if (points.isEmpty() || points.get(points.size()-1)[0] < now) {
            points.add(new double[]{now, currentAggregateRate});
        }
    }

    /**
     * Returns the recorded time-series points.
     *
     * @return a list of arrays in the form {time, aggregateRate}
     */
    public List<double[]> asPoints() {
        return points;
    }

    /**
     * Records a simulation event into an internal string-based CSV log.
     *
     * <p>Each logged entry follows the format:</p>
     * <pre>
     * sourceId,eventType,eventTime,currentAggregateRate
     * </pre>
     *
     * @param sourceId              the ID of the traffic source generating the event
     * @param eventType             the type of event (ON/OFF)
     * @param eventTime             the time the event occurred
     * @param currentAggregateRate  the aggregate traffic at the moment of the event
     */
    public void recordEventLog(double sourceId, EventType eventType, double eventTime, double currentAggregateRate){
        String eventlog = (int) sourceId + "," + eventType + "," + eventTime + "," + currentAggregateRate;
        eventsLog.add(eventlog);

    }

    /**
     * Writes the event log to a CSV file.
     *
     * <p>Produces a file with header:</p>
     * <pre>
     * sourceId,eventType,eventTime,currentAggregateRate
     * </pre>
     *
     * @param filePath the path to the CSV file to write
     */
    public void logEventsToCsv(String filePath){
        try(PrintWriter writer = new PrintWriter(new FileWriter(filePath, false))){
            writer.println("sourceId" + "," + "eventType" + "," + "eventTime" + "," + "currentAggregateRate");
            for (String event: eventsLog){
                writer.println(event);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("Logged simulation event data to " + filePath);
    }

    /**
     * Writes the time-series (time, aggregateRate) data to a CSV file.
     *
     * <p>Outputs the file with header:</p>
     * <pre>
     * time,totalAggregateRate
     * </pre>
     *
     * @param filePath the output CSV file path
     */
    public void logTimeSeriesDataToCsv(String filePath){
        try(PrintWriter writer = new PrintWriter(new FileWriter(filePath, false))){
            writer.println("time,totalAggregateRate");
            for (double[] p : points) {
                writer.println(p[0] + "," + p[1]);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Logged time-series data to " + filePath);
    }

    /**
     * Writes the time-series data to a CSV file using {@link Files#writeString(Path, String)}.
     *
     * @param path the file path to write to
     * @throws IOException if writing to the file fails
     */
    public void toCsv(Path path) throws IOException {
        var sb = new StringBuilder();
        sb.append("time,totalAggregateRate\n");
        for (double[] p : points) {
            sb.append(p[0]).append(',').append(p[1]).append('\n');
        }
        Files.writeString(path, sb.toString());
    }
}
