package io.github.muaiso.risiko.domain;

/**
 * Dreiecksverteilung mit unterer Grenze {@code a}, Mode (wohlsten Fall,
 * Spitze) {@code c} und oberer Grenze {@code b}.
 *
 * <p>Sehr verbreitet in der Investitionsrechnung, weil Fachbereiche drei
 * Punkte sicher angeben koennen: pessimistic (a), most-likely (c),
 * optimistic (b). Die Verteilung ist rechtsschief, wenn {@code c} naeher
 * an {@code a} liegt.</p>
 */
public final class TriangularDistribution implements Distribution {

    private final double a;
    private final double b;
    private final double c;

    public TriangularDistribution(double a, double b, double c) {
        if (b < a) {
            throw new IllegalArgumentException("b (oben) muss >= a (unten) sein");
        }
        if (c < a || c > b) {
            throw new IllegalArgumentException("c (Mode) muss in [a, b] liegen");
        }
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    @Override
    public double sample(RandomSource rng) {
        double u = rng.nextDouble();
        double fc = (c - a) / (b - a);
        if (u < fc) {
            return a + Math.sqrt(u * (b - a) * (c - a));
        }
        return b - Math.sqrt((1.0 - u) * (b - a) * (b - c));
    }

    @Override
    public double expectedValue() {
        return (a + b + c) / 3.0;
    }

    @Override
    public double minimum() {
        return a;
    }

    @Override
    public double maximum() {
        return b;
    }
}
