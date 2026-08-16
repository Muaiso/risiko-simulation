package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die rendite-adjustierten Risikokennzahlen (Sharpe, Sortino,
 * Downside Deviation, Variationskoeffizient) an bekannten Verteilungen.
 */
class RiskMetricsRatioTest {

    @Test
    void sharpeRatioUsesMeanOverStd() {
        // symmetrisch um 100, Populations-Std = sqrt(5000/3) ~= 40.8248
        double[] values = {50, 100, 150};
        double expected = 100.0 / Math.sqrt(5000.0 / 3.0);
        assertEquals(expected, RiskMetrics.sharpeRatio(values, 0.0), 1e-9);
    }

    @Test
    void sharpeRatioZeroWhenNoRisk() {
        double[] values = {100, 100, 100};
        assertEquals(0.0, RiskMetrics.sharpeRatio(values, 0.0), 1e-9);
    }

    @Test
    void sortinoIgnoresUpsideVolatility() {
        // asymmetrisch: ein sehr hoher Wert (Upside) darf die Downside
        // Deviation nicht erhoehen; nur Realisierungen < target (0) zaehlen
        double[] values = {-10, 90, 100, 2000};
        double downside = RiskMetrics.downsideDeviation(values, 0.0);
        // nur -10 unter 0: sqrt(100/4) = 5
        assertEquals(5.0, downside, 1e-9);
        assertTrue(RiskMetrics.sortinoRatio(values, 0.0) > 0.0);
    }

    @Test
    void downsideDeviationZeroWhenAllAboveTarget() {
        double[] values = {10, 20, 30};
        assertEquals(0.0, RiskMetrics.downsideDeviation(values, 0.0), 1e-9);
        assertEquals(0.0, RiskMetrics.sortinoRatio(values, 0.0), 1e-9);
    }

    @Test
    void coefficientOfVariationIsNormalized() {
        // Mittel 100, Populations-Std = sqrt(800/3) ~= 16.3299 -> CV ~0.1633
        double[] values = {80, 100, 120};
        double expected = Math.sqrt(800.0 / 3.0) / 100.0;
        assertEquals(expected, RiskMetrics.coefficientOfVariation(values), 1e-9);
    }

    @Test
    void coefficientOfVariationZeroWhenMeanZero() {
        double[] values = {-1, 0, 1};
        assertEquals(0.0, RiskMetrics.coefficientOfVariation(values), 1e-9);
    }
}
