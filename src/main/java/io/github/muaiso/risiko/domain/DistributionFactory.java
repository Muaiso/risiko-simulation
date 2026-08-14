package io.github.muaiso.risiko.domain;

import io.github.muaiso.risiko.exception.InvalidProjectException;

/**
 * Fabrik zur Erzeugung einer {@link Distribution} aus einem typisierten
 * Schluesselwort und den zugehoerigen Parametern.
 *
 * <p>Zentralisiert die Abbildung "API-Typ-String -> fachliche Verteilung"
 * und entlastet damit die DTO-Schicht. Unbekannte Typen fuehren zu einer
 * {@link InvalidProjectException}.</p>
 */
public final class DistributionFactory {

    private DistributionFactory() {
    }

    /**
     * Erzeugt eine Verteilung anhand von Typ und Parametern.
     *
     * @param type     "normal" | "uniform" | "triangular" | "lognormal"
     * @param mu       Normal: Mittelwert (optional)
     * @param sigma    Normal: Standardabweichung (optional)
     * @param lower    Uniform: untere Grenze (optional)
     * @param upper    Uniform: obere Grenze (optional)
     * @param a        Triangular: untere Grenze (optional)
     * @param b        Triangular: obere Grenze (optional)
     * @param c        Triangular: Mode (optional)
     * @param median   LogNormal: Median (optional)
     * @param spread   LogNormal: Spanne (optional)
     * @return die erzeugte Verteilung
     */
    public static Distribution create(String type, Double mu, Double sigma,
                                      Double lower, Double upper,
                                      Double a, Double b, Double c,
                                      Double median, Double spread) {
        return switch (type.toLowerCase()) {
            case "normal" -> new NormalDistribution(req(mu, "normal.mu"), req(sigma, "normal.sigma"));
            case "uniform" -> new UniformDistribution(req(lower, "uniform.lower"), req(upper, "uniform.upper"));
            case "triangular" -> new TriangularDistribution(
                    req(a, "triangular.a"), req(b, "triangular.b"), req(c, "triangular.c"));
            case "lognormal" -> new LogNormalDistribution(
                    req(median, "lognormal.median"), req(spread, "lognormal.spread"));
            default -> throw new InvalidProjectException("Unbekannter Verteilungstyp: " + type);
        };
    }

    private static double req(Double value, String field) {
        if (value == null) {
            throw new InvalidProjectException("Feld " + field + " fehlt");
        }
        return value;
    }
}
