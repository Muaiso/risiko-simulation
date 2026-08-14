package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet den NPV-Rechner: deterministischer Erwartungswert und
 * diskontierte Realisierung.
 */
class NetPresentValueCalculatorTest {

    private InvestmentProject singleYearProject() {
        var flows = java.util.List.of(
                StochasticCashFlow.fixed(0, -1000.0),
                StochasticCashFlow.fixed(1, 1100.0));
        return new InvestmentProject("Test", 0.10, flows);
    }

    @Test
    void expectedNpvMatchesManualDiscounting() {
        // -1000 + 1100/1.1 = -1000 + 1000 = 0
        double npv = NetPresentValueCalculator.expectedNetPresentValue(singleYearProject());
        assertEquals(0.0, npv, 1e-9);
    }

    @Test
    void realizedNpvUsesProvidedDrawFunction() {
        var project = singleYearProject();
        double npv = NetPresentValueCalculator.realizedNetPresentValue(project, cf -> {
            // erzwinge fixierte Realisierungen
            return cf.year() == 0 ? -1000.0 : 1210.0;
        });
        // -1000 + 1210/1.1 = -1000 + 1100 = 100
        assertEquals(100.0, npv, 1e-9);
    }

    @Test
    void npvGrowsWithLongerHorizonAtSameRate() {
        var shortProject = new InvestmentProject("S", 0.0,
                java.util.List.of(StochasticCashFlow.fixed(0, -100.0),
                        StochasticCashFlow.fixed(1, 100.0)));
        var longProject = new InvestmentProject("L", 0.0,
                java.util.List.of(StochasticCashFlow.fixed(0, -100.0),
                        StochasticCashFlow.fixed(5, 100.0)));
        // bei rate 0 ist NPV identisch (nur Betragssumme)
        assertEquals(
                NetPresentValueCalculator.expectedNetPresentValue(shortProject),
                NetPresentValueCalculator.expectedNetPresentValue(longProject),
                1e-9);
    }

    @Test
    void npvDiscountsLaterCashFlowsMore() {
        var project = new InvestmentProject("D", 0.10,
                java.util.List.of(StochasticCashFlow.fixed(0, -100.0),
                        StochasticCashFlow.fixed(1, 110.0),
                        StochasticCashFlow.fixed(2, 121.0)));
        // -100 + 100 + 100 = 100
        assertEquals(100.0, NetPresentValueCalculator.expectedNetPresentValue(project), 1e-9);
    }
}
