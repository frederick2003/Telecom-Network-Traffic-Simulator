package simulator;

public class FFTUtil {


    public static double[] realFFT(double[] x) {
        int n = x.length;
        double[] real = new double[n];
        double[] imag = new double[n];

        // Copy input
        System.arraycopy(x, 0, real, 0, n);

        fft(real, imag);

        // magnitude only for Davies-Harte
        double[] power = new double[n];
        for (int i = 0; i < n; i++) {
            power[i] = real[i] * real[i] + imag[i] * imag[i];
        }

        return power;
    }

    public static double[] realIFFT(double[] spectrum) {
        int n = spectrum.length;
        double[] real = new double[n];
        double[] imag = new double[n];

        // Only magnitude is used, generate imaginary = 0
        for (int i = 0; i < n; i++) {
            real[i] = Math.sqrt(spectrum[i]);
        }

        ifft(real, imag);
        return real;
    }

    private static void fft(double[] real, double[] imag) {
        int n = real.length;

        // bit-reverse
        int shift = 1 + Integer.numberOfLeadingZeros(n);
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> shift;
            if (j > i) {
                double tmp = real[i]; real[i] = real[j]; real[j] = tmp;
                tmp = imag[i]; imag[i] = imag[j]; imag[j] = tmp;
            }
        }

        // butterflies
        for (int size = 2; size <= n; size *= 2) {
            double angle = -2 * Math.PI / size;
            double wMulReal = Math.cos(angle);
            double wMulImag = Math.sin(angle);

            for (int i = 0; i < n; i += size) {
                double wReal = 1;
                double wImag = 0;

                for (int j = 0; j < size/2; j++) {
                    int even = i + j;
                    int odd = even + size/2;

                    double r = wReal * real[odd] - wImag * imag[odd];
                    double im = wReal * imag[odd] + wImag * real[odd];

                    real[odd] = real[even] - r;
                    imag[odd] = imag[even] - im;

                    real[even] += r;
                    imag[even] += im;

                    double tmpReal = wReal * wMulReal - wImag * wMulImag;
                    wImag = wReal * wMulImag + wImag * wMulReal;
                    wReal = tmpReal;
                }
            }
        }
    }

    private static void ifft(double[] real, double[] imag) {
        int n = real.length;

        for (int i = 0; i < n; i++) imag[i] = -imag[i]; // conjugate
        fft(real, imag);
        for (int i = 0; i < n; i++) { real[i] /= n; imag[i] = -imag[i] / n; }
    }
}
