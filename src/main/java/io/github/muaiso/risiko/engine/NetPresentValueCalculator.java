package io.github.muaiso.risiko.engine;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

/**
 * Berechnet den Kapitalwert (Net Present Value, NPV) eines Projekts.
 *
 * <p>Der deterministische NPV nutzt die Erwartungswerte der Verteilungen
 * ("Mean-Case"). Die stochastische Verteilung des NPV ermittelt der
 * Monte-Carlo-Simulator, der pro Lauf konkrete Realisierungen zieht.</p>
 */
public final class NetPresentValueCalculator {

    private NetPresentValueCalculator() {
    }

    /**
     * Kapitalwert auf Basis der Erwartungswerte aller Zahlungsstroeme.
     *
     * @param project das zu bewertende Projekt
     * @return Erwartungs-NPV (Barwert der erwarteten Rueckfluesse abzueglich
     *         der erwarteten Investitionen)
     */
    public static double expectedNetPresentValue(InvestmentProject project) {
        double sum = 0.0;
        for (StochasticCashFlow cf : project.cashFlows()) {
            double amount = cf.distribution() == null
                    ? cf.fixedAmount()
                    : cf.distribution().expectedValue();
            sum += amount / Math.pow(1.0 + project.discountRate(), cf.year());
        }
        return sum;
    }

    /**
     * Kapitalwert fuer eine einzelne, konkret realisierte Simulationsiteration.
     *
     * @param project das Projekt
     * @param drawFn  liefert den realisierten Betrag je Cashflow
     * @return NPV dieser Realisierung
     */
    public static double realizedNetPresentValue(
            InvestmentProject project,
            java.util.function.ToDoubleFunction<StochasticCashFlow> drawFn) {
        double sum = 0.0;
        for (StochasticCashFlow cf : project.cashFlows()) {
            double amount = drawFn.applyAsDouble(cf);
            sum += amount / Math.pow(1.0 + project.discountRate(), cf.year());
        }
        return sum;
    }
}
