package simulator;

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
    // ------------------------
// Hosking-style FGN Generator (no FFT required)
// ------------------------
    private double[] generateFGN(int n, double H) {

        double[] x = new double[n];       // output FGN samples
        double[] gamma = new double[n];   // autocovariance γ(k)

        // 1. Autocovariance function of FGN increments
        //    γ(k) = 0.5 ( |k+1|^{2H} - 2|k|^{2H} + |k-1|^{2H} ), k >= 0
        for (int k = 0; k < n; k++) {
            double kPlus  = Math.pow(k + 1, 2 * H);
            double kZero  = Math.pow(k, 2 * H);
            double kMinus = (k == 0) ? 0.0 : Math.pow(k - 1, 2 * H); // avoid (-1)^(2H)
            gamma[k] = 0.5 * (kPlus - 2.0 * kZero + kMinus);
        }

        // Optional: normalise so that variance = 1 at lag 0
        double var0 = gamma[0];
        if (var0 <= 0) {
            // Should not happen for 0 < H < 1, but guard anyway
            var0 = 1e-8;
            gamma[0] = var0;
        }

        // 2. Hosking / Durbin–Levinson recursion
        double[] phiPrev = new double[n]; // φ_{k-1}(j)
        double[] phiCurr = new double[n]; // φ_{k}(j)

        // initial variance
        double v = gamma[0];

        // k = 0 term
        phiPrev[0] = 1.0;
        x[0] = Math.sqrt(v) * rng.nextGaussian();

        // k = 1..n-1
        for (int k = 1; k < n; k++) {

            // 2.1 Compute θ_k
            double sum = 0.0;
            for (int j = 0; j < k; j++) {
                sum += phiPrev[j] * gamma[k - j];
            }
            double theta = (gamma[k] - sum) / v;

            // 2.2 Update φ_k(j) for j = 0..k
            for (int j = 0; j < k; j++) {
                phiCurr[j] = phiPrev[j] - theta * phiPrev[k - 1 - j];
            }
            phiCurr[k] = theta;

            // 2.3 Update conditional variance
            double newVar = v * (1.0 - theta * theta);
            if (newVar < 1e-12) {
                newVar = 1e-12; // numerical safety
            }

            // 2.4 Generate X_k
            double mean = 0.0;
            for (int j = 0; j < k; j++) {
                mean += phiCurr[j] * x[j];
            }
            x[k] = mean + Math.sqrt(newVar) * rng.nextGaussian();

            // 2.5 Prepare for next iteration
            v = newVar;
            double[] tmp = phiPrev;
            phiPrev = phiCurr;
            phiCurr = tmp;
        }

        return x;
    }


}
