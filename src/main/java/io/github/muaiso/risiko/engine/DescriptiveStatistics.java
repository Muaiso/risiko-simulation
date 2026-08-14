package io.github.muaiso.risiko.engine;

import java.util.Arrays;

/**
 * Deskriptive Statistik ueber eine Stichprobe von Simulationsergebnissen.
 *
 * <p>Alle Kennzahlen sind mit einem einzigen Durchlauf ueber die Daten
 * (oder ueber sortierte Daten fuer Perzentile) bestimmbar. Die Klasse ist
 * immutable: Einmal aus den Ergebnissen gebaut, aendert sie sich nicht.</p>
 */
public final class DescriptiveStatistics {

    private final double[] sorted;
    private final double mean;
    private final double variance;
    private final double standardDeviation;
    private final double min;
    private final double max;

    public DescriptiveStatistics(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("mindestens ein Wert erforderlich");
        }
        this.sorted = values.clone();
        Arrays.sort(this.sorted);
        this.min = this.sorted[0];
        this.max = this.sorted[this.sorted.length - 1];

        double sum = 0.0;
        for (double v : this.sorted) {
            sum += v;
        }
        this.mean = sum / this.sorted.length;

        double varSum = 0.0;
        for (double v : this.sorted) {
            double d = v - this.mean;
            varSum += d * d;
        }
        // population variance (ganze Verteilung liegt vor, keine Stichprobe)
        this.variance = varSum / this.sorted.length;
        this.standardDeviation = Math.sqrt(this.variance);
    }

    public double getMean() {
        return mean;
    }

    public double getVariance() {
        return variance;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getCount() {
        return sorted.length;
    }

    /**
     * Empirical Perzentil ueber lineare Interpolation (wie numpy.percentile
     * mit Methode 'linear').
     *
     * @param p Perzentil in Prozent, muss in [0, 100] liegen
     * @return der interpoliert geschätzte Perzentilwert
     */
    public double percentile(double p) {
        if (p < 0.0 || p > 100.0) {
            throw new IllegalArgumentException("Perzentil p muss in [0, 100] liegen");
        }
        if (p == 0.0) {
            return min;
        }
        if (p == 100.0) {
            return max;
        }
        double rank = (p / 100.0) * (sorted.length - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        double frac = rank - lo;
        return sorted[lo] + frac * (sorted[hi] - sorted[lo]);
    }
}
