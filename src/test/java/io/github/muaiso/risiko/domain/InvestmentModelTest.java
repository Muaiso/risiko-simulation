package io.github.muaiso.risiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die Domain-Modelle CashFlow, StochasticCashFlow und InvestmentProject:
 * Diskontierung, Validierung und Realisierung stochastischer Betraege.
 */
class InvestmentModelTest {

    @Test
    void cashFlowDiscountsByYear() {
        var cf = new CashFlow(2, 121.0);
        // bei 10 % Diskontierung: 121 / 1.1^2 = 100
        assertEquals(100.0, cf.presentValue(0.10), 1e-9);
    }

    @Test
    void cashFlowRejectsNegativeYear() {
        assertThrows(IllegalArgumentException.class, () -> new CashFlow(-1, 100));
    }

    @Test
    void stochasticCashFlowFixedReturnsConstant() {
        var cf = StochasticCashFlow.fixed(3, 500.0);
        var rng = new StandardNormalRandomSource(new java.util.SplittableRandom(1L));
        assertEquals(500.0, cf.realizedAmount(rng), 1e-9);
        assertTrue(cf.distribution() == null);
    }

    @Test
    void stochasticCashFlowSampledReturnsFromDistribution() {
        var dist = new UniformDistribution(0.0, 10.0);
        var cf = StochasticCashFlow.stochastic(1, dist);
        var rng = new StandardNormalRandomSource(new java.util.SplittableRandom(2L));
        double v = cf.realizedAmount(rng);
        assertTrue(v >= 0.0 && v <= 10.0);
    }

    @Test
    void projectRejectsNegativeDiscountRate() {
        var flows = java.util.List.of(StochasticCashFlow.fixed(0, -100.0));
        assertThrows(IllegalArgumentException.class,
                () -> new InvestmentProject("X", -0.05, flows));
    }

    @Test
    void projectRejectsEmptyCashFlows() {
        assertThrows(IllegalArgumentException.class,
                () -> new InvestmentProject("X", 0.1, java.util.List.of()));
    }

    @Test
    void projectRequiresEitherFixedOrDistribution() {
        // fixedAmount=NaN und keine distribution -> ungueltig
        assertThrows(IllegalArgumentException.class,
                () -> new StochasticCashFlow(1, Double.NaN, null));
    }
}
