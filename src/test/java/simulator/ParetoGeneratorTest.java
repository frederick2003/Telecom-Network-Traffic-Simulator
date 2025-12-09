package simulator;

import java.util.Random;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the ParetoSampler class.
 * These tests focus on verifying the correctness of the inverse-transform
 * sampling method used for generating heavy-tailed ON/OFF durations.
 *
 * The Pareto distribution has three key properties:
 *  1. All values are >= xm (the scale/minimum parameter).
 *  2. The distribution has a known mean and median for a given Alpha and xm.
 *  3. Sampling with a fixed RNG seed must be deterministic.
 *
 * These tests check each of these properties to ensure statistical correctness.
 */

public class ParetoGeneratorTest {


     /**
     * Deterministic output test.
     * With a fixed RNG seed, the sampler must produce the same value on
     * every run. This protects against accidental changes to the sampling
     * formula and ensures reproducibility during debugging.
     */

    @Test
    public void testParetoDeterministicWithSeed() {
        Random rng = new Random(12345);
        double x = ParetoSampler.sample(1.5, 1.0, rng);

        assertEquals(1.3490567202955195, x, 1e-12);
    }

     /**
     * Basic validity test.
     * All Pareto samples must be greater than or equal to xm.
     * This checks that the inverse-CDF implementation never produces values
     * outside the support of the Pareto distribution.
     */

    @Test
    public void testSamplesAboveMinimum(){
        Random rng = new Random();
        double alpha = 1.5, xm = 2.0;

        for (int i = 0; i < 100000; i++) {
            double x = ParetoSampler.sample(alpha, xm, rng);
            assertTrue(x >= xm);
        }
    }

     /**
     * Statistical mean test.
     * For alpha = 2.0 and xm = 1.0, the theoretical mean is:
     *
     *      E[X] = alpha * xm / (alpha - 1) = 2
     *
     * By generating a large number of samples, the empirical mean should
     * converge reasonably close to this value. A tolerance of 5% is used
     * due to randomness and the heavy-tailed nature of the distribution.
     */

    @Test
    public void testParetoMean() {
        Random rng = new Random(12345);
        double alpha = 2.0;
        double xm = 1.0;

        int N = 500000;
        double sum = 0;

        for (int i = 0; i < N; i++) {
            sum += ParetoSampler.sample(alpha, xm, rng);
        }

        double empiricalMean = sum / N;
        double theoreticalMean = (alpha * xm) / (alpha - 1);

        assertEquals(theoreticalMean, empiricalMean, 0.05 * theoreticalMean);
    }

     /**
     * Statistical median test.
     * The Pareto median is given by:
     *
     *      median = xm * 2^(1/alpha)
     *
     * This test verifies that the sampled median converges to this value
     * over a large number of samples. Again, a tolerance is applied because
     * sampling involves natural random variation.
     */

    @Test
    public void testParetoMedian() {
        Random rng = new Random(12345);
        double alpha = 1.5;
        double xm = 1.0;

        int N = 200000;
        double[] samples = new double[N];

        for (int i = 0; i < N; i++) {
            samples[i] = ParetoSampler.sample(alpha, xm, rng);
        }

        Arrays.sort(samples);
        double empiricalMedian = samples[N/2];
        double theoreticalMedian = xm * Math.pow(2.0, 1.0 / alpha);

        assertEquals(theoreticalMedian, empiricalMedian, 0.05 * theoreticalMedian);
    }
}
