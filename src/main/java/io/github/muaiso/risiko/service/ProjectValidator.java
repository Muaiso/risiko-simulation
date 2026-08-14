package io.github.muaiso.risiko.service;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.exception.InvalidProjectException;

/**
 * Validiert ein {@link InvestmentProject} und die Simulationsparameter
 * vor der eigentlichen Analyse.
 *
 * <p>Haelt die Validierungsregeln gekapselt, damit der
 * {@code SimulationService} nur die fachliche Orchestrierung enthaelt
 * und die Regeln isoliert testbar sind.</p>
 */
public final class ProjectValidator {

    /** Maximale zulaessige Iterationszahl (Schutz vor Ressourcen-Erschoepfung). */
    public static final int MAX_ITERATIONS = 5_000_000;

    private ProjectValidator() {
    }

    /**
     * Prueft Projekt und Iterationszahl.
     *
     * @param project     das zu analysierende Projekt
     * @param iterations  Anzahl der Monte-Carlo-Laeufe
     * @throws InvalidProjectException wenn die Eingabe unzulaessig ist
     */
    public static void validate(InvestmentProject project, int iterations) {
        if (iterations <= 0) {
            throw new InvalidProjectException("Iterationen muessen > 0 sein");
        }
        if (iterations > MAX_ITERATIONS) {
            throw new InvalidProjectException(
                    "Iterationen duerfen " + MAX_ITERATIONS + " nicht ueberschreiten");
        }
        if (project.cashFlows().isEmpty()) {
            throw new InvalidProjectException("Projekt hat keine Cashflows");
        }
    }
}
