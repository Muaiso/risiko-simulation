package io.github.muaiso.risiko.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.github.muaiso.risiko.exception.GlobalExceptionHandler;
import io.github.muaiso.risiko.service.SimulationService;

/**
 * Web-Layer-Test der neuen GET-Endpunkte fuer Amortisation und Szenarien.
 */
class SimulationControllerExtraEndpointsTest {

    private MockMvc mockMvc;

    private static final String BODY = """
            {
              "name": "Extra",
              "discountRate": 0.08,
              "iterations": 2000,
              "seed": 555,
              "cashFlows": [
                { "year": 0, "fixedAmount": -1000 },
                { "year": 1, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } },
                { "year": 2, "distribution": { "type": "normal", "mu": 600, "sigma": 50 } }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SimulationController(new SimulationService()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void paybackEndpointReturnsRecovery() throws Exception {
        mockMvc.perform(get("/api/v1/simulations/payback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recovered").isBoolean())
                .andExpect(jsonPath("$.paybackYear").isNumber())
                .andExpect(jsonPath("$.finalNpv").isNumber());
    }

    @Test
    void scenariosEndpointReturnsThreeCases() throws Exception {
        mockMvc.perform(get("/api/v1/simulations/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestCaseNpv").isNumber())
                .andExpect(jsonPath("$.baseCaseNpv").isNumber())
                .andExpect(jsonPath("$.worstCaseNpv").isNumber());
    }

    @Test
    void postSimulationStillWorks() throws Exception {
        // Regression: Hauptendpunkt unveraendert erreichbar
        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Extra"));
    }
}
