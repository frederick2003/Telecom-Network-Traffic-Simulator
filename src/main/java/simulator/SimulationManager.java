package main.java.simulator;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

import static main.java.simulator.EventType.SOURCE_ON;
import static main.java.simulator.EventType.TICK;

/**
 * The central controller of the event driven telecom traffic simulator.
 */
public class SimulationManager {
    private final PriorityQueue<Event> eventQ = new PriorityQueue<>();
    private final List<TrafficSource> sources = new ArrayList<>();
    private final TimeSeriesRecorder recorder = new TimeSeriesRecorder();
    private double now = 0.0;
    private double aggregateRate = 0.0;

    /**
     * Adds a new Traffic source (ON,OFF generator) to the simulator so it can participate in the event-driven process.
     * @param s Traffic source to add to the ArrayList.
     */
    public void addSource(TrafficSource s) {
        sources.add(s);
    }

    /**
     * Adds an event to the event queue, (PriorityQueue sorted by event time).
     * This queue determines the order in which events are processed in the simulation.
     * @param e event to add to the PriorityQueue.
     */
    public void schedule(Event e) {
        eventQ.add(e);
    }

    /**
     * Schedules the first event for every traffic source at the start of the simulation.
     *
     * For each TrafficSource in sources, it calls that source's scheduleInitialEvent method(now) method.
     * Each source decides randomly whether it is on or off and schedules the appropriate first event.
     * Each event is then added to the event queue via the schedule method.
     */
    public void seedInitialEvents() {
        for (TrafficSource s : sources) {
            schedule(s.scheduleInitialEvent(now));
        }
    }

    /**
     * Runs main simulation loop, processes events until user defined total time is reached.
     * Core method that progresses the simulation.
     *
     * @param untilTime user defined total simulation time.
     */
    public void run(double untilTime) {

        seedInitialEvents(); // Puts each sources first event into the event queue.
        recorder.record(0.0, aggregateRate);

        while (!eventQ.isEmpty()) {
            Event e = eventQ.poll(); // Grabs first event in the event queue.
            if (e.getTime() > untilTime) break;

            // Integrate piecewise-constant segment
            recorder.addSegment(now, e.getTime(), aggregateRate);
            now = e.getTime();

            // Updates source state.
            handle(e);
        }

        // Closes off the final time segment.
        recorder.finish(untilTime);
    }

    /**
     * Manages event execution, namely toggles ON, OFF sources and adjusts aggregateRate.
     * @param e current event object.
     */
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

    // Getter methods.
    public TimeSeriesRecorder getRecorder() { return recorder; }
    public double getNow() { return now; }
    public double getAggregateRate() { return aggregateRate; }
}
