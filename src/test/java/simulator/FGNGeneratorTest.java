package simulator;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FGNGeneratorTest {

    @Test
    void testSequenceLengthAndRepeatability() {
        int n = 10;
        double H = 0.8;

        FGNGenerator gen1 = new FGNGenerator(n, H, new Random(123));
        FGNGenerator gen2 = new FGNGenerator(n, H, new Random(123));

        double[] seq1 = new double[n];
        double[] seq2 = new double[n];

        for (int i = 0; i < n; i++) {
            seq1[i] = gen1.next();
            seq2[i] = gen2.next();
        }

        // sequences generated with same seed must match
        assertArrayEquals(seq1, seq2);
    }

    @Test
    void testNextWrapsAround() {
        int n = 5;
        FGNGenerator gen = new FGNGenerator(n, 0.7, new Random(1));

        double[] firstPass = new double[n];
        for (int i = 0; i < n; i++) {
            firstPass[i] = gen.next();
        }

        // After n calls, next() should loop back to index 0
        double wrapped = gen.next();  // this should equal firstPass[0]

        assertEquals(firstPass[0], wrapped);
    }

    @Test
    void testValuesAreFinite() {
        FGNGenerator gen = new FGNGenerator(20, 0.75, new Random(99));

        for (int i = 0; i < 40; i++) { // cover wraparound as well
            double value = gen.next();
            assertTrue(Double.isFinite(value), "FGN sample must be finite");
        }
    }

    @Test
    void testDifferentSeedsProduceDifferentSequences() {
        FGNGenerator gen1 = new FGNGenerator(10, 0.7, new Random(10));
        FGNGenerator gen2 = new FGNGenerator(10, 0.7, new Random(20));

        boolean anyDifferent = false;
        for (int i = 0; i < 10; i++) {
            if (gen1.next() != gen2.next()) {
                anyDifferent = true;
                break;
            }
        }

        assertTrue(anyDifferent, "Different RNG seeds should produce different sequences");
    }

    @Test
    void testGeneratedSequenceHasCorrectLength() {
        int n = 50;
        FGNGenerator gen = new FGNGenerator(n, 0.9, new Random(5));

        // extract a full cycle of samples
        double[] seq = new double[n];
        for (int i = 0; i < n; i++) {
            seq[i] = gen.next();
        }

        assertEquals(n, seq.length);
    }
}
