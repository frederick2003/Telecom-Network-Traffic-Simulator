package main.java.simulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TimeSeriesRecorder {

    private final List<double[]> points = new ArrayList<>();
    private double lastTime = 0.0;
    private Double currentValue = null;

    public void addSegment(double t0, double t1, double value) {
        if (t1 < t0) throw new IllegalArgumentException("t1 >= t0");
        if (points.isEmpty() && t0 > 0) {
            // Ensure we start at 0
            points.add(new double[]{0.0, value});
        }
        // Record at t1 with the new value after segment
        points.add(new double[]{t1, value});
        lastTime = t1;
        currentValue = value;
    }

    public void record(double t, double value) {
        points.add(new double[]{t, value});
        lastTime = t;
        currentValue = value;
    }

    public void finish(double now) {
        if (currentValue == null) return;
        if (points.isEmpty() || points.get(points.size()-1)[0] < now) {
            points.add(new double[]{now, currentValue});
        }
    }

    public List<double[]> asPoints() {
        return points;
    }

    public void toCsv(Path path) throws IOException {
        var sb = new StringBuilder();
        sb.append("time,value\n");
        for (double[] p : points) {
            sb.append(p[0]).append(',').append(p[1]).append('\n');
        }
        Files.writeString(path, sb.toString());
    }
}
