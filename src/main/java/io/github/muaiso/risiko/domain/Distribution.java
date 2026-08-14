package io.github.muaiso.risiko.domain;

/**
 * Eine Wahrscheinlichkeitsverteilung, aus der Stichproben fuer die
 * Monte-Carlo-Simulation gezogen werden.
 *
 * <p>Jede Verteilung liefert ueber {@link #sample(RandomSource)} einen
 * konkreten Realisierungswert. Zusaetzlich werden Lagemaße und
 * Spannweiten fuer Validierung und Dokumentation bereitgestellt.</p>
 */
public interface Distribution {

    /**
     * Zieht einen Stichprobenwert aus der Verteilung.
     *
     * @param rng die zu verwendende Zufallsquelle
     * @return ein gezogener Wert (z. B. ein erwarteter Cashflow)
     */
    double sample(RandomSource rng);

    /**
     * Erwartungswert der Verteilung (theoretisch).
     *
     * @return der Erwartungswert
     */
    double expectedValue();

    /**
     * Untere Schranke des sinnvollen Wertebereichs.
     *
     * @return minimal moeglicher Wert
     */
    double minimum();

    /**
     * Obere Schranke des sinnvollen Wertebereichs.
     *
     * @return maximal moeglicher Wert
     */
    double maximum();
}
