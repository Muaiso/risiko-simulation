# Contributing

Danke für dein Interesse an diesem Portfolio-Projekt! Es dient der
Demonstration einer sauber geschichteten Risiko-Simulations-Engine und ist
im Kern "abgeschlossen". Dennoch sind korrekte Erweiterungen willkommen.

## Architektur-Regeln

Die Layering-Regeln werden durch `LayeringTest` erzwungen:

- `web` darf nur `service` und `domain` nutzen
- `service` darf nur `engine` und `domain` nutzen
- `engine` darf nur `domain` nutzen
- `domain` darf keine anderen Schichten nutzen

Verstöße lassen den Build fehlschlagen.

## Lokales Entwickeln

```bash
mvn package      # baut und testet
mvn verify       # inkl. JaCoCo-Coverage
```

## Commit-Stil

- Präfix: `feat:`, `fix:`, `test:`, `docs:`, `chore:`, `ci:`, `refactor:`
- Imperativ, Deutsch, kurze erste Zeile
- Jeder Commit soll eine in sich geschlossene, kompilierfähige Änderung sein

## Tests

Neue Fachlogik benötigt Tests. Ziel-Coverage: > 85 % Instructions.
