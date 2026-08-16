package io.github.muaiso.risiko.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testet die Histogramm-Bildung: Faecher, Kanten und Gesamtzaehlung.
 */
class HistogramTest {

    @Test
    void countsSumToTotal() {
        double[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        var hist = new Histogram(values, 5);
        assertEquals(10, hist.getTotal());
        // alle Werte im [min,max]-Bereich
        assertEquals(1.0, hist.getMin(), 1e-9);
        assertEquals(10.0, hist.getMax(), 1e-9);
    }

    @Test
    void singleValueAllInOneBin() {
        double[] values = {5, 5, 5};
        var hist = new Histogram(values, 4);
        assertEquals(3, hist.getTotal());
        long total = 0;
        for (int i = 0; i < hist.getBins(); i++) {
            total += hist.getCount(i);
        }
        assertEquals(3, total);
    }

    @Test
    void binEdgesAreMonotonic() {
        double[] values = new double[200];
        for (int i = 0; i < 200; i++) {
            values[i] = i;
        }
        var hist = new Histogram(values, 10);
        for (int i = 1; i < hist.getBins(); i++) {
            assertTrue(hist.getBinStart(i) > hist.getBinStart(i - 1));
            assertTrue(hist.getBinEnd(i) > hist.getBinStart(i));
        }
    }

    @Test
    void rejectsEmptyValues() {
        assertThrows(IllegalArgumentException.class, () -> new Histogram(new double[]{}, 5));
    }

    @Test
    void rejectsZeroBins() {
        assertThrows(IllegalArgumentException.class, () -> new Histogram(new double[]{1.0}, 0));
    }
}
