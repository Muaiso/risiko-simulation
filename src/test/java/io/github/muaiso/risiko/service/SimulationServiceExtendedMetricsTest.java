package io.github.muaiso.risiko.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.NormalDistribution;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet, dass die erweiterten Kennzahlen (Momente, Sharpe/Sortino, CV)
 * im Service-Ergebnis plausibel befuellt werden.
 */
class SimulationServiceExtendedMetricsTest {

    private InvestmentProject profitableProject() {
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -1_000_000.0),
                StochasticCashFlow.stochastic(1, new NormalDistribution(600_000.0, 50_000.0)),
                StochasticCashFlow.stochastic(2, new NormalDistribution(600_000.0, 50_000.0)));
        return new InvestmentProject("Demo", 0.08, flows);
    }

    @Test
    void extendedMetricsArePopulated() {
        var service = new SimulationService();
        var result = service.analyze(profitableProject(), 20_000, 7L);

        // Sharpe/Sortino enden bei profitablen, risikobehafteten Projekten > 0
        assertTrue(result.sharpeRatio() > 0.0);
        assertTrue(result.sortinoRatio() > 0.0);
        // Variationskoeffizient positiv und endlich
        assertTrue(result.coefficientOfVariation() > 0.0);
        // Momente sind endliche Zahlen
        assertTrue(Double.isFinite(result.skewness()));
        assertTrue(Double.isFinite(result.excessKurtosis()));
    }

    @Test
    void symmetricProjectHasNearZeroSkewness() {
        var service = new SimulationService();
        var result = service.analyze(profitableProject(), 50_000, 123L);
        // Normalverteilte Cashflows -> Schiefe nahe 0
        assertTrue(Math.abs(result.skewness()) < 0.2);
    }
}
