package io.github.muaiso.risiko.engine;

import java.util.Arrays;

/**
 * Haeufigkeitsverteilung (Histogramm) der simulierten NPV-Werte.
 *
 * <p>Teilt den Wertebereich in {@code bins} gleich breite Faecher ein und
 * zaehlt die Besetzung je Fach. Dient als kompakte Grundlage fuer die
 * Verteilungs-Darstellung (z. B. Tornado-/Dichte-Plots) ohne dass die
 * rohe Stichprobe uebertragen werden muss.</p>
 */
public final class Histogram {

    private final int bins;
    private final double min;
    private final double max;
    private final double binWidth;
    private final long[] counts;

    /**
     * Erzeugt das Histogramm aus den Werten.
     *
     * @param values die simulierten NPV-Werte (nicht leer)
     * @param bins   Anzahl der Faecher (>= 1)
     */
    public Histogram(double[] values, int bins) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("mindestens ein Wert erforderlich");
        }
        if (bins < 1) {
            throw new IllegalArgumentException("bins muss >= 1 sein");
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        this.min = stats.getMin();
        this.max = stats.getMax();
        this.bins = bins;
        this.binWidth = (max - min) / bins;
        this.counts = new long[bins];

        for (double v : values) {
            int idx = indexOf(v);
            // oberes Ende schliesst die letzte Kante mit ein
            counts[idx]++;
        }
    }

    private int indexOf(double value) {
        if (binWidth == 0.0) {
            return 0;
        }
        int idx = (int) ((value - min) / binWidth);
        if (idx < 0) {
            idx = 0;
        }
        if (idx >= bins) {
            idx = bins - 1;
        }
        return idx;
    }

    /** @return Anzahl der Faecher */
    public int getBins() {
        return bins;
    }

    /** @return untere Grenze des Wertebereichs */
    public double getMin() {
        return min;
    }

    /** @return obere Grenze des Wertebereichs */
    public double getMax() {
        return max;
    }

    /** @return Breite eines Fachs */
    public double getBinWidth() {
        return binWidth;
    }

    /**
     * @param bin Fach-Index (0-basiert)
     * @return Besetzungszahl des Fachs
     */
    public long getCount(int bin) {
        if (bin < 0 || bin >= bins) {
            throw new IndexOutOfBoundsException("bin ausserhalb des Bereichs");
        }
        return counts[bin];
    }

    /**
     * @param bin Fach-Index (0-basiert)
     * @return untere Kanten des Fachs (linker Rand)
     */
    public double getBinStart(int bin) {
        return min + bin * binWidth;
    }

    /**
     * @param bin Fach-Index (0-basiert)
     * @return obere Kanten des Fachs (rechter Rand)
     */
    public double getBinEnd(int bin) {
        return min + (bin + 1) * binWidth;
    }

    /**
     * @return Gesamtanzahl der gezaehlten Werte
     */
    public long getTotal() {
        return Arrays.stream(counts).sum();
    }
}
