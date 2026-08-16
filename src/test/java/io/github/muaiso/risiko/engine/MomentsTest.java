package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die zentralen Momente (Schiefe, Exzess-Wölbung) an bekannten
 * symmetrischen und asymmetrischen Verteilungen.
 */
class MomentsTest {

    @Test
    void symmetricDistributionHasZeroSkewness() {
        double[] values = {-3, -2, -1, 0, 1, 2, 3};
        assertEquals(0.0, Moments.skewness(values), 1e-9);
    }

    @Test
    void rightSkewedDistributionHasPositiveSkewness() {
        // Verteilung mit langem rechten Schwanz
        double[] values = {-2, -1, 0, 1, 2, 3, 10};
        double skew = Moments.skewness(values);
        assertTrue(skew > 0.0, "rechtsschief erwartet");
    }

    @Test
    void normalLikeDistributionHasNearZeroExcessKurtosis() {
        // Gleichverteilte Stichprobe: nicht schwanzlastig (Exzess < 1)
        double[] values = new double[1000];
        for (int i = 0; i < 1000; i++) {
            values[i] = i;
        }
        double kurt = Moments.excessKurtosis(values);
        assertTrue(kurt < 1.0, "Gleichverteilung ist nicht schwanzlastig");
    }

    @Test
    void tooFewSamplesReturnZero() {
        assertEquals(0.0, Moments.skewness(new double[]{1.0}), 1e-9);
        assertEquals(0.0, Moments.excessKurtosis(new double[]{1.0, 2.0}), 1e-9);
    }
}
