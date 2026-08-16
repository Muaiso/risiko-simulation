package io.github.muaiso.risiko.engine;

import java.util.Arrays;

/**
 * Risikokennzahlen auf Basis einer (per Simulation gewonnenen) Verteilung
 * der Projektwerte (NPV). Die Metriken geben Antwort auf die Frage:
 * "Wie viel koennen wir im schlechtesten Fall verlieren?" sowie auf
 * rendite-adjustierte Risikomaße (Sharpe/Sortino).
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

    /**
     * Sharpe Ratio der NPV-Verteilung.
     *
     * <p>Misst die ueber den risikofreien Zins hinaus erzielte Rendite pro
     * Einheit Gesamtrisiko (Standardabweichung). Eine hohe Sharpe Ratio
     * bedeutet guenstiges Risiko-Ertrags-Verhaeltnis.</p>
     *
     * @param values        die simulierten NPV-Werte
     * @param riskFreeRate  risikofreier Referenzwert (z. B. 0 oder Kapitalkosten)
     * @return Sharpe Ratio; 0 bei verschwindendem Risiko
     */
    public static double sharpeRatio(double[] values, double riskFreeRate) {
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        double std = stats.getStandardDeviation();
        if (std == 0.0) {
            return 0.0;
        }
        return (stats.getMean() - riskFreeRate) / std;
    }

    /**
     * Sortino Ratio der NPV-Verteilung.
     *
     * <p>Wie die Sharpe Ratio, gewichtet aber nur nach unten gerichtetes
     * Risiko (Downside Deviation). Belohnt asymmetrische Verteilungen mit
     * langem rechtem Schwanz staerker als die Sharpe Ratio.</p>
     *
     * @param values        die simulierten NPV-Werte
     * @param riskFreeRate  risikofreier Referenzwert
     * @return Sortino Ratio; 0 bei verschwindendem Downside-Risiko
     */
    public static double sortinoRatio(double[] values, double riskFreeRate) {
        double downside = downsideDeviation(values, riskFreeRate);
        if (downside == 0.0) {
            return 0.0;
        }
        double mean = new DescriptiveStatistics(values).getMean();
        return (mean - riskFreeRate) / downside;
    }

    /**
     * Downside Deviation: Standardabweichung nur der Realisierungen, die
     * schlechter als {@code target} ausfallen.
     *
     * @param values die simulierten NPV-Werte
     * @param target  Ziel-/Referenzwert (z. B. 0 oder Kapitalkosten)
     * @return Downside Deviation (>= 0)
     */
    public static double downsideDeviation(double[] values, double target) {
        if (values.length == 0) {
            return 0.0;
        }
        double sumSq = 0.0;
        for (double v : values) {
            double diff = Math.min(0.0, v - target);
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / values.length);
    }

    /**
     * Variationskoeffizient (Coefficient of Variation, CV).
     *
     * <p>Normiert die Streuung auf den Mittelwert und macht NPV-Verteilungen
     * unterschiedlicher Projektgroessen vergleichbar. CV = Stdabw / Mittel.</p>
     *
     * @param values die simulierten NPV-Werte
     * @return Variationskoeffizient; 0 bei Mittelwert 0
     */
    public static double coefficientOfVariation(double[] values) {
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        double mean = stats.getMean();
        if (mean == 0.0) {
            return 0.0;
        }
        return stats.getStandardDeviation() / Math.abs(mean);
    }
}
