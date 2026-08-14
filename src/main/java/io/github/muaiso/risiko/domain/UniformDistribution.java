package io.github.muaiso.risiko.domain;

/**
 * Stetige Gleichverteilung ueber das Intervall {@code [lower, upper]}.
 *
 * <p>Modelliert "wir wissen nur, dass der Wert irgendwo zwischen A und B
 * liegt, aber nicht wo genau" — eine konservative Unsicherheitsannahme
 * ohne centroiding.</p>
 */
public final class UniformDistribution implements Distribution {

    private final double lower;
    private final double upper;

    public UniformDistribution(double lower, double upper) {
        if (upper < lower) {
            throw new IllegalArgumentException("upper muss >= lower sein");
        }
        this.lower = lower;
        this.upper = upper;
    }

    public double getLower() {
        return lower;
    }

    public double getUpper() {
        return upper;
    }

    @Override
    public double sample(RandomSource rng) {
        return lower + (upper - lower) * rng.nextDouble();
    }

    @Override
    public double expectedValue() {
        return (lower + upper) / 2.0;
    }

    @Override
    public double minimum() {
        return lower;
    }

    @Override
    public double maximum() {
        return upper;
    }
}
