package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die deskriptive Statistik: Mittelwert, Varianz, Standardabweichung
 * und Perzentil-Interpolation an Hand bekannten Daten.
 */
class DescriptiveStatisticsTest {

    @Test
    void meanAndVarianceOfKnownSet() {
        double[] data = {1, 2, 3, 4, 5};
        var stats = new DescriptiveStatistics(data);
        assertEquals(3.0, stats.getMean(), 1e-9);
        // population variance = 2.0
        assertEquals(2.0, stats.getVariance(), 1e-9);
        assertEquals(Math.sqrt(2.0), stats.getStandardDeviation(), 1e-9);
    }

    @Test
    void minAndMaxAreExtremes() {
        double[] data = {-5, 0, 3, 99};
        var stats = new DescriptiveStatistics(data);
        assertEquals(-5.0, stats.getMin(), 1e-9);
        assertEquals(99.0, stats.getMax(), 1e-9);
    }

    @Test
    void percentileInterpolatesLinearly() {
        // [10,20,30,40]: p=50 -> Rang 1.5 -> 25
        double[] data = {10, 20, 30, 40};
        var stats = new DescriptiveStatistics(data);
        assertEquals(25.0, stats.percentile(50.0), 1e-9);
        assertEquals(10.0, stats.percentile(0.0), 1e-9);
        assertEquals(40.0, stats.percentile(100.0), 1e-9);
    }

    @Test
    void percentileRejectsOutOfRange() {
        var stats = new DescriptiveStatistics(new double[]{1, 2});
        assertThrows(IllegalArgumentException.class, () -> stats.percentile(101.0));
        assertThrows(IllegalArgumentException.class, () -> stats.percentile(-1.0));
    }

    @Test
    void emptyDataRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DescriptiveStatistics(new double[]{}));
    }

    @Test
    void singleValueHasZeroVariance() {
        var stats = new DescriptiveStatistics(new double[]{42.0});
        assertEquals(42.0, stats.getMean(), 1e-9);
        assertEquals(0.0, stats.getVariance(), 1e-9);
        assertEquals(42.0, stats.percentile(50.0), 1e-9);
    }

    @Test
    void largeSampleMeanConverges() {
        // Große gleichverteilte Stichprobe [0..999] -> Mittel ~ 499.5
        double[] data = new double[1000];
        for (int i = 0; i < 1000; i++) {
            data[i] = i;
        }
        var stats = new DescriptiveStatistics(data);
        assertTrue(Math.abs(stats.getMean() - 499.5) < 1e-6);
    }
}
