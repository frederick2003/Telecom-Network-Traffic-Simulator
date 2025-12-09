package simulator;

import main.java.simulator.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrafficSourceTest {
    @Test
    void testConstructorInitialisation() {
        TrafficSource ts = new TrafficSource(
                5,              // id
                10.0,              // onRate
                2.5, 1.0,   // alphaOn, xmOn
                3.0, 2.0,   // alphaOff, xmOff
                42L,
                TrafficModelType.PARETO,
                0.8
        );

        assertEquals(5, ts.getId());
        assertEquals(10.0, ts.getOnRate());
        assertFalse(ts.getIsOn(), "Traffic source should start OFF.");
    }

    @Test
    void testScheduleInitialEventAlwaysSourceOn() {
        TrafficSource ts = new TrafficSource(
                1, 5,
                2, 1,
                2, 1,
                123L,
                TrafficModelType.PARETO,
                0.7
        );

        double now = 0.0;
        Event e = ts.scheduleInitialEvent(now);

        assertEquals(EventType.SOURCE_ON, e.getType());
        assertTrue(e.getTime() > now, "Initial ON event must be in the future.");
        assertEquals(1, e.getSourceId());
    }

    @Test
    void testScheduleNextEventTogglesState() {
        TrafficSource ts = new TrafficSource(
                1, 5,
                2, 1,
                2, 1,
                999L,
                TrafficModelType.PARETO,
                0.7
        );

        // Initially OFF
        assertFalse(ts.getIsOn());

        Event e1 = ts.scheduleNextEvent(10.0);
        assertTrue(ts.getIsOn(), "Expected transition: OFF -> ON");
        assertEquals(EventType.SOURCE_OFF, e1.getType());

        Event e2 = ts.scheduleNextEvent(e1.getTime());
        assertFalse(ts.getIsOn(), "Expected transition: ON -> OFF");
        assertEquals(EventType.SOURCE_ON, e2.getType());
    }

    @Test
    void testParetoSamplingDeterministic() {
        TrafficSource ts1 = new TrafficSource(
                1, 5,
                2, 1,    // ON params
                2, 1,    // OFF params
                100L,
                TrafficModelType.PARETO,
                0.7
        );

        TrafficSource ts2 = new TrafficSource(
                1, 5,
                2, 1,
                2, 1,
                100L,     // SAME SEED
                TrafficModelType.PARETO,
                0.7
        );

        double d1 = ts1.sampleOnDuration();
        double d2 = ts2.sampleOnDuration();

        assertEquals(d1, d2, 1e-9, "Pareto sampling must be deterministic with equal seeds.");
    }

    @Test
    void testFGNSamplingPositiveValues() {
        TrafficSource ts = new TrafficSource(
                1, 5,
                2, 1,
                2, 1,
                50L,
                TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE,
                0.9
        );

        // Indirectly test FGN via scheduleNextEvent (which calls sampleDuration)
        Event e1 = ts.scheduleNextEvent(0.0); // OFF → ON
        Event e2 = ts.scheduleNextEvent(e1.getTime()); // ON → OFF

        assertTrue(e1.getTime() > 0, "FGN ON duration must be > 0.");
        assertTrue(e2.getTime() > e1.getTime(), "FGN OFF duration must also be > 0.");
    }

    @Test
    void testInitialStateAlwaysOff() {
        TrafficSource ts = new TrafficSource(
                7, 3.0,
                2.3, 1.0,
                2.3, 1.0,
                1234L,
                TrafficModelType.PARETO,
                0.8
        );

        assertFalse(ts.getIsOn(), "All traffic sources must start in OFF state.");
    }
}