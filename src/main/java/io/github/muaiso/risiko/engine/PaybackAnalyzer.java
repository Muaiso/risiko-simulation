package io.github.muaiso.risiko.engine;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Diskontierter Amortisationspunkt (Discounted Payback Period).
 *
 * <p>Bestimmt das Jahr, ab dem die kumulierten, auf den Projektstart
 * abgezinsten Zahlungsstroeme den Kapitalwert (NPV) nicht mehr negativ
 * sind. Bei stochastischen Cashflows wird mit den Erwartungswerten
 * gerechnet (deterministischer Base-Case). Liefert neben dem Jahr auch
 * den exakten Zeitpunkt (linear zwischen zwei Jahren interpoliert) sowie
 * das Gesamt-NPV nach Ende des Horizonts.</p>
 */
public final class PaybackAnalyzer {

    private PaybackAnalyzer() {
    }

    /**
     * Berechnet die diskontierte Amortisation fuer ein Projekt.
     *
     * @param project das zu analysierende Projekt
     * @return Ergebnis mit Jahr, exaktem Zeitpunkt und End-NPV
     */
    public static PaybackResult analyze(InvestmentProject project) {
        List<Double> discounted = new ArrayList<>();
        double rate = project.discountRate();
        double cumulative = 0.0;
        int paybackYear = -1;
        double paybackExact = Double.POSITIVE_INFINITY;

        double prevCumulative = 0.0;
        double prevDiscounted = 0.0;
        int prevYear = 0;

        for (StochasticCashFlow cf : project.cashFlows()) {
            double amount = cf.distribution() == null
                    ? cf.fixedAmount()
                    : cf.distribution().expectedValue();
            double dcf = amount / Math.pow(1.0 + rate, cf.year());
            discounted.add(dcf);
            cumulative += dcf;

            if (paybackYear == -1 && cumulative >= 0.0) {
                paybackYear = cf.year();
                // lineare Interpolation zwischen Vorjahr (negativ) und Jetzt (positiv)
                if (prevCumulative < 0.0) {
                    double frac = -prevCumulative / (dcf - prevDiscounted <= 0.0 ? 1.0 : (dcf - prevDiscounted));
                    paybackExact = prevYear + frac;
                } else {
                    paybackExact = (double) cf.year();
                }
            }
            prevCumulative = cumulative;
            prevDiscounted = dcf;
            prevYear = cf.year();
        }

        boolean recovered = cumulative >= 0.0;
        return new PaybackResult(
                recovered ? paybackYear : -1,
                recovered ? paybackExact : Double.POSITIVE_INFINITY,
                cumulative,
                recovered);
    }

    /**
     * Ergebnis der diskontierten Amortisationsanalyse.
     *
     * @param paybackYear       ganzzahliges Jahr des Umschlagens (-1 = nie)
     * @param paybackExact      exakter Zeitpunkt (Jahre, interpoliert)
     * @param finalNpv          kumulierter diskontierter NPV ueber Horizont
     * @param recovered         true wenn innerhalb des Horizonts amortisiert
     */
    public record PaybackResult(
            int paybackYear,
            double paybackExact,
            double finalNpv,
            boolean recovered) {
    }
}
