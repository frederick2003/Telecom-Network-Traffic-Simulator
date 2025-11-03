package main.java.simulator;

import java.util.Random;

public class ParetoSampler {
    private ParetoSampler() {}

    public static double sample(double alpha, double xm, Random rng) {
        if (alpha <= 0 || xm <= 0) throw new IllegalArgumentException("alpha, xm > 0 required");
        double u = 1.0 - rng.nextDouble(); // (0,1]
        return xm / Math.pow(u, 1.0 / alpha);
    }
}
