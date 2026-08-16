package io.github.muaiso.risiko.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.github.muaiso.risiko.service.SimulationService;

/**
 * Web-Layer-Test des SimulationControllers ueber einen standalone
 * MockMvc (ohne vollstaendigen Spring-Kontext). Der Service wird
 * direkt injiziert; der Fokus liegt auf Routing, Mapping und
 * Validierungsfehlern.
 */
class SimulationControllerWebTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SimulationController(new SimulationService()))
                .setControllerAdvice(new io.github.muaiso.risiko.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void postSimulationReturnsOkWithMetrics() throws Exception {
        String body = """
                {
                  "name": "TestProjekt",
                  "discountRate": 0.08,
                  "iterations": 2000,
                  "seed": 12345,
                  "cashFlows": [
                    { "year": 0, "fixedAmount": -1000 },
                    { "year": 1, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } },
                    { "year": 2, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("TestProjekt"))
                .andExpect(jsonPath("$.iterations").value(2000))
                .andExpect(jsonPath("$.expectedNpv").exists())
                .andExpect(jsonPath("$.valueAtRisk95").exists())
                .andExpect(jsonPath("$.sharpeRatio").exists())
                .andExpect(jsonPath("$.sortinoRatio").exists())
                .andExpect(jsonPath("$.coefficientOfVariation").exists())
                .andExpect(jsonPath("$.skewness").exists())
                .andExpect(jsonPath("$.excessKurtosis").exists())
                .andExpect(jsonPath("$.sensitivity").isArray());
    }

    @Test
    void postSimulationReturnsExtendedMetrics() throws Exception {
        String body = """
                {
                  "name": "Erweitert",
                  "discountRate": 0.08,
                  "iterations": 4000,
                  "seed": 777,
                  "cashFlows": [
                    { "year": 0, "fixedAmount": -1000 },
                    { "year": 1, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } },
                    { "year": 2, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sharpeRatio").isNumber())
                .andExpect(jsonPath("$.sortinoRatio").isNumber())
                .andExpect(jsonPath("$.coefficientOfVariation").isNumber())
                .andExpect(jsonPath("$.skewness").isNumber())
                .andExpect(jsonPath("$.excessKurtosis").isNumber());
    }

    @Test
    void postSimulationRejectsMissingName() throws Exception {
        String body = """
                {
                  "discountRate": 0.08,
                  "cashFlows": [
                    { "year": 0, "fixedAmount": -1000 }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postSimulationRejectsUnknownDistribution() throws Exception {
        String body = """
                {
                  "name": "X",
                  "discountRate": 0.05,
                  "cashFlows": [
                    { "year": 1, "distribution": { "type": "banana", "mu": 1, "sigma": 1 } }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
