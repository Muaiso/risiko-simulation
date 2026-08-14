package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Testet, dass die simulierte Varianz der NPV-Verteilung mit der
 * theoretisch erwarteten Streuung der zugrunde liegenden Verteilung
 * konvergiert.
 */
class MonteCarloVarianceTest {

    @Test
    void simulatedStandardDeviationNearTheoretical() {
        // Ein Cashflow Jahr 1 ~ Normal(mu=0, sigma=100), rate 0
        // NPV = -0 + X  =>  NPV-Streuung == sigma == 100
        var project = new InvestmentProject("Var", 0.0, java.util.List.of(
                StochasticCashFlow.fixed(0, 0.0),
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(0.0, 100.0))));

        double[] result = new MonteCarloSimulator(project, 100_000L).simulate(100_000);
        var stats = new DescriptiveStatistics(result);
        // empirische SD sollte nahe 100 liegen (Toleranz 5)
        assertTrue(Math.abs(stats.getStandardDeviation() - 100.0) < 5.0,
                "empirische SD " + stats.getStandardDeviation() + " sollte nahe 100 liegen");
    }

    @Test
    void largerIterationsReduceSamplingError() {
        var project = new InvestmentProject("C", 0.0, java.util.List.of(
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(50.0, 10.0))));

        double small = new DescriptiveStatistics(
                new MonteCarloSimulator(project, 1L).simulate(1_000)).getStandardDeviation();
        double large = new DescriptiveStatistics(
                new MonteCarloSimulator(project, 1L).simulate(100_000)).getStandardDeviation();

        // beide nahe 10; große Stichprobe stabiler (hier nur Plausibilitaet)
        assertTrue(Math.abs(small - 10.0) < 1.5);
        assertTrue(Math.abs(large - 10.0) < 0.5);
    }
}
