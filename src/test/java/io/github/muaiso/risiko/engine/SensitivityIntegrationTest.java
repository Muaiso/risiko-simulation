package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Integrationstest der Sensitivitaetsanalyse ueber das Project-Modell:
 * korrekte Sortierung und Spannweiten bei mehreren Treibern.
 */
class SensitivityIntegrationTest {

    @Test
    void ranksDominantDriverFirstAcrossProject() {
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -1_000_000.0),
                // Jahr 1: moderate Streuung
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(500_000.0, 20_000.0)),
                // Jahr 2: sehr starke Streuung (dominanter Treiber)
                StochasticCashFlow.stochastic(2, new io.github.muaiso.risiko.domain.NormalDistribution(500_000.0, 200_000.0)),
                // Jahr 3: kleine Streuung
                StochasticCashFlow.stochastic(3, new io.github.muaiso.risiko.domain.TriangularDistribution(400_000.0, 600_000.0, 500_000.0)));
        var project = new InvestmentProject("Integration", 0.08, flows);

        var results = SensitivityAnalyzer.analyze(project);
        assertEquals(3, results.size());
        // staerkster Treiber (Jahr 2, sigma 200k) muss an Position 0 stehen
        assertEquals(2, results.get(0).year());
        // Spannweiten streng absteigend
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).range() >= results.get(i).range());
        }
    }
}
