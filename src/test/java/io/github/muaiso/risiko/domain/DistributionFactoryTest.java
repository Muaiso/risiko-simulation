package io.github.muaiso.risiko.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.exception.InvalidProjectException;

/**
 * Testet die DistributionFactory: typ- und parametergesteuerte Erzeugung
 * sowie Fehlerfaelle (fehlende Felder, unbekannter Typ).
 */
class DistributionFactoryTest {

    @Test
    void createsNormal() {
        Distribution d = DistributionFactory.create("normal", 10.0, 2.0, null, null, null, null, null, null, null);
        assertEquals(10.0, d.expectedValue(), 1e-9);
    }

    @Test
    void createsUniform() {
        Distribution d = DistributionFactory.create("uniform", null, null, 0.0, 10.0, null, null, null, null, null);
        assertEquals(5.0, d.expectedValue(), 1e-9);
    }

    @Test
    void createsTriangular() {
        Distribution d = DistributionFactory.create("triangular", null, null, 0.0, 10.0, 0.0, 10.0, 5.0, null, null);
        assertEquals(5.0, d.expectedValue(), 1e-9);
    }

    @Test
    void createsLogNormal() {
        Distribution d = DistributionFactory.create("lognormal", null, null, null, null, null, null, null, 100.0, 2.0);
        assertTrue(d.expectedValue() > 100.0);
    }

    @Test
    void acceptsTypeCaseInsensitive() {
        Distribution d = DistributionFactory.create("NORMAL", 1.0, 1.0, null, null, null, null, null, null, null);
        assertEquals(1.0, d.expectedValue(), 1e-9);
    }

    @Test
    void missingParameterThrows() {
        assertThrows(InvalidProjectException.class,
                () -> DistributionFactory.create("normal", null, 1.0, null, null, null, null, null, null, null));
    }

    @Test
    void unknownTypeThrows() {
        assertThrows(InvalidProjectException.class,
                () -> DistributionFactory.create("banana", 1.0, 1.0, null, null, null, null, null, null, null));
    }

    private void assertTrue(boolean b) {
        org.junit.jupiter.api.Assertions.assertTrue(b);
    }
}
