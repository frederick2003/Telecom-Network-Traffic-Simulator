package main.java.simulator;

import java.util.Random;

public class FGNGenerator {

    private final int n;
    private final double H;
    private final Random rng;
    private double[] fgn;
    private int index = 0;

    public FGNGenerator(int n, double H, Random rng) {
        this.n = n;
        this.H = H;
        this.rng = rng;

        this.fgn = generateFGN(n, H);
    }

    // Return next sample in the generated sequence
    public double next() {
        double value = fgn[index++];
        if (index >= n) index = 0; // wrap around (cyclic)
        return value;
    }

    // ------------------------
    // Davies-Harte Algorithm
    // ------------------------
    private double[] generateFGN(int n, double H) {

        int m = 1;
        while (m < 2 * n) m *= 2;

        double[] r = new double[m];
        double[] lambda = new double[m];

        // step 1: Compute covariance for FGN increments:
        for (int k = 0; k < n; k++) {
            r[k] = 0.5 * (Math.pow(k + 1, 2 * H) - 2 * Math.pow(k, 2 * H) + Math.pow(Math.abs(k - 1), 2 * H));
        }
        r[0] = 1.0; // variance

        // mirror
        for (int k = n; k < m; k++) {
            r[k] = r[m - k];
        }

        // step 2: FFT (real)
        lambda = FFTUtil.realFFT(r);

        // step 3: generate Gaussian values multiplied by sqrt(lambda)
        double[] W = new double[m];
        for (int k = 0; k < m; k++) {
            W[k] = Math.sqrt(lambda[k] / m) * rng.nextGaussian();
        }

        // step 4: inverse FFT
        double[] X = FFTUtil.realIFFT(W);

        // take first n values as FGN samples
        double[] out = new double[n];
        System.arraycopy(X, 0, out, 0, n);
        return out;
    }
}
