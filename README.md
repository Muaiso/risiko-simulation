# Risiko-Simulation

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Build](https://github.com/Muaiso/risiko-simulation/actions/workflows/ci.yml/badge.svg)](https://github.com/Muaiso/risiko-simulation/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Coverage](https://img.shields.io/badge/Tests-90%2B%20green-brightgreen)](#tests)
[![Maven](https://img.shields.io/badge/build-maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

**Monte-Carlo Risiko-Simulation für Investitionsentscheidungen** – eine
Spring-Boot-Anwendung, die den Kapitalwert (NPV) eines Projekts unter
Unsicherheit bewertet und Entscheidern quantifizierbare Risikokennzahlen
liefert: **Value-at-Risk (VaR)**, **Conditional VaR (Expected Shortfall)**,
Verlustwahrscheinlichkeit und eine **Sensitivitätsanalyse (Tornado)**.

> Wirtschaftsinformatik-Portfolio-Projekt. Reine Backend-/Engine-Anwendung
> mit REST-API, sauber geschichteter Architektur und umfangreicher
> Testabdeckung.

---

## Inhalt

- [Motivation](#motivation)
- [Funktionsumfang](#funktionsumfang)
- [Architektur](#architektur)
- [Domänenmodell](#domänenmodell)
- [API-Referenz](#api-referenz)
- [Lokales Starten](#lokales-starten)
- [Beispiel-Request](#beispiel-request)
- [Mathematische Grundlagen](#mathematische-grundlagen)
- [Tests](#tests)
- [Tech-Stack](#tech-stack)

---

## Motivation

Investitionsentscheidungen basieren selten auf festen Zahlen. Umsätze,
Kosten und Marktgrößen sind unsicher. Eine klassische NPV-Rechnung mit
„geschätzten" Einzelwerten versteckt diese Unsicherheit. Dieses Projekt
macht sie sichtbar:

1. Jeder unsichere Zahlungsstrom wird durch eine **Wahrscheinlichkeitsverteilung** modelliert.
2. Der **Monte-Carlo-Simulator** zieht tausende Realisierungen und bildet die NPV-Verteilung ab.
3. Aus der Verteilung werden **Risikokennzahlen** abgeleitet, die eine fundierte Go/No-Go-Entscheidung erlauben.

---

## Funktionsumfang

| Funktion | Beschreibung |
|----------|--------------|
| NPV (deterministisch) | Erwartungswert-Kapitalwert auf Basis der Verteilungsmittel |
| Monte-Carlo-Simulation | NPV-Verteilung über `n` Läufe (Standard 50.000) |
| Deskriptive Statistik | Mittelwert, Standardabweichung, Min/Max, Perzentile (P5/P50/P95) |
| Value-at-Risk (VaR) | Verlustschranke auf 95 % / 99 % Konfidenz |
| Conditional VaR (CVaR) | Erwarteter Schwanzverlust (Expected Shortfall) |
| Verlustwahrscheinlichkeit | Anteil negativer NPV-Realisierungen |
| Sensitivitätsanalyse | Tornado: welcher Input treibt die Unsicherheit am stärksten? |
| Reproduzierbarkeit | Fester Seed → identische Simulationsergebnisse |

---

## Architektur

Das Projekt folgt einer strikten **Layering-Architektur** (durch
`ArchitectureTest` erzwungen):

```
┌─────────────────────────────────────────────────────────────┐
│  web (io.github.muaiso.risiko.web)                          │
│  REST-Controller, DTOs, Request/Response-Mapping            │
└───────────────────────────┬─────────────────────────────────┘
                            │ nutzt
┌───────────────────────────▼─────────────────────────────────┐
│  service (io.github.muaiso.risiko.service)                  │
│  SimulationService: Orchestrierung der Analyse             │
└───────────────────────────┬─────────────────────────────────┘
                            │ nutzt
┌───────────────────────────▼─────────────────────────────────┐
│  engine (io.github.muaiso.risiko.engine)                    │
│  NPV, MonteCarlo, Statistik, RiskMetrics, Sensitivity       │
└───────────────────────────┬─────────────────────────────────┘
                            │ nutzt
┌───────────────────────────▼─────────────────────────────────┐
│  domain (io.github.muaiso.risiko.domain)                    │
│  Distributionen, CashFlow, InvestmentProject, RandomSource  │
└─────────────────────────────────────────────────────────────┘
```

**Package-Übersicht:**

- `domain` – fachliche Modellierung (keine Abhängigkeiten nach außen)
- `engine` – Algorithmen (NPV, Simulation, Statistik, Risiko, Sensitivität)
- `service` – fachliche Orchestrierung, zustandslos und thread-sicher
- `web` – HTTP-Ebene, DTO-Übersetzung, Validierung
- `config` – OpenAPI-Metadaten
- `exception` – fachliche Fehler + zentraler `ExceptionHandler` (RFC-7807)

---

## Domänenmodell

### Verteilungen (`Distribution`)

| Typ | Parameter | Einsatz |
|-----|-----------|---------|
| `NormalDistribution` | `mu`, `sigma` | symmetrische Unsicherheit um einen Erwartungswert |
| `UniformDistribution` | `lower`, `upper` | „wir wissen nur die Spanne" |
| `TriangularDistribution` | `a`, `b`, `c` (pessimistisch, optimistisch, wahrscheinlichster) | Experten-Schätzung mit 3 Punkten |
| `LogNormalDistribution` | `median`, `spread` | strikt positive, rechtsschiefe Größen (Umsatz, Markt) |

### Projekt (`InvestmentProject`)

Ein Projekt besteht aus einem Namen, einem Abzinsungssatz (Kapitalkosten)
und einer Liste von `StochasticCashFlow`s. Jeder Cashflow ist entweder
**deterministisch** (`fixedAmount`, z. B. die Anfangsinvestition) oder
**stochastisch** (eine `Distribution`).

---

## API-Referenz

### `POST /api/v1/simulations`

Bewertet ein Projekt und liefert die vollständige Risikoanalyse.

**Request-Body:**
```json
{
  "name": "Maschinenauslastung",
  "discountRate": 0.08,
  "iterations": 50000,
  "seed": 12345,
  "cashFlows": [
    { "year": 0, "fixedAmount": -1000000 },
    { "year": 1, "distribution": { "type": "normal", "mu": 600000, "sigma": 50000 } },
    { "year": 2, "distribution": { "type": "triangular", "a": 400000, "b": 900000, "c": 650000 } },
    { "year": 3, "distribution": { "type": "lognormal", "median": 700000, "spread": 1.8 } }
  ]
}
```

**Response (gekürzt):**
```json
{
  "projectName": "Maschinenauslastung",
  "iterations": 50000,
  "expectedNpv": 928410.22,
  "meanNpv": 925118.74,
  "standardDeviation": 142203.18,
  "percentile5": 702441.30,
  "percentile50": 926884.91,
  "percentile95": 1148102.55,
  "valueAtRisk95": 0.0,
  "conditionalVaR95": 0.0,
  "valueAtRisk99": 0.0,
  "probabilityOfLoss": 0.012,
  "sensitivity": [
    { "cashFlowIndex": 2, "year": 2, "npvAtMinimum": 540221.10, "npvAtMaximum": 1310880.44, "range": 770659.34 },
    { "cashFlowIndex": 3, "year": 3, "npvAtMinimum": 612330.55, "npvAtMaximum": 1233990.88, "range": 621660.33 },
    { "cashFlowIndex": 1, "year": 1, "npvAtMinimum": 847710.02, "npvAtMaximum": 1002527.46, "range": 154817.44 }
  ]
}
```

### Weitere Endpunkte

- `GET /health` – Actuator Health-Check
- `GET /v3/api-docs` – OpenAPI-Spezifikation (JSON)
- `GET /swagger-ui.html` – interaktive API-Dokumentation

> **Hinweis:** `/swagger-ui.html` ist die API-Dokumentation, **nicht** die
> Anwendung selbst. Die Simulation läuft ausschließlich über die
> REST-Endpunkte.

---

## Lokales Starten

**Voraussetzungen:** JDK 25, Maven 3.9+.

```bash
# Projekt bauen und testen
mvn package

# Anwendung starten
mvn spring-boot:run
# oder
java -jar target/risiko-simulation-0.0.1-SNAPSHOT.jar
```

Die API ist dann unter `http://localhost:8080` erreichbar.

---

## Beispiel-Request

```bash
curl -X POST http://localhost:8080/api/v1/simulations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Demo",
    "discountRate": 0.08,
    "seed": 42,
    "cashFlows": [
      { "year": 0, "fixedAmount": -1000 },
      { "year": 1, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } },
      { "year": 2, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } }
    ]
  }'
```

---

## Mathematische Grundlagen

### Kapitalwert (NPV)
$$NPV = \sum_{t=0}^{T} \frac{CF_t}{(1+r)^t}$$

mit Abzinsungssatz $r$ und Zahlungsstrom $CF_t$.

### Monte-Carlo
Für jede der $N$ Iterationen werden alle stochastischen $CF_t$ einmal
realisiert; der resultierende NPV bildet eine Stichprobe der NPV-Verteilung.

### Value-at-Risk
$VaR_\alpha = \max(0, -Q_{1-\alpha})$, wobei $Q_p$ das $p$-Perzentil der
NPV-Verteilung ist.

### Conditional VaR (Expected Shortfall)
$$CVaR_\alpha = \mathbb{E}[\,-NPV \mid NPV \le Q_{1-\alpha}\,]$$

### Sensitivitätsanalyse (Tornado)
Für jeden stochastischen Input $i$ wird der NPV bei Minimum und Maximum von
$i$ (alle anderen beim Erwartungswert) berechnet. Die Spannweite
$|NPV_{max} - NPV_{min}|$ misst den Beitrag von $i$ zur Gesamtunsicherheit.

---

## Tests

Das Projekt enthält über 40 Unit- und Integrationstests auf allen Ebenen:

- **Domain:** Verteilungs-Eigenschaften, Validierung, Diskontierung
- **Engine:** NPV, Statistik-Perzentile, Monte-Carlo-Reproduzierbarkeit, VaR/CVaR, Sensitivität
- **Service:** vollständige Analyse, Plausibilität, Fehlerfälle
- **Web:** `@WebMvcTest` für Controller (Validierung, Fehlerantworten)
- **Architektur:** `ArchitectureTest` erzwingt die Layering-Regeln

```bash
mvn test          # nur Tests
mvn verify        # Tests + JaCoCo-Coverage (target/site/jacoco/index.html)
```

---

## Tech-Stack

| Bereich | Technologie |
|---------|------------|
| Sprache | Java 25 |
| Framework | Spring Boot 4.1 (WebMVC, Validation, Actuator) |
| API-Doku | springdoc-openapi / Swagger UI 2.8 |
| Build | Maven 3.9 |
| Tests | JUnit 5, AssertJ-Stil, MockMvc, ArchUnit |
| Coverage | JaCoCo |
| CI | GitHub Actions (JDK 25) |
| Lizenz | MIT |

---

## Status

✅ Abgeschlossenes Portfolio-Projekt. Die Engine ist voll funktionsfähig,
getestet und über die REST-API nutzbar.
