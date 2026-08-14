package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet die Sensitivitaetsanalyse (Tornado): Identifikation des
 * staerksten Unsicherheits-Treibers.
 */
class SensitivityAnalyzerTest {

    @Test
    void mostVolatileInputRanksFirst() {
        // Jahr 1: sehr breite Dreiecksverteilung (starker Treiber)
        // Jahr 2: enge Normalverteilung (schwacher Treiber)
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -1000.0),
                StochasticCashFlow.stochastic(1,
                        new io.github.muaiso.risiko.domain.TriangularDistribution(-500.0, 3000.0, 500.0)),
                StochasticCashFlow.stochastic(2,
                        new io.github.muaiso.risiko.domain.NormalDistribution(800.0, 5.0)));
        var project = new InvestmentProject("Sens", 0.10, flows);

        var results = SensitivityAnalyzer.analyze(project);
        assertFalse(results.isEmpty());
        // Erster Eintrag muss der breitere (Jahr 1) sein
        assertEquals(1, results.get(0).year());
        assertTrue(results.get(0).range() > results.get(1).range());
    }

    @Test
    void deterministicFlowsProduceNoSensitivity() {
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -100.0),
                StochasticCashFlow.fixed(1, 150.0));
        var project = new InvestmentProject("Det", 0.10, flows);
        var results = SensitivityAnalyzer.analyze(project);
        assertTrue(results.isEmpty(), "ohne stochastische Inputs keine Sensitivitaet");
    }

    @Test
    void rangeIsAbsoluteDifference() {
        var flows = java.util.List.of(
                StochasticCashFlow.stochastic(0,
                        new io.github.muaiso.risiko.domain.TriangularDistribution(0.0, 100.0, 50.0)));
        var project = new InvestmentProject("R", 0.0, flows);
        var results = SensitivityAnalyzer.analyze(project);
        // NPV bei min=0 -> 0; bei max=100 -> 100; range = 100
        assertEquals(100.0, results.get(0).range(), 1e-9);
    }
}
