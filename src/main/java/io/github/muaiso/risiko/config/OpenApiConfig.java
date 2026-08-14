package io.github.muaiso.risiko.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguriert die OpenAPI-Metadaten, die unter {@code /swagger-ui.html}
 * sowie {@code /v3/api-docs} bereitgestellt werden.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI risikoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Risiko-Simulation API")
                .description("Monte-Carlo Risiko-Simulation fuer Investitionsentscheidungen: "
                        + "NPV-Verteilung, Value-at-Risk, Conditional VaR und Sensitivitaetsanalyse.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Muaiso")
                        .url("https://github.com/Muaiso/risiko-simulation"))
                .license(new io.swagger.v3.oas.models.info.License()
                        .name("MIT")));
    }
}
