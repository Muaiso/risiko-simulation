# Changelog

Alle wesentlichen Änderungen an diesem Projekt werden in dieser Datei
dokumentiert. Das Format lehnt sich an [Keep a Changelog](https://keepachangelog.com/)
an.

## [0.2.0] – 2026-08-16

### Hinzugefügt
- Erweiterte Risikokennzahlen: Sharpe Ratio, Sortino Ratio, Downside Deviation, Variationskoeffizient
- Zentrale Momente der NPV-Verteilung: Schiefe (Skewness) und Exzess-Wölbung (Kurtosis)
- `Histogram`-Klasse zur kompakten Verteilungsdarstellung der NPV-Stichprobe
- `PaybackAnalyzer`: diskontierte Amortisationsperiode (Base-Case, interpoliert)
- `ScenarioAnalyzer`: Best-/Base-/Worst-Case-NPV auf Basis der Verteilungsränder
- Neue REST-Endpunkte `GET /api/v1/simulations/payback` und `/scenarios`
- Erweiterte `SimulationResult`- und Response-DTOs um die neuen Kennzahlen
- 13 neue Unit-/Integrationstests (Momente, Risiko-Ratios, Histogramm, Payback, Szenario, Web)

## [0.1.0] – 2026-08-14

### Hinzugefügt
- Monte-Carlo-Simulator für die NPV-Verteilung eines Investitionsprojekts
- Vier Wahrscheinlichkeitsverteilungen: Normal, Uniform, Triangular, LogNormal
- Deskriptive Statistik (Mittel, Varianz, Perzentile P5/P50/P95)
- Risikokennzahlen: Value-at-Risk (95 %/99 %), Conditional VaR, Verlustwahrscheinlichkeit
- Sensitivitätsanalyse (Tornado) zur Identifikation der Unsicherheits-Treiber
- REST-API `POST /api/v1/simulations` mit OpenAPI/Swagger-Dokumentation
- Reproduzierbare Simulationen über einen Seed-Parameter
- Layering-Test (reflectionsbasiert, Java-25-kompatibel)
- 68 Unit-/Integrationstests, ~90 % Instruction-Coverage
