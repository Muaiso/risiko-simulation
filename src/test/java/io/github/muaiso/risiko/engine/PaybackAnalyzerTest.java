package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.NormalDistribution;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet die diskontierte Amortisationsanalyse an bekannten Cashflow-Mustern.
 */
class PaybackAnalyzerTest {

    private InvestmentProject projectOf(double... amounts) {
        var flows = new java.util.ArrayList<StochasticCashFlow>();
        for (int t = 0; t < amounts.length; t++) {
            flows.add(StochasticCashFlow.fixed(t, amounts[t]));
        }
        return new InvestmentProject("P", 0.0, flows);
    }

    @Test
    void recoversWithinHorizon() {
        // Investition -1000, danach +600, +600 -> kumuliert ab Jahr 2 positiv
        var result = PaybackAnalyzer.analyze(projectOf(-1000, 600, 600));
        assertTrue(result.recovered());
        assertEquals(2, result.paybackYear());
        // exakt zwischen Jahr 1 (kum -400) und Jahr 2 (kum +200): 1 + 400/600
        assertEquals(1.0 + 400.0 / 600.0, result.paybackExact(), 1e-9);
    }

    @Test
    void neverRecoversWhenLossMaking() {
        var result = PaybackAnalyzer.analyze(projectOf(-1000, 100, 100));
        assertFalse(result.recovered());
        assertEquals(-1, result.paybackYear());
        assertTrue(Double.isInfinite(result.paybackExact()));
        assertTrue(result.finalNpv() < 0.0);
    }

    @Test
    void discountingDelaysPayback() {
        // ohne Abzinsung (0%) amortisiert sofort; mit Abzinsung spaeter
        var undiscounted = PaybackAnalyzer.analyze(projectOf(-1000, 600, 600));
        var discounted = PaybackAnalyzer.analyze(
                new InvestmentProject("P", 0.10, java.util.List.of(
                        StochasticCashFlow.fixed(0, -1000.0),
                        StochasticCashFlow.fixed(1, 600.0),
                        StochasticCashFlow.fixed(2, 600.0))));
        assertTrue(discounted.paybackExact() >= undiscounted.paybackExact());
    }

    @Test
    void usesExpectedValueForStochastic() {
        var project = new InvestmentProject("S", 0.0, java.util.List.of(
                StochasticCashFlow.fixed(0, -1000.0),
                StochasticCashFlow.stochastic(1, new NormalDistribution(700.0, 50.0)),
                StochasticCashFlow.stochastic(2, new NormalDistribution(700.0, 50.0))));
        var result = PaybackAnalyzer.analyze(project);
        assertTrue(result.recovered());
        assertEquals(2, result.paybackYear());
    }
}
