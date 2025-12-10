package simulator;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HurstEstimatorTest {

    @Test
    void testInsufficientDataReturnsDefault() {
        List<Double> data = List.of(1.0, 2.0, 3.0); // < 10 samples

        double hurst = HurstEstimator.estimateHurst(data);

        assertEquals(0.5, hurst, 1e-9);
    }

    @Test
    void testEstimateHurstReturnsFiniteNumber() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            data.add(Math.sin(i * 0.1)); // oscillating but valid signal
        }

        double hurst = HurstEstimator.estimateHurst(data);

        assertTrue(Double.isFinite(hurst));
    }

    @Test
    void testHurstOnIncreasingSeriesIsHigh() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            data.add((double) i); // strictly increasing trend → persistent signal
        }

        double hurst = HurstEstimator.estimateHurst(data);

        // Should be significantly above 0.5 for a trending series
        assertTrue(hurst > 0.7, "Expected high H for strong positive trend");
    }

    @Test
    void testHurstOnWhiteNoiseIsNearRandom() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            data.add(Math.random()); // uncorrelated random noise
        }

        double hurst = HurstEstimator.estimateHurst(data);

        // Should be around 0.5 ± tolerance for white noise
        assertTrue(hurst > 0.3 && hurst < 0.7, "Expected roughly 0.5 for noise");
    }

    @Test
    void testLinearRegressionSlope() throws Exception {
        // Use reflection to call the private method
        var method = HurstEstimator.class.getDeclaredMethod(
                "linearRegressionSlope",
                List.class, List.class
        );
        method.setAccessible(true);

        List<Double> x = List.of(1.0, 2.0, 3.0);
        List<Double> y = List.of(2.0, 4.0, 6.0); // perfect slope = 2

        double slope = (double) method.invoke(null, x, y);

        assertEquals(2.0, slope, 1e-9);
    }

    @Test
    void testComputeRSProducesPositiveValue() throws Exception {
        // Access private computeRS
        var method = HurstEstimator.class.getDeclaredMethod(
                "computeRS", List.class, int.class
        );
        method.setAccessible(true);

        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(Math.sin(i * 0.1) + Math.random() * 0.1);
        }

        double rs = (double) method.invoke(null, data, 20);

        assertTrue(rs > 0, "R/S statistic should be positive");
    }
}
