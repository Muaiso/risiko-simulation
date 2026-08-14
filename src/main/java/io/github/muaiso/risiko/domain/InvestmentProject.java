package io.github.muaiso.risiko.domain;

import java.util.List;

/**
 * Ein Investitionsprojekt: eine Folge von (unsicheren) Zahlungsstroemen.
 *
 * <p>Jeder Cashflow kann entweder deterministisch (fixer Betrag) oder
 * stochastisch (eine {@link Distribution}) sein. Die erste Variante wird
 * typischerweise fuer die Anfangsinvestition (t0), die zweite fuer
 * unsichere Rueckfluesse verwendet.</p>
 *
 * @param name       Bezeichnung des Projekts
 * @param discountRate risikoloser/gewichteter Abzinsungssatz (Kapitalkosten)
 * @param cashFlows  die Zahlungsstroeme, indiziert ueber {@code year}
 */
public record InvestmentProject(String name, double discountRate, List<StochasticCashFlow> cashFlows) {

    public InvestmentProject {
        if (discountRate < 0) {
            throw new IllegalArgumentException("discountRate muss >= 0 sein");
        }
        if (cashFlows == null || cashFlows.isEmpty()) {
            throw new IllegalArgumentException("mindestens ein Cashflow erforderlich");
        }
    }
}
