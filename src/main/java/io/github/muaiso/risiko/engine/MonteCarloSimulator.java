package io.github.muaiso.risiko.engine;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.RandomSource;
import io.github.muaiso.risiko.domain.StandardNormalRandomSource;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Monte-Carlo-Simulator fuer die NPV-Verteilung eines Projekts.
 *
 * <p>Fuer jede Iteration werden alle stochastischen Cashflows einmal
 * realisiert und der resultierende NPV berechnet. Die Menge der NPV-Werte
 * bildet die unsichere Verteilung des Projektwerts ab.</p>
 */
public final class MonteCarloSimulator {

    private final InvestmentProject project;
    private final RandomSource randomSource;

    public MonteCarloSimulator(InvestmentProject project, RandomSource randomSource) {
        this.project = project;
        this.randomSource = randomSource;
    }

    /**
     * Convenience-Konstruktor mit festem Seed fuer reproduzierbare Laeufe.
     *
     * @param project das Projekt
     * @param seed    Startwert des Pseudozufallsgenerators
     */
    public MonteCarloSimulator(InvestmentProject project, long seed) {
        this(project, new StandardNormalRandomSource(new java.util.SplittableRandom(seed)));
    }

    /**
     * Fuehrt {@code iterations} Simulationen durch.
     *
     * @param iterations Anzahl der Laeufe (typisch 10.000 - 100.000)
     * @return Array der realisierten NPV-Werte (Laenge == iterations)
     */
    public double[] simulate(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations muss > 0 sein");
        }
        double[] results = new double[iterations];
        for (int i = 0; i < iterations; i++) {
            double npv = 0.0;
            for (StochasticCashFlow cf : project.cashFlows()) {
                double amount = cf.realizedAmount(randomSource);
                npv += amount / Math.pow(1.0 + project.discountRate(), cf.year());
            }
            results[i] = npv;
        }
        return results;
    }
}
