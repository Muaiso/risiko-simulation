package io.github.muaiso.risiko.domain;

/**
 * Normalverteilung (Gauß-Verteilung) mit Mittelwert {@code mu} und
 * Standardabweichung {@code sigma}.
 *
 * <p>Haeufig verwendet fuer Cashflows, deren Unsicherheit symmetrisch um
 * einen Erwartungswert streut. Negativwerte sind mathematisch moeglich;
 * bei Bedarf sollte der Aufrufer die Probe auf {@code >= 0} begrenzen.</p>
 */
public final class NormalDistribution implements Distribution {

    private final double mu;
    private final double sigma;

    public NormalDistribution(double mu, double sigma) {
        if (sigma < 0) {
            throw new IllegalArgumentException("Standardabweichung sigma muss >= 0 sein");
        }
        this.mu = mu;
        this.sigma = sigma;
    }

    public double getMu() {
        return mu;
    }

    public double getSigma() {
        return sigma;
    }

    @Override
    public double sample(RandomSource rng) {
        return mu + sigma * rng.nextGaussian();
    }

    @Override
    public double expectedValue() {
        return mu;
    }

    @Override
    public double minimum() {
        // 6-Sigma-Grenze als praktische Untergrenze (99,9999 % der Masse)
        return mu - 6.0 * sigma;
    }

    @Override
    public double maximum() {
        return mu + 6.0 * sigma;
    }
}
