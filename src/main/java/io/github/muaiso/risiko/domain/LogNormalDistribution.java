package io.github.muaiso.risiko.domain;

/**
 * Lognormalverteilung, parametrisiert ueber den Median {@code median} und
 * einen Multiplikator {@code spread} (Verhaeltnis 95-Perzentil / Median).
 *
 * <p>Log-Normal ist die kanonische Wahl fuer strikt positive Groessen mit
 * rechtsschiefer Verteilung: Marktgroessen, Umsatzpotenziale, Preise.
 * Ein Median von 100 mit spread 1.5 bedeutet: 50 % der Realisierungen
 * liegen unter 100, 95 % unter 150.</p>
 */
public final class LogNormalDistribution implements Distribution {

    private final double median;
    private final double spread;

    public LogNormalDistribution(double median, double spread) {
        if (median <= 0) {
            throw new IllegalArgumentException("Median muss > 0 sein");
        }
        if (spread <= 1) {
            throw new IllegalArgumentException("Spread muss > 1 sein");
        }
        this.median = median;
        this.spread = spread;
    }

    public double getMedian() {
        return median;
    }

    public double getSpread() {
        return spread;
    }

    @Override
    public double sample(RandomSource rng) {
        // sigma aus dem 95-Persentil-Ansatz: median * spread = median * exp(1.645 * sigma)
        double sigma = Math.log(spread) / 1.645;
        double mu = Math.log(median);
        double z = mu + sigma * rng.nextGaussian();
        return Math.exp(z);
    }

    @Override
    public double expectedValue() {
        double sigma = Math.log(spread) / 1.645;
        return median * Math.exp(0.5 * sigma * sigma);
    }

    @Override
    public double minimum() {
        return 0.0;
    }

    @Override
    public double maximum() {
        // 99,9 % Perzentil als praktische Obergrenze
        double sigma = Math.log(spread) / 1.645;
        return median * Math.exp(3.09 * sigma);
    }
}
