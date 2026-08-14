package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.CashFlow;

/**
 * Ergaenzende Tests fuer die Diskontierung von CashFlows bei
 * unterschiedlichen Abzinsungssaetzen.
 */
class CashFlowDiscountingTest {

    @Test
    void discountsLongerHorizonMoreStrongly() {
        // 121 EUR in Jahr 2 bei 10 % -> 100 EUR (wie Jahr 1 mit 110)
        var cf2 = new CashFlow(2, 121.0);
        var cf1 = new CashFlow(1, 110.0);
        assertEquals(cf1.presentValue(0.10), cf2.presentValue(0.10), 1e-9);
    }

    @Test
    void zeroRateKeepsNominal() {
        var cf = new CashFlow(5, 500.0);
        assertEquals(500.0, cf.presentValue(0.0), 1e-9);
    }

    @Test
    void negativeRateIncreasesPresentValue() {
        var cf = new CashFlow(3, 1000.0);
        double pvNeg = cf.presentValue(-0.05);
        double pvPos = cf.presentValue(0.05);
        // bei negativem Satz wird weniger (bzw. rueckgaengig) abgezinst
        assertEquals(1000.0 / Math.pow(0.95, 3), pvNeg, 1e-9);
        assertEquals(1000.0 / Math.pow(1.05, 3), pvPos, 1e-9);
        org.junit.jupiter.api.Assertions.assertTrue(pvNeg > pvPos);
    }
}
