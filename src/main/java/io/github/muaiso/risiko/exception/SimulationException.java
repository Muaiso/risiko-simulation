package io.github.muaiso.risiko.exception;

/**
 * Basisklasse fuer alle fachlichen Fehler der Risiko-Simulation.
 */
public class SimulationException extends RuntimeException {

    public SimulationException(String message) {
        super(message);
    }

    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
