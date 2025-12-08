package main.java.simulator;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

/**
 * The central controller of the event-driven telecom traffic simulator.
 *
 * <p>This class coordinates all major simulation components, including:
 * <ul>
 *     <li>The event queue (priority-driven)</li>
 *     <li>All {@link TrafficSource} objects</li>
 *     <li>Statistics collection via {@link StatisticsManager}</li>
 *     <li>Queueing behaviour via {@link SimpleQueue}</li>
 *     <li>Time-series recording via {@link TimeSeriesRecorder}</li>
 * </ul>
 *
 * <p>Its responsibilities include:
 * <ul>
 *     <li>Scheduling and executing events in chronological order</li>
 *     <li>Maintaining the global simulation clock</li>
 *     <li>Updating aggregate traffic rate as sources toggle ON/OFF</li>
 *     <li>Feeding traffic into a buffer queue and measuring congestion</li>
 *     <li>Recording simulation output for later inspection or CSV export</li>
 * </ul>
 */
public class SimulationManager {
    /** Priority queue storing all future events, ordered by event time. */
    public final PriorityQueue<Event> eventQ = new PriorityQueue<>();

    /**  List of all active traffic sources participating in the simulation. */
    private final List<TrafficSource> sources = new ArrayList<>();

    /** Records piecewise-constant traffic rate segments and event logs. */
    private final TimeSeriesRecorder recorder = new TimeSeriesRecorder();

    /**  Collects and computes statistics on aggregate traffic, queueing, and events. */
    private final StatisticsManager statsManager = new StatisticsManager();

    /** Simple FIFO queue modelling network congestion behaviour.  */
    private final SimpleQueue queue = new SimpleQueue(1000, 5.0);

    /** The current simulation time (updated as events execute). */
    private double now = 0.0;

    /** Current aggregate traffic rate (sum of ON source rates). */
    private double aggregateRate = 0.0;

    /**
     * Adds a new {@link TrafficSource} to the simulation.
     * Each {@link TrafficSource} object generates ON/OFF events during the event-friven loop.
     * @param source Traffic source to add.
     */
    public void addSource(TrafficSource source) {
        sources.add(source);
    }

    /**
     * Schedules a new event by inserting it into the priority queue.
     * Events are always processed in chronological order.
     * @param event The event to schedule.
     */
    public void schedule(Event event) {
        eventQ.add(event);
    }

    /**
     * Schedules the first event for every traffic source.
     *
     * <p>Each source starts in the OFF state. Calling
     * {@link TrafficSource#scheduleInitialEvent(double)} samples the first OFF duration
     * and returns the corresponding event. These initial events are placed into
     * the event queue before the simulation loop begins.
     */
    public void seedInitialEvents() {
        for (TrafficSource s : sources) {
            schedule(s.scheduleInitialEvent(now));
        }
    }

    /**
     * Manages the logic of a single event execution.
     *
     * <p>This includes:</p>
     * <ul>
     *     <li>Toggling the state of a {@link TrafficSource}</li>
     *     <li>Updating the aggregate traffic rate</li>
     *     <li>Scheduling the next ON or OFF event</li>
     * </ul>
     * @param e The event to process.
     */
    protected void handle(Event e) {
        TrafficSource s = (e.getSourceId() >= 0 && e.getSourceId() < sources.size()) ? sources.get(e.getSourceId()) : null;
        if (s == null) return;

        // Switch on event type
        switch (e.getType()) {
            case SOURCE_ON -> {
                if (!s.getIsOn()) {
                    // Schedules an on duration & Toggles TrafficSource state of On.
                    schedule(s.scheduleNextEvent(now));

                    // Adds the source rate to the aggregate rate.
                    aggregateRate += s.getOnRate();
                }
            }
            case SOURCE_OFF -> {
                if (s.getIsOn()) {
                    // Schedules an off duration & Toggles TrafficSource state of off.

                    // 3. New event scheduled.
                    schedule(s.scheduleNextEvent(now));

                    // Removes the source rate from the aggregate rate.
                    aggregateRate -= s.getOnRate();
                }
            }
            default -> {
                System.out.print("Default triggered");
            }
        }
    }

    /**
     * Executes the main simulation loop until the specified time limit is reached.
     *
     * <p>The loop repeatedly:
     * <ol>
     *     <li>Extracts the next event from the priority queue</li>
     *     <li>Advances the simulation time</li>
     *     <li>Updates state and traffic based on the event</li>
     *     <li>Feeds aggregate traffic into the {@link SimpleQueue}</li>
     *     <li>Records time-series data and statistics</li>
     * </ol>
     *
     * <p>Once the simulation ends, statistics are finalised and summary reports are written.</p>
     *
     * @param untilTime The maximum simulation time before stopping.
     */
    public void run(double untilTime) {

        seedInitialEvents(); // Puts each sources' first event into the event queue.
        recorder.record(0.0, aggregateRate);

        System.out.println("Simulation started...");
        System.out.println("Running until time: " + untilTime + "\n");

        double nextStatusUpdate = 0.0;
        double statusInterval = untilTime / 10.0;

        while (!eventQ.isEmpty()) {
            Event e = eventQ.poll(); // Grabs and removes event in the front of the event queue.
            if (e.getTime() > untilTime) break;

            // Integrate piecewise-constant segment
            recorder.addSegment(now, e.getTime(), aggregateRate);
            now = e.getTime();

            // Updates source state.
            handle(e);

            // Queue Integration
            queue.addTraffic(aggregateRate);
            queue.service();
            statsManager.updateQueueStats(queue.getQueueLength(), queue.getDroppedPackets());

            // Simulation Statistics
            statsManager.recordAggregateRate(aggregateRate);
            statsManager.updateSimulationStatistics(e, sources.get(e.getSourceId()), aggregateRate);
            recorder.recordEventLog(e.getSourceId(),e.getType(), e.getTime(), aggregateRate);

            logSimulationStatus(now, nextStatusUpdate, untilTime, statusInterval);
        }

        statsManager.finaliseStatistics(untilTime);
        statsManager.computeHurst();
        statsManager.logSummaryStatsToCsv("data/summary-stats.csv");
        // Closes off the final time segment.
        recorder.finish(untilTime);
    }

    /**
     * Periodically logs simulation progress to console.
     *
     * @param currentTime The current simulation time.
     * @param nextStatusUpdate The time at which the next progress update should occur
     * @param untilTime The total simulation time.
     * @param statusInterval The interval between progress updates.
     */
    private void logSimulationStatus(double currentTime, double nextStatusUpdate,
                                     double untilTime, double statusInterval){
        if (currentTime >= nextStatusUpdate) {
            double percent = (currentTime /untilTime) * 100.0;
            System.out.printf(
                    "Status: time = %.2f / %.2f (%.0f%% complete)%n",
                    currentTime, untilTime, percent
            );
            nextStatusUpdate += statusInterval;
        }
    }

    /**
     * @return the internal {@link TimeSeriesRecorder} used during simulation
     */
    public TimeSeriesRecorder getRecorder() { return recorder; }

    /**
     * @return the current simulation time (in time units)
     */
    public double getNow() { return now; }

    /**
     * @return the current aggregate traffic rate across all ON sources
     */
    public double getAggregateRate() { return aggregateRate; }
}