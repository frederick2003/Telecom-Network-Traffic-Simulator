package main.java.simulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeSeriesRecorder {

    private final List<double[]> points = new ArrayList<>();
    private double lastTime = 0.0;
    private Double currentAggregateRate = null;
    private final List<String> eventsLog = new ArrayList<>();

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

    public void record(double t, double totalAggregateRate) {
        points.add(new double[]{t, totalAggregateRate});
        lastTime = t;
        currentAggregateRate = totalAggregateRate;
    }

    public void finish(double now) {
        if (currentAggregateRate == null) return;
        if (points.isEmpty() || points.get(points.size()-1)[0] < now) {
            points.add(new double[]{now, currentAggregateRate});
        }
    }

    public List<double[]> asPoints() {
        return points;
    }

    public void recordEventLog(double sourceId, EventType eventType, double eventTime, double currentAggregateRate){
        String eventlog = (int) sourceId + "," + eventType + "," + eventTime + "," + currentAggregateRate;
        eventsLog.add(eventlog);

    }

    public void recordSummarysStats(){
        
    }
    
    public void printEventsLog(){
        System.out.println("sourceId" + "," + "eventType" + "," + "eventTime" + "," + "currentAggregateRate");
        for (String event: eventsLog){
            System.out.println(event);
        }
    }

    public void outputTimeSeriesData(){
        System.out.println("time,totalAggregateRate");
        for (double[] p : points) {
            System.out.println("Time: " + p[0] + ", aggregateRate: " + p[1]);
        }
    }

    public void toCsv(Path path) throws IOException {
        var sb = new StringBuilder();
        sb.append("time,totalAggregateRate\n");
        for (double[] p : points) {
            sb.append(p[0]).append(',').append(p[1]).append('\n');
        }
        Files.writeString(path, sb.toString());
    }
}
