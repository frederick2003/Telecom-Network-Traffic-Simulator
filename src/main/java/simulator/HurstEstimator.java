package simulator;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for estimating the Hurst Parameter H of a time-series using Rescaled Range (R/S) analysis method.
 * <p>Read more here: <a href="https://en.wikipedia.org/wiki/Hurst_exponent">Hurst Exponent Wikipedia</a></p>
 *
 * <p>This implementation computes R/S statistics over multiple window sizes,
 *  applies a log–log transform, and performs linear regression to estimate the
 *  slope. This slope corresponds to the Hurst exponent.</p>
 */
public class HurstEstimator {

    /**
     * Estimates the Hurst parameter H from a list of time-series samples using the Rescaled Range (R/S) method.
     *
     * @param data the time-series values to analyse. Must contain at least 10 samples.
     * @return the estimated hurst parameter, or 0.5 if insufficient data
     */
    public static double estimateHurst(List<Double> data) {

        int N = data.size();
        if (N < 10) {
            return 0.5; // Default Value
        }

        List<Integer> windowSizes = List.of(
                10, 20, 50, 100, 200, 500, 1000
        );

        List<Double> logN = new ArrayList<>();
        List<Double> logRS = new ArrayList<>();

        for (int n : windowSizes) {
            if (n >= N) break;

            double rs = computeRS(data, n);
            if (rs > 0) {
                logN.add(Math.log(n));
                logRS.add(Math.log(rs));
            }
        }
        return linearRegressionSlope(logN, logRS);
    }

    /**
     * Computes the mean Rescaled Range (R/S) statistic for a given window size.
     *
     *
     * @param data the full dataset.
     * @param windowSize the size of each segment for R/S analysis.
     * @return the average R/S statistic across all complete segments.
     */
    private static double computeRS(List<Double> data, int windowSize) {
        int N = data.size();
        int numSegments = N / windowSize;

        double totalRS = 0;

        for (int s = 0; s < numSegments; s++) {
            int start = s * windowSize;
            int end = start + windowSize;

            List<Double> segment = data.subList(start, end);

            double mean = segment.stream().mapToDouble(v -> v).average().orElse(0);

            double cumulative = 0;
            double minCum = Double.MAX_VALUE;
            double maxCum = Double.MIN_VALUE;

            List<Double> deviations = new ArrayList<>();
            for (double v : segment) {
                cumulative += (v - mean);
                deviations.add(cumulative);
                minCum = Math.min(minCum, cumulative);
                maxCum = Math.max(maxCum, cumulative);
            }

            double R = maxCum - minCum;
            double S = segment.stream().mapToDouble(v -> Math.pow(v - mean, 2)).sum();
            S = Math.sqrt(S / windowSize);

            if (S != 0) {
                totalRS += (R / S);
            }
        }
        return totalRS / numSegments;
    }

    /**
     * Performs a simple linear regression on the input x and y values and returns the slope of the line of best-fit.
     *
     * <p>This regression is used to estimate the Hurst exponent as the slope of the log(R/S) versus log(window size) curve</p>
     *
     * @param x the list of log window sizes.
     * @param y the list of log R/S statistics corresponding to the same indices in {@code x}
     * @return the slope of the fitted regression line.
     */
    private static double linearRegressionSlope(List<Double> x, List<Double> y) {
        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumXX += x.get(i) * x.get(i);
        }

        return (n * sumXY - sumX * sumY) /
                (n * sumXX - sumX * sumX);
    }
}
