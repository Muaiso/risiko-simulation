package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.NormalDistribution;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet die Szenario-Analyse (Best/Base/Worst-Case) an einem Projekt mit
 * Normalverteilten Rueckflüssen.
 */
class ScenarioAnalyzerTest {

    private InvestmentProject normalProject() {
        return new InvestmentProject("S", 0.0, java.util.List.of(
                StochasticCashFlow.fixed(0, -1000.0),
                StochasticCashFlow.stochastic(1, new NormalDistribution(600.0, 50.0)),
                StochasticCashFlow.stochastic(2, new NormalDistribution(600.0, 50.0))));
    }

    @Test
    void bestExceedsBaseExceedsWorst() {
        var result = ScenarioAnalyzer.analyze(normalProject());
        assertTrue(result.best() > result.base(), "Best > Base");
        assertTrue(result.base() > result.worst(), "Base > Worst");
    }

    @Test
    void baseUsesExpectedValues() {
        // Investition -1000, erwartete Rueckfluesse 600 + 600 = +200
        var result = ScenarioAnalyzer.analyze(normalProject());
        assertEquals(200.0, result.base(), 1e-9);
    }

    @Test
    void boundsFromDistributionEdges() {
        // Normal mu=600, sigma=50: 6-Sigma-Grenzen -> min 300, max 900
        var result = ScenarioAnalyzer.analyze(normalProject());
        // worst = -1000 + 300 + 300 = -400
        assertEquals(-400.0, result.worst(), 1e-6);
        // best = -1000 + 900 + 900 = 800
        assertEquals(800.0, result.best(), 1e-6);
    }
}
