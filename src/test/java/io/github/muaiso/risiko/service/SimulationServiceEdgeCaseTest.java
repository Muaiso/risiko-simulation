package io.github.muaiso.risiko.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Ergaenzende Service-Tests fuer Randfaelle: negative Rendite, sehr hohe
 * Iterationszahlen und Verlustprojekte.
 */
class SimulationServiceEdgeCaseTest {

    @Test
    void lossProjectHasHighLossProbability() {
        // Investition 1 Mio, Rueckfluss nur 100k -> klarer Verlust
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -1_000_000.0),
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(100_000.0, 5_000.0)));
        var project = new InvestmentProject("Verlust", 0.08, flows);
        var result = new SimulationService().analyze(project, 10_000, 7L);

        assertTrue(result.expectedNpv() < 0.0, "Erwartungs-NPV muss negativ sein");
        assertTrue(result.probabilityOfLoss() > 0.99, "Verlustwahrscheinlichkeit muss sehr hoch sein");
    }

    @Test
    void highIterationsStayStable() {
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -100.0),
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(120.0, 10.0)));
        var project = new InvestmentProject("Stabil", 0.0, flows);
        var result = new SimulationService().analyze(project, 200_000, 11L);

        assertEquals(200_000, result.iterations());
        // bei rate 0: Erwartungs-NPV = -100 + 120 = 20
        assertTrue(Math.abs(result.meanNpv() - 20.0) < 0.5,
                "Mittel bei hohen Iterationen sehr nah am Erwartungswert");
    }

    @Test
    void lowerDiscountRateIncreasesNpv() {
        // Ein Project, einmal mit 5%, einmal mit 15% Abzinsung
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -100.0),
                StochasticCashFlow.fixed(1, 110.0));
        var low = new SimulationService().analyze(
                new InvestmentProject("low", 0.05, flows), 1, 1L);
        var high = new SimulationService().analyze(
                new InvestmentProject("high", 0.15, flows), 1, 1L);
        assertTrue(low.expectedNpv() > high.expectedNpv(),
                "niedrigerer Abzinsungssatz muss NPV erhoehen");
    }
}
