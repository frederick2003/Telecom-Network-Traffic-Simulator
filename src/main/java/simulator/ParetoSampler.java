package main.java.simulator;

import java.util.Random;

/**
 * Utility class for generating samples from a Pareto distribution using
 * the inverse transform sampling method. The Pareto distribution is used
 * in this project to model heavy-tailed ON/OFF durations for traffic sources.
 */
public class ParetoSampler {
    private ParetoSampler() {}

    /**
     * Generates a random sample from a Pareto(alpha, xm) distribution.
     *
     * This method uses inverse transform sampling, which works by:
     *   1. Drawing a uniform random variable U ~ Uniform(0, 1).
     *   2. Applying the inverse cumulative distribution function (CDF)
     *      of the Pareto distribution:
     *
     *          X = xm * (1 - U)^(-1/alpha)
     *
     *      This transforms U into a value X that follows the desired
     *      heavy-tailed Pareto distribution.
     *
     * The Pareto distribution has:
     *    - Minimum value xm (scale parameter)
     *    - Shape parameter alpha controlling tail heaviness
     *
     * Inverse transform sampling of a pareto distribution.
     * @param alpha the shape parameter, (how heavy is the tail).
     * @param xm minimum possible value
     * @param rng random number generator.
     * @return a random number that follows a pareto distribution.
     */
    public static double sample(double alpha, double xm, Random rng) {
        if (alpha <= 0 || xm <= 0) throw new IllegalArgumentException("alpha, xm > 0 required");

        // Generate U in (0,1]. Using 1 - nextDouble() avoids taking log(0) or 1/0.
        double u = 1.0 - rng.nextDouble();

        // Apply the inverse Pareto CDF.
        return xm / Math.pow(u, 1.0 / alpha);
    }
}
