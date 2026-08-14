package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die Risikokennzahlen VaR, CVaR und Verlustwahrscheinlichkeit an
 * einer bekannten symmetrischen Verteilung.
 */
class RiskMetricsTest {

    @Test
    void valueAtRiskOfSymmetricLossDistribution() {
        // Haelfte der Werte -100 (Verlust), Haelfte +100 (Gewinn)
        double[] values = new double[1000];
        for (int i = 0; i < 1000; i++) {
            values[i] = (i % 2 == 0) ? -100.0 : 100.0;
        }
        // 95 % VaR: 5 %-Quantil = -100, Verlust = max(0, 100) = 100
        double var95 = RiskMetrics.valueAtRisk(values, 95.0);
        assertEquals(100.0, var95, 1e-9);
    }

    @Test
    void valueAtRiskZeroWhenAlwaysProfitable() {
        double[] values = {10, 20, 30, 40};
        // 5 %-Quantil = 10 (positiv), Verlust 0
        assertEquals(0.0, RiskMetrics.valueAtRisk(values, 95.0), 1e-9);
    }

    @Test
    void conditionalVaRIsTailMean() {
        // 100 Werte: 95x 0 (Gewinn), 5x -200 (Verlust)
        double[] values = new double[100];
        for (int i = 0; i < 95; i++) {
            values[i] = 0.0;
        }
        for (int i = 95; i < 100; i++) {
            values[i] = -200.0;
        }
        // Tail (5 % schlechteste) = die -200 Werte -> CVaR = 200
        double cvar = RiskMetrics.conditionalValueAtRisk(values, 95.0);
        assertEquals(200.0, cvar, 1e-9);
    }

    @Test
    void probabilityOfLossCountsNegative() {
        double[] values = {-5, -5, 10, 20};
        // 2 von 4 negativ -> 0.5
        assertEquals(0.5, RiskMetrics.probabilityOfLoss(values), 1e-9);
    }

    @Test
    void higherConfidenceGivesLargerVaR() {
        double[] values = new double[1000];
        for (int i = 0; i < 1000; i++) {
            values[i] = -i; // -0 .. -999
        }
        double var95 = RiskMetrics.valueAtRisk(values, 95.0);
        double var99 = RiskMetrics.valueAtRisk(values, 99.0);
        assertTrue(var99 >= var95, "99 % VaR sollte >= 95 % VaR sein");
    }
}
