package io.github.muaiso.risiko.engine;

import io.github.muaiso.risiko.domain.Distribution;
import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Sensitivitaetsanalyse ("Tornado-Diagramm").
 *
 * <p>Fuer jeden unsicheren Input (stochastischen Cashflow) wird der NPV
 * einmal mit dem Minimum und einmal mit dem Maximum des Wertebereichs
 * berechnet (alle anderen Inputs beim Erwartungswert). Die Spannweite
 * {@code npvMax - npvMin} misst, wie stark dieser Input die Gesamt-Unsicherheit
 * des Projektwerts beeinflusst. Sortiert absteigend ergibt das die
 * klassische Tornado-Reihenfolge.</p>
 */
public final class SensitivityAnalyzer {

    private SensitivityAnalyzer() {
    }

    /**
     * Berechnet die Sensitivitaet jedes stochastischen Inputs.
     *
     * @param project das zu analysierende Projekt
     * @return Liste von {@link SensitivityResult}, sortiert nach staerkster
     *         Spannweite (Tornado: oben = wichtigster Treiber)
     */
    public static List<SensitivityResult> analyze(InvestmentProject project) {
        List<SensitivityResult> results = new ArrayList<>();
        List<StochasticCashFlow> flows = project.cashFlows();

        for (int i = 0; i < flows.size(); i++) {
            StochasticCashFlow cf = flows.get(i);
            if (cf.distribution() == null) {
                continue; // deterministische Inputs haben keine Unsicherheits-Spannweite
            }
            Distribution dist = cf.distribution();
            double npvMin = npvWithOverride(project, flows, i, dist.minimum());
            double npvMax = npvWithOverride(project, flows, i, dist.maximum());
            results.add(new SensitivityResult(i, cf.year(), npvMin, npvMax));
        }

        results.sort((a, b) -> Double.compare(b.range(), a.range()));
        return results;
    }

    private static double npvWithOverride(
            InvestmentProject project,
            List<StochasticCashFlow> flows,
            int index,
            double overrideAmount) {
        double sum = 0.0;
        for (int j = 0; j < flows.size(); j++) {
            StochasticCashFlow cf = flows.get(j);
            double amount;
            if (j == index) {
                amount = overrideAmount;
            } else if (cf.distribution() == null) {
                amount = cf.fixedAmount();
            } else {
                amount = cf.distribution().expectedValue();
            }
            sum += amount / Math.pow(1.0 + project.discountRate(), cf.year());
        }
        return sum;
    }

    /**
     * Ergebnis der Sensitivitaetsanalyse fuer einen einzelnen Input.
     *
     * @param cashFlowIndex Position im Cashflow-Array des Projekts
     * @param year          Zeitpunkt des zugehoerigen Zahlungsstroms
     * @param npvAtMinimum  NPV wenn dieser Input auf sein Minimum gesetzt wird
     * @param npvAtMaximum  NPV wenn dieser Input auf sein Maximum gesetzt wird
     */
    public record SensitivityResult(int cashFlowIndex, int year, double npvAtMinimum, double npvAtMaximum) {
        public double range() {
            return Math.abs(npvAtMaximum - npvAtMinimum);
        }
    }
}
