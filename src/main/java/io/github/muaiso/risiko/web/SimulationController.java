package io.github.muaiso.risiko.web;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.service.SimulationService;
import io.github.muaiso.risiko.service.SimulationService.SensitivityView;
import io.github.muaiso.risiko.service.SimulationService.SimulationResult;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller fuer die Monte-Carlo Risiko-Simulation.
 *
 * <p>Einziger Endpunkt: {@code POST /api/v1/simulations}. Nimmt eine
 * Projektdefinition entgegen und liefert die vollstaendige Analyse
 * (NPV-Verteilung, Risikokennzahlen, Sensitivitaet).</p>
 */
@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    public ResponseEntity<Dtos.SimulationResponse> simulate(
            @Valid @RequestBody Dtos.SimulationRequest request) {

        InvestmentProject project = Dtos.toDomain(request);

        long seed = request.seed() != null ? request.seed() : System.nanoTime();
        int iterations = request.iterations() != null
                ? request.iterations()
                : SimulationService.DEFAULT_ITERATIONS;

        SimulationResult result = simulationService.analyze(project, iterations, seed);

        return ResponseEntity.ok(toResponse(result));
    }

    private Dtos.SimulationResponse toResponse(SimulationResult result) {
        var sensitivity = result.sensitivity().stream()
                .map(this::toDto)
                .toList();
        return new Dtos.SimulationResponse(
                result.projectName(),
                result.iterations(),
                result.expectedNpv(),
                result.meanNpv(),
                result.standardDeviation(),
                result.minNpv(),
                result.maxNpv(),
                result.percentile5(),
                result.percentile50(),
                result.percentile95(),
                result.valueAtRisk95(),
                result.conditionalVaR95(),
                result.valueAtRisk99(),
                result.probabilityOfLoss(),
                result.sharpeRatio(),
                result.sortinoRatio(),
                result.coefficientOfVariation(),
                result.skewness(),
                result.excessKurtosis(),
                sensitivity);
    }

    private Dtos.SensitivityDto toDto(SensitivityView r) {
        return new Dtos.SensitivityDto(
                r.cashFlowIndex(),
                r.year(),
                r.npvAtMinimum(),
                r.npvAtMaximum(),
                r.range());
    }
}
