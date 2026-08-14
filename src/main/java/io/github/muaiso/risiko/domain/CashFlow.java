package io.github.muaiso.risiko.domain;

/**
 * Ein einzelner diskontierter Zahlungsstrom zu einem Zeitpunkt {@code year}.
 *
 * <p>{@code amount} ist der Nominalbetrag zum Zeitpunkt {@code year}
 * (Jahr 0 = sofort, Investition; spaetere Jahre = Rueckfluesse). Das Vorzeichen
 * ist semantisch: negative Betraege sind Auszahlungen, positive Einnahmen.</p>
 *
 * @param year   Zeitpunkt in Jahren ab Projektstart (0 = t0)
 * @param amount Nominalbetrag des Zahlungsstroms
 */
public record CashFlow(int year, double amount) {

    public CashFlow {
        if (year < 0) {
            throw new IllegalArgumentException("year muss >= 0 sein");
        }
    }

    /**
     * Diskontiert diesen Zahlungsstrom auf den Barwert (t0).
     *
     * @param discountRate jaehrlicher Abzinsungssatz (z. B. 0.08 fuer 8 %)
     * @return Barwert des Zahlungsstroms
     */
    public double presentValue(double discountRate) {
        return amount / Math.pow(1.0 + discountRate, year);
    }
}
