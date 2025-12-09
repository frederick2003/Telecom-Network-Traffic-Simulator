package simulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticsManagerTest {

    @TempDir
    Path tempDir;

    // Dummy TrafficSource (not used directly by StatsManager)
    static class DummySource extends TrafficSource {
        public DummySource() { super(0, 0, 0, 0, 0, 0, 0L, TrafficModelType.PARETO, 0.8); }
    }

    @Test
    void testUpdateSimulationStatisticsUpdatesTotalsAndPeakTraffic() {
        StatisticsManager stats = new StatisticsManager();
        DummySource source = new DummySource();
        Event event1 = new Event(5.0, 1, EventType.SOURCE_ON);
        Event event2 = new Event(8.0, 1, EventType.SOURCE_ON);

        // First event at time 5 with aggregate rate 10.0
        stats.updateSimulationStatistics(event1, source, 10.0);

        assertEquals(1, stats.getTotalEvents());
        assertEquals(10.0, stats.getPeakTraffic());
        assertEquals(0.0, stats.getTotalSimulationTime()); // finalised later

        // Second event at time 8 with aggregate rate 25.0
        stats.updateSimulationStatistics(event2, source, 25.0);

        assertEquals(2, stats.getTotalEvents());
        assertEquals(25.0, stats.getPeakTraffic()); // peak updated
    }

    @Test
    void testTimeWeightedAverageCalculation() {
        StatisticsManager stats = new StatisticsManager();
        DummySource source = new DummySource();
        Event event1 = new Event(5.0,1,EventType.SOURCE_ON);
        Event event2 = new Event(10.0, 1, EventType.SOURCE_ON);

        // First event at t=5 with rate=10
        stats.updateSimulationStatistics(event1, source, 10.0);
        // Area from t=0 to t=5 uses initial rate = 0, contributes 0

        // Second event at t=10 with rate=20
        stats.updateSimulationStatistics(event2, source, 20.0);
        // Area from t=5 to t=10: 10 * 5 = 50

        // Finalise at t=20
        stats.finaliseStatistics(20.0);
        // Area from t=10 to t=20: 20 * 10 = 200
        // Total area = 50 + 200 = 250
        // Average = area / totalTime = 250 / 20 = 12.5

        assertEquals(20.0, stats.getTotalSimulationTime());
        assertEquals(12.5, stats.getAverageTraffic());
    }

    @Test
    void testUpdateQueueStatsTracksMaxLength() {
        StatisticsManager stats = new StatisticsManager();

        stats.updateQueueStats(5.0, 0.0);
        stats.updateQueueStats(10.0, 2.0);
        stats.updateQueueStats(7.0, 4.0);

        assertEquals(10.0, stats.getMaxQueueLength());
        assertEquals(4.0, stats.getTotalDroppedPackets());
    }

    @Test
    void testRecordAggregateRateAndComputeHurst() {
        StatisticsManager stats = new StatisticsManager();

        stats.recordAggregateRate(5.0);
        stats.recordAggregateRate(10.0);
        stats.recordAggregateRate(20.0);

        // Uses stub HurstEstimator → returns predictable 0.75
        stats.computeHurst();

        // We cannot access hurstParameter directly but CSV export will show it.
        Path file = tempDir.resolve("summary.csv");
        stats.logSummaryStatsToCsv(file.toString());

        String content = "";
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            fail("Failed to read written summary CSV");
        }

        assertTrue(content.contains("0.75")); // Hurst appears in CSV
    }

    @Test
    void testLogSummaryStatsToCsvWritesCorrectValues() throws IOException {
        StatisticsManager stats = new StatisticsManager();
        DummySource source = new DummySource();
        Event event = new Event(5.0,1, EventType.SOURCE_ON);

        // Simulate one event
        stats.updateSimulationStatistics(event, source, 12.0);
        stats.finaliseStatistics(10.0);

        // Compute Hurst with stub
        stats.recordAggregateRate(10.0);
        stats.computeHurst();

        Path file = tempDir.resolve("summary_stats.csv");
        stats.logSummaryStatsToCsv(file.toString());

        String csv = Files.readString(file);

        assertTrue(csv.contains("Total Events"));
        assertTrue(csv.contains("1")); // one event processed
        assertTrue(csv.contains("12.0")); // peak traffic
        assertTrue(csv.contains(String.valueOf(stats.getAverageTraffic())));
        assertTrue(csv.contains("0.75")); // stub Hurst value
    }
}
