package main.java.simulator;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

import static main.java.simulator.EventType.SOURCE_ON;
import static main.java.simulator.EventType.TICK;

public class SimulationManager {
    private final PriorityQueue<Event> eventQ = new PriorityQueue<>();
    private final List<TrafficSource> sources = new ArrayList<>();
    private final TimeSeriesRecorder recorder = new TimeSeriesRecorder();
    private double now = 0.0;
    private double aggregateRate = 0.0;

    public TimeSeriesRecorder getRecorder() { return recorder; }
    public double getNow() { return now; }
    public double getAggregateRate() { return aggregateRate; }

    public void addSource(TrafficSource s) {
        sources.add(s);
    }

    public void schedule(Event e) {
        eventQ.add(e);
    }

    public void seedInitialEvents() {
        for (TrafficSource s : sources) {
            schedule(s.scheduleInitialEvent(now));
        }
    }

    public void run(double untilTime) {
        seedInitialEvents();
        recorder.record(0.0, aggregateRate);
        while (!eventQ.isEmpty()) {
            Event e = eventQ.poll();
            if (e.getTime() > untilTime) break;

            // Integrate piecewise-constant segment
            recorder.addSegment(now, e.getTime(), aggregateRate);
            now = e.getTime();

            handle(e);
        }
        recorder.finish(untilTime);
    }

    private void handle(Event e) {
        TrafficSource s = (e.getSourceId() >= 0 && e.getSourceId() < sources.size()) ? sources.get(e.getSourceId()) : null;
        if (s == null) return;

        switch (e.getType()) {
            case SOURCE_ON -> {
                if (!s.getIsOn()) {
                    schedule(s.scheduleNextEvent(now)); // toggles to ON inside
                    aggregateRate += s.getOnRate();
                }
            }
            case SOURCE_OFF -> {
                if (s.getIsOn()) {
                    schedule(s.scheduleNextEvent(now)); // toggles to OFF inside
                    aggregateRate -= s.getOnRate();
                }
            }
            case TICK -> {
                // Optional: for periodic reporting
            }
        }
    }
}
