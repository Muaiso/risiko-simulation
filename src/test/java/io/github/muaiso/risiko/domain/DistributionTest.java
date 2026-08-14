package io.github.muaiso.risiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die vier implementierten Wahrscheinlichkeitsverteilungen:
 * Erwartungswerte, Grenzen und grundlegende Sampling-Eigenschaften.
 */
class DistributionTest {

    @Test
    void normalExpectedValueIsMu() {
        var d = new NormalDistribution(100.0, 10.0);
        assertEquals(100.0, d.expectedValue(), 1e-9);
    }

    @Test
    void normalRejectsNegativeSigma() {
        assertThrows(IllegalArgumentException.class, () -> new NormalDistribution(0, -1));
    }

    @Test
    void normalBoundsAreSixSigma() {
        var d = new NormalDistribution(50.0, 5.0);
        assertEquals(20.0, d.minimum(), 1e-9);
        assertEquals(80.0, d.maximum(), 1e-9);
    }

    @Test
    void normalSampleHasRoughlyUnitVariance() {
        var d = new NormalDistribution(0.0, 1.0);
        var rng = new StandardNormalRandomSource(new java.util.SplittableRandom(42L));
        int n = 50_000;
        double sum = 0.0;
        double sumSq = 0.0;
        for (int i = 0; i < n; i++) {
            double v = d.sample(rng);
            sum += v;
            sumSq += v * v;
        }
        double mean = sum / n;
        double variance = sumSq / n - mean * mean;
        // empirische Varianz einer Standardnormalverteilung ~ 1 (Toleranz 0.1)
        assertTrue(Math.abs(variance - 1.0) < 0.1, "empirische Varianz sollte ~1 sein, war " + variance);
    }

    @Test
    void uniformExpectedValueIsMidpoint() {
        var d = new UniformDistribution(10.0, 30.0);
        assertEquals(20.0, d.expectedValue(), 1e-9);
    }

    @Test
    void uniformRejectsInvertedBounds() {
        assertThrows(IllegalArgumentException.class, () -> new UniformDistribution(5, 1));
    }

    @Test
    void uniformSampleWithinBounds() {
        var d = new UniformDistribution(0.0, 1.0);
        var rng = new StandardNormalRandomSource(new java.util.SplittableRandom(7L));
        for (int i = 0; i < 500; i++) {
            double v = d.sample(rng);
            assertTrue(v >= 0.0 && v <= 1.0, "Uniform-Probe muss in [0,1] liegen");
        }
    }

    @Test
    void triangularExpectedValueIsAverageOfThree() {
        var d = new TriangularDistribution(0.0, 10.0, 5.0);
        assertEquals(5.0, d.expectedValue(), 1e-9);
    }

    @Test
    void triangularRejectsModeOutsideRange() {
        assertThrows(IllegalArgumentException.class, () -> new TriangularDistribution(0, 10, 11));
    }

    @Test
    void triangularSampleWithinBounds() {
        var d = new TriangularDistribution(-20.0, 20.0, 0.0);
        var rng = new StandardNormalRandomSource(new java.util.SplittableRandom(99L));
        for (int i = 0; i < 500; i++) {
            double v = d.sample(rng);
            assertTrue(v >= -20.0 && v <= 20.0);
        }
    }

    @Test
    void logNormalRejectsNonPositiveMedian() {
        assertThrows(IllegalArgumentException.class, () -> new LogNormalDistribution(0, 2));
    }

    @Test
    void logNormalRejectsSpreadBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new LogNormalDistribution(100, 0.5));
    }

    @Test
    void logNormalExpectedValueAboveMedian() {
        // Rechtsschiefe Verteilung: E[X] > Median
        var d = new LogNormalDistribution(100.0, 2.0);
        assertTrue(d.expectedValue() > 100.0, "E[X] muss ueber dem Median liegen");
    }

    @Test
    void logNormalSampleAlwaysPositive() {
        var d = new LogNormalDistribution(50.0, 3.0);
        var rng = new StandardNormalRandomSource(new java.util.SplittableRandom(3L));
        for (int i = 0; i < 500; i++) {
            assertTrue(d.sample(rng) > 0.0, "LogNormal-Probe muss strikt positiv sein");
        }
    }
}
