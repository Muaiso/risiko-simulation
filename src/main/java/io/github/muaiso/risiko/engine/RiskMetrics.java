package io.github.muaiso.risiko.engine;

import java.util.Arrays;

/**
 * Risikokennzahlen auf Basis einer (per Simulation gewonnenen) Verteilung
 * der Projektwerte (NPV). Die Metriken geben Antwort auf die Frage:
 * "Wie viel koennen wir im schlechtesten Fall verlieren?"
 */
public final class RiskMetrics {

    private RiskMetrics() {
    }

    /**
     * Value at Risk (VaR) auf einem bestimmten Konfidenzniveau.
     *
     * <p>VaR(p) ist der (schlechteste) Verlust, der mit Wahrscheinlichkeit p
     * NICHT ueberschritten wird. Wir definieren ihn als Verlustgroesse:
     * ein positiver Rueckgabewert bedeutet "mit p-Prozent Wahrscheinlichkeit
     * ist der Verlust hoechstens dieser Betrag".</p>
     *
     * @param values        die simulierten NPV-Werte
     * @param confidence     Konfidenzniveau in Prozent (z. B. 95 fuer 95 %)
     * @return Value at Risk als positive Verlustgroesse
     */
    public static double valueAtRisk(double[] values, double confidence) {
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        // Perzentil des NPV bei (100 - confidence): z. B. 5 %-Quantil bei 95 % VaR
        double quantile = stats.percentile(100.0 - confidence);
        // Verlust = max(0, -quantile): nur echte Verluste zaehlen
        return Math.max(0.0, -quantile);
    }

    /**
     * Conditional Value at Risk (CVaR / Expected Shortfall).
     *
     * <p>Der Erwartungswert aller Verluste, die schlimmer als das VaR sind.
     * Antwortet auf "wenn es schiefgeht, wie schlimm wird es dann
     * im Mittel?". Robustere Risikokennzahl als VaR, da sie die
     * Schwanzform einbezieht.</p>
     *
     * @param values        die simulierten NPV-Werte
     * @param confidence     Konfidenzniveau in Prozent
     * @return Erwarteter Verlust im Schwanz (Tail-Expected-Loss)
     */
    public static double conditionalValueAtRisk(double[] values, double confidence) {
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        double threshold = stats.percentile(100.0 - confidence);
        double[] tail = Arrays.stream(values).filter(v -> v <= threshold).toArray();
        if (tail.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : tail) {
            sum += Math.max(0.0, -v);
        }
        return sum / tail.length;
    }

    /**
     * Wahrscheinlichkeit, dass der NPV negativ (Verlust) ausfaellt.
     *
     * @param values die simulierten NPV-Werte
     * @return Anteil negativer Realisierungen in [0, 1]
     */
    public static double probabilityOfLoss(double[] values) {
        long losses = Arrays.stream(values).filter(v -> v < 0.0).count();
        return (double) losses / values.length;
    }
}
