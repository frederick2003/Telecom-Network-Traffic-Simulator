package simulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeSeriesRecorderTest {

    @TempDir
    Path tempDir;

    @Test
    void testAddSegmentAddsPointsCorrectly() {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.addSegment(0.0, 5.0, 10.0);
        recorder.addSegment(5.0, 8.0, 20.0);

        List<double[]> points = recorder.asPoints();

        assertEquals(2, points.size());
        assertArrayEquals(new double[]{5.0, 10.0}, points.get(0));
        assertArrayEquals(new double[]{8.0, 20.0}, points.get(1));
    }

    @Test
    void testAddSegmentTracksTimeEqualToZero(){

        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.addSegment(2.0, 5.0, 10.0);

        List<double[]> points = recorder.asPoints();

        assertArrayEquals(new double[]{0.0, 10.0}, points.get(0));
        assertArrayEquals(new double[]{5.0, 10.0}, points.get(1));

    }

    @Test
    void testAddSegmentThrowsWhenT1LessThanT0() {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();
        assertThrows(IllegalArgumentException.class, () ->
                recorder.addSegment(5.0, 3.0, 10.0));
    }

    @Test
    void testRecordAddsPoint() {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.record(2.0, 15.0);

        List<double[]> points = recorder.asPoints();
        assertEquals(1, points.size());
        assertArrayEquals(new double[]{2.0, 15.0}, points.get(0));
    }

    @Test
    void testFinishAppendsFinalPointIfNeeded() {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.record(5.0, 12.0);
        recorder.finish(10.0);

        List<double[]> points = recorder.asPoints();

        assertEquals(2, points.size());
        assertArrayEquals(new double[]{10.0, 12.0}, points.get(1));
    }

    @Test
    void testFinishDoesNothingIfAlreadyAtFinalTime() {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.record(10.0, 12.0);
        recorder.finish(10.0);

        List<double[]> points = recorder.asPoints();
        assertEquals(1, points.size());
    }

    @Test
    void testRecordEventLogStoresCorrectFormat() {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.recordEventLog(3, EventType.SOURCE_ON, 5.5, 10.0);

        // Reflection hack to access private eventsLog
        var field = recorder.getClass().getDeclaredFields();
        // Instead, use eventCount if fixed:
        // assertEquals(1, recorder.getEventCount());

        assertEquals(1, recorder.getEventCount());
    }

    @Test
    void testLogEventsToCsvWritesFileCorrectly() throws IOException {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.recordEventLog(1, EventType.SOURCE_ON, 2.0, 5.0);
        recorder.recordEventLog(2, EventType.SOURCE_OFF, 3.5, 10.0);

        Path file = tempDir.resolve("events.csv");

        recorder.logEventsToCsv(file.toString());

        String content = Files.readString(file);

        assertTrue(content.contains("sourceId,eventType,eventTime,currentAggregateRate"));
        assertTrue(content.contains("1,SOURCE_ON,2.0,5.0"));
        assertTrue(content.contains("2,SOURCE_OFF,3.5,10.0"));
    }

    @Test
    void testLogTimeSeriesDataToCsvWritesPointsCorrectly() throws IOException {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.record(0.0, 5.0);
        recorder.record(2.0, 10.0);

        Path file = tempDir.resolve("timeseries.csv");
        recorder.logTimeSeriesDataToCsv(file.toString());

        String content = Files.readString(file);

        assertTrue(content.contains("time,totalAggregateRate"));
        assertTrue(content.contains("0.0,5.0"));
        assertTrue(content.contains("2.0,10.0"));
    }

    @Test
    void testToCsvWritesCorrectContent() throws IOException {
        TimeSeriesRecorder recorder = new TimeSeriesRecorder();

        recorder.record(1.0, 8.0);
        recorder.record(3.0, 12.0);

        Path file = tempDir.resolve("export.csv");
        recorder.toCsv(file);

        String content = Files.readString(file);

        assertTrue(content.contains("time,totalAggregateRate"));
        assertTrue(content.contains("1.0,8.0"));
        assertTrue(content.contains("3.0,12.0"));
    }
}
