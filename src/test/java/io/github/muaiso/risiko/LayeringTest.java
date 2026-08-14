package io.github.muaiso;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.github.muaiso.risiko.RisikoSimulationApplication;

/**
 * Layering-Test ohne Fremd-Bibliothek: laedt alle fachlichen Klassen ueber
 * Reflection und erzwingt die Schichtung.
 *
 * <p>Regeln:
 * Web darf nur Service + Domain nutzen;
 * Service darf nur Engine + Domain nutzen;
 * Engine darf nur Domain nutzen;
 * Domain darf keine anderen Schichten nutzen.</p>
 */
class LayeringTest {

    private enum Layer {
        WEB("io.github.muaiso.risiko.web"),
        SERVICE("io.github.muaiso.risiko.service"),
        ENGINE("io.github.muaiso.risiko.engine"),
        DOMAIN("io.github.muaiso.risiko.domain"),
        OTHER("");

        final String pkg;

        Layer(String pkg) {
            this.pkg = pkg;
        }

        static Layer of(String className) {
            for (Layer l : values()) {
                if (!l.pkg.isEmpty() && className.startsWith(l.pkg)) {
                    return l;
                }
            }
            return OTHER;
        }
    }

    @Test
    void respectsLayerBoundaries() throws IOException {
        // stellt sicher, dass die Main-Klassen ueberhaupt im Classpath sind
        RisikoSimulationApplication.class.getName();

        Path classesDir = Paths.get("target", "classes");
        assertTrue(Files.exists(classesDir), "target/classes muss existieren (vorher mvn compile ausfuehren)");

        List<String> ownPackages = List.of(
                "io.github.muaiso.risiko.web",
                "io.github.muaiso.risiko.service",
                "io.github.muaiso.risiko.engine",
                "io.github.muaiso.risiko.domain");

        List<String> violations = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();

        try (Stream<Path> files = Files.walk(classesDir)) {
            for (Path file : (Iterable<Path>) files
                    .filter(p -> p.toString().endsWith(".class") && !p.toString().contains("$"))
                    ::iterator) {
                String className = toClassName(classesDir, file);
                Layer from = Layer.of(className);
                if (from == Layer.OTHER) {
                    continue;
                }
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    violations.add("Konnte Klasse nicht laden: " + className);
                }
            }
        }

        for (Class<?> clazz : classes) {
            Layer from = Layer.of(clazz.getName());
            inspect(clazz, from, ownPackages, violations);
        }

        assertTrue(violations.isEmpty(),
                "Layering-Verletzungen gefunden:\n" + String.join("\n", violations));
    }

    private void inspect(Class<?> clazz, Layer from, List<String> ownPackages, List<String> violations) {
        // Felder
        for (var f : clazz.getDeclaredFields()) {
            checkType(clazz.getName(), from, f.getType().getName(), ownPackages, violations);
        }
        // Methoden: Parameter- und Rueckgabetypen
        for (var m : clazz.getDeclaredMethods()) {
            checkType(clazz.getName(), from, m.getReturnType().getName(), ownPackages, violations);
            for (var p : m.getParameterTypes()) {
                checkType(clazz.getName(), from, p.getName(), ownPackages, violations);
            }
        }
        // direkte Superklasse
        var sup = clazz.getSuperclass();
        if (sup != null && sup != Object.class) {
            checkType(clazz.getName(), from, sup.getName(), ownPackages, violations);
        }
        // implementierte Interfaces
        for (var i : clazz.getInterfaces()) {
            checkType(clazz.getName(), from, i.getName(), ownPackages, violations);
        }
    }

    private void checkType(String className, Layer from, String ref, List<String> ownPackages, List<String> violations) {
        if (!ownPackages.stream().anyMatch(ref::startsWith)) {
            return; // externe Abhaengigkeit (Spring, JDK) ignorieren
        }
        Layer to = Layer.of(ref);
        if (to == Layer.OTHER || to == from) {
            return;
        }
        boolean allowed = switch (from) {
            case WEB -> to == Layer.SERVICE || to == Layer.DOMAIN;
            case SERVICE -> to == Layer.ENGINE || to == Layer.DOMAIN;
            case ENGINE -> to == Layer.DOMAIN;
            case DOMAIN -> false;
            default -> true;
        };
        if (!allowed) {
            violations.add(from + " (" + className + ") nutzt unerlaubt " + to + " (" + ref + ")");
        }
    }

    private String toClassName(Path root, Path file) {
        String rel = root.relativize(file).toString();
        return rel.substring(0, rel.length() - ".class".length()).replace('\\', '/').replace('/', '.');
    }
}
