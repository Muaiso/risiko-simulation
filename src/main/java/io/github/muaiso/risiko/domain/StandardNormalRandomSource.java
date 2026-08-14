package io.github.muaiso.risiko.domain;

import java.util.random.RandomGenerator;

/**
 * {@link RandomSource}, das auf einem JDK-{@link RandomGenerator} aufbaut.
 *
 * <p>Die Gauß-Verteilung wird ueber die Box-Muller-Transformation erzeugt.
 * Die Klasse ist bei gleichem Seed vollstaendig deterministisch und damit
 * fuer reproduzierbare Simulationen und Unit-Tests geeignet.</p>
 */
public final class StandardNormalRandomSource implements RandomSource {

    private final RandomGenerator generator;

    public StandardNormalRandomSource(RandomGenerator generator) {
        this.generator = generator;
    }

    @Override
    public double nextDouble() {
        return generator.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return generator.nextGaussian();
    }
}
