package io.github.muaiso.risiko.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;
import io.github.muaiso.risiko.exception.InvalidProjectException;

/**
 * Integrationstest der Service-Ebene: vollstaendige Analyse eines
 * Projekts mit plausiblen Ergebnissen.
 */
class SimulationServiceTest {

    private InvestmentProject sampleProject() {
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -1_000_000.0),
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(600_000.0, 50_000.0)),
                StochasticCashFlow.stochastic(2, new io.github.muaiso.risiko.domain.NormalDistribution(600_000.0, 50_000.0)),
                StochasticCashFlow.stochastic(3, new io.github.muaiso.risiko.domain.NormalDistribution(600_000.0, 50_000.0)));
        return new InvestmentProject("Maschinenauslastung", 0.08, flows);
    }

    @Test
    void analyzeProducesConsistentStatistics() {
        var service = new SimulationService();
        var result = service.analyze(sampleProject(), 20_000, 2024L);

        assertEquals("Maschinenauslastung", result.projectName());
        assertEquals(20_000, result.iterations());
        // Erwartungs-NPV > 0 (Einzahlungen ueberwiegen)
        assertTrue(result.expectedNpv() > 0.0);
        // Mittel der Simulation nahe Erwartungswert
        assertTrue(Math.abs(result.meanNpv() - result.expectedNpv()) < result.expectedNpv() * 0.05);
        // Standardabweichung positiv
        assertTrue(result.standardDeviation() > 0.0);
        // Perzentile geordnet
        assertTrue(result.percentile5() <= result.percentile50());
        assertTrue(result.percentile50() <= result.percentile95());
        // Verlustwahrscheinlichkeit in [0,1]
        assertTrue(result.probabilityOfLoss() >= 0.0 && result.probabilityOfLoss() <= 1.0);
    }

    @Test
    void analyzeVaRNonNegative() {
        var service = new SimulationService();
        var result = service.analyze(sampleProject(), 10_000, 99L);
        assertTrue(result.valueAtRisk95() >= 0.0);
        assertTrue(result.conditionalVaR95() >= result.valueAtRisk95());
    }

    @Test
    void analyzeRejectsZeroIterations() {
        var service = new SimulationService();
        assertThrows(InvalidProjectException.class,
                () -> service.analyze(sampleProject(), 0, 1L));
    }

    @Test
    void analyzeRejectsTooManyIterations() {
        var service = new SimulationService();
        assertThrows(InvalidProjectException.class,
                () -> service.analyze(sampleProject(), 10_000_000, 1L));
    }

    @Test
    void defaultIterationsConstantIsLarge() {
        assertTrue(SimulationService.DEFAULT_ITERATIONS >= 10_000);
    }
}
