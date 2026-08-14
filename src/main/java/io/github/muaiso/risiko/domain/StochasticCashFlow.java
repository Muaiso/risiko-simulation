package io.github.muaiso.risiko.domain;

/**
 * Verknuepft einen Zeitpunkt mit einer (ggf. stochastischen) Zahlungshoehe.
 *
 * <p>Ist {@code distribution} null, wird der feste {@code fixedAmount}
 * verwendet. Andernfalls wird pro Simulation ein Wert aus der Verteilung
 * gezogen.</p>
 *
 * @param year          Zeitpunkt in Jahren ab Projektstart
 * @param fixedAmount   deterministischer Betrag (wird genutzt wenn distribution == null)
 * @param distribution  stochastische Zahlungshoehe (optional)
 */
public record StochasticCashFlow(int year, double fixedAmount, Distribution distribution) {

    public StochasticCashFlow {
        if (year < 0) {
            throw new IllegalArgumentException("year muss >= 0 sein");
        }
        if (distribution == null && Double.isNaN(fixedAmount)) {
            throw new IllegalArgumentException("entweder fixedAmount oder distribution erforderlich");
        }
    }

    /**
     * Liefert den zum Zeitpunkt {@code year} gehoerigen Betrag fuer eine
     * konkrete Simulations-Realisierung.
     *
     * @param rng die Zufallsquelle fuer stochastische Betraege
     * @return realisierter Betrag des Zahlungsstroms
     */
    public double realizedAmount(RandomSource rng) {
        if (distribution == null) {
            return fixedAmount;
        }
        return distribution.sample(rng);
    }

    public static StochasticCashFlow fixed(int year, double amount) {
        return new StochasticCashFlow(year, amount, null);
    }

    public static StochasticCashFlow stochastic(int year, Distribution distribution) {
        return new StochasticCashFlow(year, Double.NaN, distribution);
    }
}
