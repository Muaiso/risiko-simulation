package io.github.muaiso.risiko.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.exception.InvalidProjectException;

/**
 * Testet die DTO-Validierung auf API-Ebene: Pflichtfelder, Wertebereiche
 * und die Ablehnung unvollstaendiger Cashflows.
 */
class DtoValidationTest {

    @Test
    void toDomainRejectsMissingFixedAndDistribution() {
        var req = new Dtos.SimulationRequest(
                "X", 0.1,
                java.util.List.of(new Dtos.CashFlowDto(1, null, null)),
                1000, 1L);
        assertThrows(InvalidProjectException.class, () -> Dtos.toDomain(req));
    }

    @Test
    void toDomainRejectsUnknownDistributionType() {
        var dist = new Dtos.DistributionDto("weird", null, null, null, null, null, null, null, null, null);
        var req = new Dtos.SimulationRequest(
                "X", 0.1,
                java.util.List.of(new Dtos.CashFlowDto(1, null, dist)),
                1000, 1L);
        assertThrows(InvalidProjectException.class, () -> Dtos.toDomain(req));
    }

    @Test
    void toDomainHonorsSeedAndIterations() {
        var flow = new Dtos.CashFlowDto(0, -100.0, null);
        var req = new Dtos.SimulationRequest("Y", 0.05, java.util.List.of(flow), 250, 99L);
        var project = Dtos.toDomain(req);
        assertEquals("Y", project.name());
        assertEquals(0.05, project.discountRate());
    }
}
