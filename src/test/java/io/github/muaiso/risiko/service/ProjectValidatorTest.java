package io.github.muaiso.risiko.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.StochasticCashFlow;
import io.github.muaiso.risiko.exception.InvalidProjectException;

/**
 * Testet die gekapselte Projektvalidierung.
 */
class ProjectValidatorTest {

    private InvestmentProject validProject() {
        return new InvestmentProject("P", 0.1, java.util.List.of(
                StochasticCashFlow.fixed(0, -100.0),
                StochasticCashFlow.stochastic(1, new io.github.muaiso.risiko.domain.NormalDistribution(120.0, 10.0))));
    }

    @Test
    void acceptsValidProject() {
        assertDoesNotThrow(() -> ProjectValidator.validate(validProject(), 10_000));
    }

    @Test
    void rejectsZeroIterations() {
        assertThrows(InvalidProjectException.class,
                () -> ProjectValidator.validate(validProject(), 0));
    }

    @Test
    void rejectsNegativeIterations() {
        assertThrows(InvalidProjectException.class,
                () -> ProjectValidator.validate(validProject(), -1));
    }

    @Test
    void rejectsTooManyIterations() {
        assertThrows(InvalidProjectException.class,
                () -> ProjectValidator.validate(validProject(), ProjectValidator.MAX_ITERATIONS + 1));
    }

    @Test
    void rejectsEmptyCashFlows() {
        // InvestmentProject lehnt leere Cashflows bereits im Konstruktor ab
        assertThrows(IllegalArgumentException.class,
                () -> new InvestmentProject("E", 0.1, java.util.List.of()));
    }
}
