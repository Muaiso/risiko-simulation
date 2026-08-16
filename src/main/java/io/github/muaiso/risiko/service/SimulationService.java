package io.github.muaiso.risiko.service;

import io.github.muaiso.risiko.domain.InvestmentProject;
import io.github.muaiso.risiko.domain.RandomSource;
import io.github.muaiso.risiko.domain.StandardNormalRandomSource;
import io.github.muaiso.risiko.engine.DescriptiveStatistics;
import io.github.muaiso.risiko.engine.MonteCarloSimulator;
import io.github.muaiso.risiko.engine.Moments;
import io.github.muaiso.risiko.engine.NetPresentValueCalculator;
import io.github.muaiso.risiko.engine.PaybackAnalyzer;
import io.github.muaiso.risiko.engine.RiskMetrics;
import io.github.muaiso.risiko.engine.ScenarioAnalyzer;
import io.github.muaiso.risiko.engine.SensitivityAnalyzer;
import io.github.muaiso.risiko.engine.SensitivityAnalyzer.SensitivityResult;
import io.github.muaiso.risiko.exception.InvalidProjectException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestriert eine vollstaendige Risikoanalyse eines Investitionsprojekts.
 *
 * <p>Kombiniert deterministischen Erwartungs-NPV, Monte-Carlo-Verteilung,
 * deskriptive Statistik, Risikokennzahlen (VaR/CVaR) und
 * Sensitivitaetsanalyse zu einem Gesamtergebnis. Die Klasse ist
 * zustandslos und thread-sicher.</p>
 */
@Service
public class SimulationService {

    /**
     * Standardanzahl an Monte-Carlo-Iterationen bei nicht naherer Angabe.
     */
    public static final int DEFAULT_ITERATIONS = 50_000;

    /**
     * Fuehrt die Analyse mit den Standard-Iterationen und einem Zufalls-Seed aus.
     *
     * @param project das zu analysierende Projekt
     * @return vollstaendiges Analyseergebnis
     */
    public SimulationResult analyze(InvestmentProject project) {
        return analyze(project, DEFAULT_ITERATIONS, System.nanoTime());
    }

    /**
     * Fuehrt die Analyse mit expliziter Iterationszahl und Seed aus.
     *
     * @param project     das zu analysierende Projekt
     * @param iterations  Anzahl der Monte-Carlo-Laeufe
     * @param seed        Seed des Pseudozufallsgenerators (Reproduzierbarkeit)
     * @return vollstaendiges Analyseergebnis
     */
    public SimulationResult analyze(InvestmentProject project, int iterations, long seed) {
        ProjectValidator.validate(project, iterations);

        double expectedNpv = NetPresentValueCalculator.expectedNetPresentValue(project);

        RandomSource rng = new StandardNormalRandomSource(
                new java.util.SplittableRandom(seed));
        MonteCarloSimulator simulator = new MonteCarloSimulator(project, rng);
        double[] npvDistribution = simulator.simulate(iterations);

        DescriptiveStatistics stats = new DescriptiveStatistics(npvDistribution);

        double var95 = RiskMetrics.valueAtRisk(npvDistribution, 95.0);
        double cvar95 = RiskMetrics.conditionalValueAtRisk(npvDistribution, 95.0);
        double var99 = RiskMetrics.valueAtRisk(npvDistribution, 99.0);
        double probLoss = RiskMetrics.probabilityOfLoss(npvDistribution);
        double sharpe = RiskMetrics.sharpeRatio(npvDistribution, 0.0);
        double sortino = RiskMetrics.sortinoRatio(npvDistribution, 0.0);
        double coeffVar = RiskMetrics.coefficientOfVariation(npvDistribution);
        double skew = Moments.skewness(npvDistribution);
        double kurtosis = Moments.excessKurtosis(npvDistribution);

        List<SensitivityResult> sensitivity = SensitivityAnalyzer.analyze(project);

        // Service-eigene Sensitivitaets-Sicht (vermeidet Web->Engine-Abhaengigkeit)
        List<SensitivityView> sensitivityView = sensitivity.stream()
                .map(r -> new SensitivityView(r.cashFlowIndex(), r.year(),
                        r.npvAtMinimum(), r.npvAtMaximum(), r.range()))
                .toList();

        return new SimulationResult(
                project.name(),
                iterations,
                expectedNpv,
                stats.getMean(),
                stats.getStandardDeviation(),
                stats.getMin(),
                stats.getMax(),
                stats.percentile(5.0),
                stats.percentile(50.0),
                stats.percentile(95.0),
                var95,
                cvar95,
                var99,
                probLoss,
                sharpe,
                sortino,
                coeffVar,
                skew,
                kurtosis,
                sensitivityView);
    }

    /**
     * Aggregiertes Ergebnis einer Risikoanalyse. Immutable.
     *
     * @param projectName          Bezeichnung des Projekts
     * @param iterations           tatsaechlich durchgefuehrte Laeufe
     * @param expectedNpv          deterministischer Erwartungs-NPV (Mean-Case)
     * @param meanNpv              Mittelwert der simulierten NPV-Verteilung
     * @param standardDeviation    Standardabweichung der NPV-Verteilung
     * @param minNpv               schlechtester simulierter NPV
     * @param maxNpv               bester simulierter NPV
     * @param percentile5          unteres Perzentil (Pessimist-Case)
     * @param percentile50         Median (Base-Case)
     * @param percentile95         oberes Perzentil (Optimist-Case)
     * @param valueAtRisk95        95 %-VaR als Verlustgroesse
     * @param conditionalVaR95     95 %-CVaR (Expected Shortfall)
     * @param valueAtRisk99        99 %-VaR als Verlustgroesse
     * @param probabilityOfLoss    Anteil negativer Realisierungen [0,1]
     * @param sharpeRatio          risiko-adjustierte Rendite (Gesamtrisiko)
     * @param sortinoRatio         risiko-adjustierte Rendite (Downside-Risiko)
     * @param coefficientOfVariation  normierte Streuung (Stdabw/Mittel)
     * @param skewness             Schiefe der NPV-Verteilung
     * @param excessKurtosis       Exzess-Wölbung der NPV-Verteilung
     * @param sensitivity          Sensitivitaets-/Tornado-Ergebnisse
     */
    public record SimulationResult(
            String projectName,
            int iterations,
            double expectedNpv,
            double meanNpv,
            double standardDeviation,
            double minNpv,
            double maxNpv,
            double percentile5,
            double percentile50,
            double percentile95,
            double valueAtRisk95,
            double conditionalVaR95,
            double valueAtRisk99,
            double probabilityOfLoss,
            double sharpeRatio,
            double sortinoRatio,
            double coefficientOfVariation,
            double skewness,
            double excessKurtosis,
            List<SensitivityView> sensitivity) {
    }

    /**
     * Service-eigene, von der Engine entkoppelte Darstellung eines
     * Sensitivitaets-Ergebnisses (Tornado). Vermeidet, dass die Web-Schicht
     * direkt Engine-Typen referenzieren muss.
     *
     * @param cashFlowIndex Position im Cashflow-Array
     * @param year          Zeitpunkt des Zahlungsstroms
     * @param npvAtMinimum  NPV wenn Input auf Minimum gesetzt
     * @param npvAtMaximum  NPV wenn Input auf Maximum gesetzt
     * @param range         Spannweite (Treiberstaerke)
     */
    public record SensitivityView(
            int cashFlowIndex, int year, double npvAtMinimum, double npvAtMaximum, double range) {
    }

    /**
     * Diskontierte Amortisationsanalyse eines Projekts (Base-Case mit
     * Erwartungswerten der stochastischen Cashflows).
     *
     * @param project das zu analysierende Projekt
     * @return Ergebnis der Amortisationsanalyse
     */
    public PaybackAnalyzer.PaybackResult analyzePayback(InvestmentProject project) {
        ProjectValidator.validateProjectOnly(project);
        return PaybackAnalyzer.analyze(project);
    }

    /**
     * Szenario-Analyse (Best/Base/Worst-Case) eines Projekts.
     *
     * @param project das zu analysierende Projekt
     * @return Ergebnis der Szenario-Analyse
     */
    public ScenarioAnalyzer.ScenarioResult analyzeScenario(InvestmentProject project) {
        ProjectValidator.validateProjectOnly(project);
        return ScenarioAnalyzer.analyze(project);
    }
}
