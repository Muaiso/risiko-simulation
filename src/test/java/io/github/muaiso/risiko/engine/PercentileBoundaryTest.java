package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Ergaenzende Perzentil-Tests fuer Randbereiche und bekannte Verteilungen.
 */
class PercentileBoundaryTest {

    @Test
    void percentileOfUniformSpreadIsLinear() {
        double[] data = new double[1001];
        for (int i = 0; i <= 1000; i++) {
            data[i] = i; // 0 .. 1000
        }
        var stats = new DescriptiveStatistics(data);
        // P10 = 100, P50 = 500, P90 = 900
        assertEquals(100.0, stats.percentile(10.0), 1e-9);
        assertEquals(500.0, stats.percentile(50.0), 1e-9);
        assertEquals(900.0, stats.percentile(90.0), 1e-9);
    }

    @Test
    void percentile25And75OfSymmetricSet() {
        double[] data = {1, 2, 3, 4};
        var stats = new DescriptiveStatistics(data);
        // Rang fuer P25: 0.25*3 = 0.75 -> 0.75 zwischen Index0(1) und 1(2) -> 1.75
        assertEquals(1.75, stats.percentile(25.0), 1e-9);
        // P75: 0.75*3 = 2.25 -> zwischen Index2(3) und 3(4) -> 3.25
        assertEquals(3.25, stats.percentile(75.0), 1e-9);
    }

    @Test
    void percentileIsMonotonic() {
        double[] data = new double[500];
        for (int i = 0; i < 500; i++) {
            data[i] = Math.sin(i);
        }
        var stats = new DescriptiveStatistics(data);
        double p10 = stats.percentile(10.0);
        double p50 = stats.percentile(50.0);
        double p90 = stats.percentile(90.0);
        assertTrue(p10 <= p50 && p50 <= p90, "Perzentile muessen monoton steigen");
    }
}
