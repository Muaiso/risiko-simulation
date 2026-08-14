package io.github.muaiso.risiko.domain;

/**
 * Abstraktion ueber eine Quelle von Zufallszahlen.
 *
 * <p>Die Simulation nutzt bewusst kein {@code Math.random()}, sondern ein
 * injizierbares {@code RandomSource}. Das erlaubt deterministische Tests
 * (feste Seed-Sequenzen) und den spaeteren Austausch gegen kryptografisch
 * sichere oder quasizufaellige Generatoren (z. B. Sobol).</p>
 */
public interface RandomSource {

    /**
     * Liefert die naechste Pseudozufallszahl aus dem Intervall {@code [0, 1)}.
     *
     * @return gleichverteilte Zahl im halboffenen Einheitsintervall
     */
    double nextDouble();

    /**
     * Liefert die naechste standardnormalverteilte Zufallszahl (Mittel 0, SD 1).
     *
     * @return standardnormalverteilter Zufallswert
     */
    double nextGaussian();
}
