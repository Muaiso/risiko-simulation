package io.github.muaiso.risiko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Risiko-Simulations-Anwendung.
 *
 * <p>Spring Boot 4 Anwendung mit WebMVC. Die eigentliche Fachlogik liegt
 * in den Packages {@code domain}, {@code engine} und {@code service}.</p>
 */
@SpringBootApplication
public class RisikoSimulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RisikoSimulationApplication.class, args);
    }
}
