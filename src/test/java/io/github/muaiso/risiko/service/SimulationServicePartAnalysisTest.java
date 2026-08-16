package io.github.muaiso.risiko.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.NormalDistribution;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet die deterministischen Teilanalysen (Payback, Szenarien) ueber den
 * Service ohne Monte-Carlo-Lauf.
 */
class SimulationServicePartAnalysisTest {

    private InvestmentProject project() {
        return new InvestmentProject("P", 0.0, java.util.List.of(
                StochasticCashFlow.fixed(0, -1000.0),
                StochasticCashFlow.stochastic(1, new NormalDistribution(600.0, 50.0)),
                StochasticCashFlow.stochastic(2, new NormalDistribution(600.0, 50.0))));
    }

    @Test
    void paybackRecoversForProfitableProject() {
        var result = new SimulationService().analyzePayback(project());
        assertTrue(result.recovered());
        assertEquals(2, result.paybackYear());
        assertTrue(result.finalNpv() > 0.0);
    }

    @Test
    void scenarioBestBaseWorstOrdered() {
        var result = new SimulationService().analyzeScenario(project());
        assertTrue(result.best() > result.base());
        assertTrue(result.base() > result.worst());
    }

    @Test
    void paybackRejectsEmptyProject() {
        // Das InvestmentProject-Record lehnt leere Cashflows bereits im
        // Konstruktor ab; der Service darf so nie ein leeres Projekt sehen.
        try {
            new InvestmentProject("Leer", 0.05, java.util.List.of());
            assertTrue(false, "haette IllegalArgumentException werfen sollen");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Cashflow"));
        }
    }
}
