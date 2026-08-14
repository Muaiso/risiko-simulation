package io.github.muaiso.risiko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Zentraler Exception-Handler: uebersetzt fachliche Ausnahmen in RFC-7807
 * ProblemDetail-Antworten mit passendem HTTP-Status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidProjectException.class)
    public ProblemDetail handleInvalidProject(InvalidProjectException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Ungueltiges Projekt");
        return detail;
    }

    @ExceptionHandler(SimulationException.class)
    public ProblemDetail handleSimulation(SimulationException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        detail.setTitle("Simulationsfehler");
        return detail;
    }
}
