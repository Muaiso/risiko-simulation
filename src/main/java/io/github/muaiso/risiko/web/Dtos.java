package io.github.muaiso.risiko.web;

import io.github.muaiso.risiko.domain.Distribution;
import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.LogNormalDistribution;
import io.github.muaiso.risiko.domain.NormalDistribution;
import io.github.muaiso.risiko.domain.StochasticCashFlow;
import io.github.muaiso.risiko.domain.TriangularDistribution;
import io.github.muaiso.risiko.domain.UniformDistribution;
import io.github.muaiso.risiko.exception.InvalidProjectException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * API-Datentraeger (Request/Response) fuer die Risiko-Simulations-Endpunkte.
 *
 * <p>Diese Klassen entkoppeln die HTTP-Schicht von der Domain. Die
 * {@code toDomain()}-Methoden uebersetzen eingehende JSON in das
 * fachliche {@link InvestmentProject}-Modell.</p>
 */
public final class Dtos {

    private Dtos() {
    }

    /** Anfrage: Projektdefinition plus Simulationsparameter. */
    public record SimulationRequest(
            @NotBlank String name,
            @NotNull @DecimalMin("0.0") Double discountRate,
            @NotEmpty @Valid List<@Valid CashFlowDto> cashFlows,
            @Min(1) Integer iterations,
            Long seed) {
    }

    /** Ein Zahlungsstrom in der API (deterministisch oder ueber Verteilung). */
    public record CashFlowDto(
            @Min(0) Integer year,
            Double fixedAmount,
            @Valid DistributionDto distribution) {
    }

    /** Verteilungsdefinition in der API. */
    public record DistributionDto(
            @NotBlank String type,
            Double mu,
            Double sigma,
            Double lower,
            Double upper,
            Double a,
            Double b,
            Double c,
            Double median,
            Double spread) {
    }

    /** Antwort: zentrale Kennzahlen der Analyse. */
    public record SimulationResponse(
            String projectName,
            int iterations,
            double expectedNpv,
            double meanNpv,
            double standardDeviation,
            double minNpv,
            double maxNpv,
            double percentile5,
            double percentile50,
            double percentile95,
            double valueAtRisk95,
            double conditionalVaR95,
            double valueAtRisk99,
            double probabilityOfLoss,
            List<SensitivityDto> sensitivity) {
    }

    /** Antwort: ein Sensitivitaets-Eintrag (Tornado). */
    public record SensitivityDto(
            int cashFlowIndex,
            int year,
            double npvAtMinimum,
            double npvAtMaximum,
            double range) {
    }

    /**
     * Uebersetzt die Anfrage in ein fachliches {@link InvestmentProject}.
     *
     * @param req die eingehende Anfrage
     * @return das Domain-Projekt
     */
    public static InvestmentProject toDomain(SimulationRequest req) {
        List<StochasticCashFlow> flows = req.cashFlows().stream()
                .map(Dtos::toDomainFlow)
                .toList();
        return new InvestmentProject(req.name(), req.discountRate(), flows);
    }

    private static StochasticCashFlow toDomainFlow(CashFlowDto dto) {
        if (dto.distribution() != null) {
            return StochasticCashFlow.stochastic(dto.year(), toDomainDistribution(dto.distribution()));
        }
        if (dto.fixedAmount() == null) {
            throw new InvalidProjectException(
                    "Cashflow Jahr " + dto.year() + ": entweder fixedAmount oder distribution erforderlich");
        }
        return StochasticCashFlow.fixed(dto.year(), dto.fixedAmount());
    }

    private static Distribution toDomainDistribution(DistributionDto d) {
        return switch (d.type().toLowerCase()) {
            case "normal" -> new NormalDistribution(req(d.mu, "normal.mu"), req(d.sigma, "normal.sigma"));
            case "uniform" -> new UniformDistribution(req(d.lower, "uniform.lower"), req(d.upper, "uniform.upper"));
            case "triangular" -> new TriangularDistribution(
                    req(d.a, "triangular.a"), req(d.b, "triangular.b"), req(d.c, "triangular.c"));
            case "lognormal" -> new LogNormalDistribution(
                    req(d.median, "lognormal.median"), req(d.spread, "lognormal.spread"));
            default -> throw new InvalidProjectException("Unbekannter Verteilungstyp: " + d.type());
        };
    }

    private static double req(Double value, String field) {
        if (value == null) {
            throw new InvalidProjectException("Feld " + field + " fehlt");
        }
        return value;
    }
}
