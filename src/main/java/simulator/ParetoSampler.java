package simulator;

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
     * <p>This method uses inverse transform sampling, which works by:</p>
     * <ol>
     *     <li>Drawing a uniform random variable U ~ Uniform(0, 1).</li>
     *     <li>Applying the inverse cumulative distribution function (CDF) of the Pareto distribution</li>
     * </ol>
     * <pre>
     *     X = xm * (1 - U)^(-1/alpha)
     * </pre>
     * <p>This transforms U into a value X that follows the desired heavy-tailed
     * Pareto distribution.</p>
     *
     * <p>The Pareto distribution is defined by:</p>
     * <ul>
     *     <li><b>xm</b>: scale parameter (minimum possible value)</li>
     *     <li><b>alpha</b>: shape parameter controlling tail heaviness</li>
     * </ul>
     * Inverse transform sampling of a pareto distribution.
     * @param alpha The shape parameter, (how heavy is the tail).
     * @param xm The minimum possible value (scale)
     * @param rng A random number generator used to produce a sample.
     * @return A random number sampled from a pareto(alpha,xm) distribution.
     */
    public static double sample(double alpha, double xm, Random rng) {
        if (alpha <= 0 || xm <= 0) throw new IllegalArgumentException("alpha, xm > 0 required");

        // Generate U in (0,1]. Using 1 - nextDouble() avoids taking log(0) or 1/0.
        double u = 1.0 - rng.nextDouble();

        // Apply the inverse Pareto CDF.
        return xm / Math.pow(u, 1.0 / alpha);
    }
}
