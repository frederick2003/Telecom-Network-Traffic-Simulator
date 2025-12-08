package test.java.simulator;

import main.java.simulator.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationManager focusing on:
 *  - Seeding initial events
 *  - Event scheduling order
 *  - Aggregate rate updates after ON/OFF transitions
 *  - Basic progression of simulation loop
 *
 * Complex subsystems (StatisticsManager, TimeSeriesRecorder, SimpleQueue)
 * are replaced with minimal stub versions so only core simulation logic is tested.
 */
public class SimulationManagerTest {

    // A modified SimulationManager with stubs for dependencies.
    private SimulationManager manager;

    @BeforeEach
    void init() {
        manager = new SimulationManagerWithStubs();
    }

    /** Verifies that adding a source stores it internally. */
    @Test
    void testAddSource() {
        TrafficSource ts = new TrafficSource(
                0, 10.0,
                2, 1,
                2, 1,
                1L,
                TrafficModelType.PARETO,
                0.8
        );

        manager.addSource(ts);
        assertEquals(1, manager.getRecorder().getEventCount(),
                "Recorder stub increments on addSource()");
    }

    /** Ensures seedInitialEvents schedules 1 event per source. */
    @Test
    void testSeedInitialEventsSchedulesEvents() {
        TrafficSource ts1 = makeSource(0);
        TrafficSource ts2 = makeSource(1);

        manager.addSource(ts1);
        manager.addSource(ts2);

        SimulationManagerWithStubs m = (SimulationManagerWithStubs) manager;
        m.seedInitialEvents();

        assertEquals(2, m.eventQ.size(),
                "Each source must schedule exactly 1 initial event.");
    }

    /** Ensures run() actually polls events and advances simulation time. */
    @Test
    void testRunAdvancesSimulationTime() {
        TrafficSource ts1 = makeSource(0);
        manager.addSource(ts1);

        manager.run(50.0);

        double now = manager.getNow();
        assertTrue(now > 0.0,
                "Simulation time must advance after processing events.");
    }

    /** Ensures aggregate rate increments when a source turns ON. */
    @Test
    void testAggregateRateIncreasesOnSourceOn() {
        TrafficSource ts1 = makeSource(0);
        manager.addSource(ts1);

        // Seed and manually execute one event
        SimulationManagerWithStubs m = (SimulationManagerWithStubs) manager;
        m.seedInitialEvents();

        Event e = m.eventQ.poll();  // First SOURCE_ON event

        // Process event
        m.invokeHandle(e);

        assertEquals(ts1.getOnRate(), manager.getAggregateRate(),
                "Aggregate rate should equal source ON rate.");
    }

    /** Ensures aggregate rate decreases when source turns OFF. */
    @Test
    void testAggregateRateDecreasesOnSourceOff() {
        TrafficSource ts1 = makeSource(0);
        manager.addSource(ts1);

        SimulationManagerWithStubs m = (SimulationManagerWithStubs) manager;
        m.seedInitialEvents();

        // First event turns it ON
        Event onEvent = m.eventQ.poll();
        m.invokeHandle(onEvent);
        assertEquals(ts1.getOnRate(), manager.getAggregateRate());

        // Next event turns it OFF
        Event offEvent = m.eventQ.poll();
        m.invokeHandle(offEvent);

        assertEquals(0.0, manager.getAggregateRate(),
                "Aggregate rate should drop to zero after OFF transition.");
    }

    // -------------------------
    // Helper Methods
    // -------------------------

    private TrafficSource makeSource(int id) {
        return new TrafficSource(
                id,               // ID
                10.0,             // ON rate
                2.0, 1.0,         // Pareto ON params
                2.0, 1.0,         // Pareto OFF params
                123L + id,        // seed
                TrafficModelType.PARETO,
                0.7               // Hurst (unused)
        );
    }

    // ---------------------------------------
    // Stubbed version of SimulationManager
    // ---------------------------------------
    private static class SimulationManagerWithStubs extends SimulationManager {

        // Expose event queue for assertions
        private final PriorityQueue<Event> eventQExposed = super.eventQ;

        @Override
        public void seedInitialEvents() {
            super.seedInitialEvents();
        }

        /** Allows tests to call the private handle() safely. */
        public void invokeHandle(Event e) {
            super.handle(e);
        }

        @Override
        public TimeSeriesRecorder getRecorder() {
            return new RecorderStub();
        }

        PriorityQueue<Event> eventQ() {
            return eventQExposed;
        }
    }

    // -------------------------
    // Simple Stub Classes
    // -------------------------

    /** Recorder stub that only tracks event count. */
    private static class RecorderStub extends TimeSeriesRecorder {
        private int count = 0;

        @Override
        public void record(double time, double rate) {
            count++;
        }

        public int getEventCount() {
            return count;
        }
    }
}
