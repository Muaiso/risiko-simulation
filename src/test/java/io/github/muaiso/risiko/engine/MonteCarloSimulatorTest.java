package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet den Monte-Carlo-Simulator auf Korrektheit, Reproduzierbarkeit
 * und sinnvolles Fehlerverhalten.
 */
class MonteCarloSimulatorTest {

    private InvestmentProject projectWithNormalReturn() {
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -100.0),
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(120.0, 10.0)));
        return new InvestmentProject("P", 0.0, flows);
    }

    @Test
    void simulateReturnsExactlyIterations() {
        var sim = new MonteCarloSimulator(projectWithNormalReturn(), 123L);
        double[] result = sim.simulate(1_000);
        assertEquals(1_000, result.length);
    }

    @Test
    void simulateIsReproducibleWithSameSeed() {
        var a = new MonteCarloSimulator(projectWithNormalReturn(), 777L).simulate(500);
        var b = new MonteCarloSimulator(projectWithNormalReturn(), 777L).simulate(500);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i], 1e-12, "gleicher Seed -> gleiche Sequenz");
        }
    }

    @Test
    void simulateDiffersWithDifferentSeed() {
        var a = new MonteCarloSimulator(projectWithNormalReturn(), 1L).simulate(500);
        var b = new MonteCarloSimulator(projectWithNormalReturn(), 2L).simulate(500);
        boolean anyDifferent = false;
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) > 1e-9) {
                anyDifferent = true;
                break;
            }
        }
        assertTrue(anyDifferent, "unterschiedliche Seeds sollten verschiedene Laeufe ergeben");
    }

    @Test
    void simulateRejectsNonPositiveIterations() {
        var sim = new MonteCarloSimulator(projectWithNormalReturn(), 1L);
        assertThrows(IllegalArgumentException.class, () -> sim.simulate(0));
        assertThrows(IllegalArgumentException.class, () -> sim.simulate(-5));
    }

    @Test
    void simulatedMeanNearExpectedNpv() {
        var project = projectWithNormalReturn();
        // Erwartungs-NPV = -100 + 120 = 20 (rate 0)
        double[] result = new MonteCarloSimulator(project, 42L).simulate(20_000);
        var stats = new DescriptiveStatistics(result);
        assertTrue(Math.abs(stats.getMean() - 20.0) < 1.0,
                "Mittel der Simulation sollte nahe dem Erwartungswert 20 liegen");
    }
}
