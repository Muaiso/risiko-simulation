package io.github.muaiso.risiko.exception;

/**
 * Wird geworfen, wenn ein uebergebenes Projekt fachlich unzulaessig ist
 * (z. B. negative Iterationen, leere Cashflows).
 */
public class InvalidProjectException extends SimulationException {

    public InvalidProjectException(String message) {
        super(message);
    }
}
