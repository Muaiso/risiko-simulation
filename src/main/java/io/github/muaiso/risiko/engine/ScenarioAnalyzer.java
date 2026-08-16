package io.github.muaiso.risiko.engine;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Szenario-Analyse (Best/Base/Worst-Case) auf Basis der Verteilungsraender.
 *
 * <p>Ergaenzt die stochastische Monte-Carlo-Sicht um drei deterministische
 * Eckpunkte: im Best-Case werden alle stochastischen Cashflows auf ihr
 * Maximum, im Worst-Case auf ihr Minimum, im Base-Case auf ihren
 * Erwartungswert gesetzt. Liefert schnelle, nachvollziehbare Spannen ohne
 * Zufall.</p>
 */
public final class ScenarioAnalyzer {

    private ScenarioAnalyzer() {
    }

    /**
     * Berechnet die drei Szenario-NPVs eines Projekts.
     *
     * @param project das zu analysierende Projekt
     * @return Szenario-Ergebnis (Best/Base/Worst)
     */
    public static ScenarioResult analyze(InvestmentProject project) {
        double best = npvWithMode(project, Mode.MAX);
        double base = npvWithMode(project, Mode.EXPECTED);
        double worst = npvWithMode(project, Mode.MIN);
        return new ScenarioResult(best, base, worst);
    }

    private static double npvWithMode(InvestmentProject project, Mode mode) {
        double sum = 0.0;
        double rate = project.discountRate();
        for (StochasticCashFlow cf : project.cashFlows()) {
            double amount;
            if (cf.distribution() == null) {
                amount = cf.fixedAmount();
            } else {
                amount = switch (mode) {
                    case MIN -> cf.distribution().minimum();
                    case MAX -> cf.distribution().maximum();
                    case EXPECTED -> cf.distribution().expectedValue();
                };
            }
            sum += amount / Math.pow(1.0 + rate, cf.year());
        }
        return sum;
    }

    private enum Mode { MIN, MAX, EXPECTED }

    /**
     * Ergebnis der Szenario-Analyse.
     *
     * @param best  NPV im guenstigsten Fall (alle Inputs max)
     * @param base  NPV im Base-Case (alle Inputs Erwartungswert)
     * @param worst NPV im schlechtesten Fall (alle Inputs min)
     */
    public record ScenarioResult(double best, double base, double worst) {
    }
}
