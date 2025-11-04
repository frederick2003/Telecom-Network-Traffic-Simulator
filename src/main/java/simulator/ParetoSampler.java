package main.java.simulator;

import java.util.Random;

public class ParetoSampler {
    private ParetoSampler() {}

    /**
     * Inverse transform sampling of a pareto distribution.
     * @param alpha the shape parameter, (how heavy is the tail).
     * @param xm minimum possible value
     * @param rng random number generator.
     * @return a random number that follows a pareto distribution.
     */
    public static double sample(double alpha, double xm, Random rng) {
        if (alpha <= 0 || xm <= 0) throw new IllegalArgumentException("alpha, xm > 0 required");
        double u = 1.0 - rng.nextDouble(); // draw a random number between (0,1]. Represents drawing a random probability.
        return xm / Math.pow(u, 1.0 / alpha); // preform inverse transform sampling.
    }
}
