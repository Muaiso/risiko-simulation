package io.github.muaiso.risiko.engine;

/**
 * Zentrale Momente hoeherer Ordnung einer NPV-Stichprobe.
 *
 * <p>Ergaenzen die deskriptive Statistik um Informationen zur Form der
 * Verteilung: {@link #skewness(double[])} misst Asymmetrie (Schiefe),
 * {@link #excessKurtosis(double[])} misst Schwanzlastigkeit (Exzess-Wölbung,
 * Normalverteilung = 0). Beide nutzen die Populations-Schaetzer und sind
 * konsistent mit {@link DescriptiveStatistics}.</p>
 */
public final class Moments {

    private Moments() {
    }

    /**
     * Schiefe (Skewness) der Verteilung.
     *
     * @param values die simulierten NPV-Werte
     * @return Schiefe; positiv = rechtsschief (langer rechter Schwanz)
     */
    public static double skewness(double[] values) {
        int n = values.length;
        if (n < 3) {
            return 0.0;
        }
        double mean = mean(values);
        double std = standardDeviation(values, mean);
        if (std == 0.0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            double z = (v - mean) / std;
            sum += z * z * z;
        }
        return sum / n;
    }

    /**
     * Exzess-Wölbung (Excess Kurtosis) der Verteilung.
     *
     * @param values die simulierten NPV-Werte
     * @return Exzess-Wölbung; 0 = normalverteilt, >0 = schwanzlastiger
     */
    public static double excessKurtosis(double[] values) {
        int n = values.length;
        if (n < 4) {
            return 0.0;
        }
        double mean = mean(values);
        double std = standardDeviation(values, mean);
        if (std == 0.0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            double z = (v - mean) / std;
            sum += z * z * z * z;
        }
        return sum / n - 3.0;
    }

    private static double mean(double[] values) {
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    private static double standardDeviation(double[] values, double mean) {
        double varSum = 0.0;
        for (double v : values) {
            double d = v - mean;
            varSum += d * d;
        }
        return Math.sqrt(varSum / values.length);
    }
}
