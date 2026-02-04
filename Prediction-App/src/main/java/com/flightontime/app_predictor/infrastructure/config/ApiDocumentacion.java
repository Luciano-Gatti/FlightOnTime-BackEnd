package com.flightontime.app_predictor.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase ApiDocumentacion.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FlightOnTime API",
                description = "Sistema FlightOnTime para predicción de vuelos. Endpoints públicos: auth y consulta de salud.",
                version = "v1"
        ),
        tags = {
                @Tag(name = "Predict", description = "Operaciones de predicción de vuelos"),
                @Tag(name = "Airports", description = "Consulta de aeropuertos"),
                @Tag(name = "Auth", description = "Operaciones de autenticación"),
                @Tag(name = "Stats", description = "Resumen y métricas de predicción"),
                @Tag(name = "Admin", description = "Operaciones administrativas")
        }
)
/**
 * Class ApiDocumentacion.
 *
 * <p>Responsable de api documentacion.</p>
 */
public class ApiDocumentacion {

    /**
     * Ejecuta la operación custom open api.
     * @return resultado de la operación custom open api.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("FlightOnTime API")
                        .description("API de predicción de vuelos. Endpoints públicos: auth y consulta de salud.")
                        .version("v1")
                        .contact(new Contact()
                                .name("FlightOnTime Team")
                                .email("soporte@flightontime.local")))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local"))
                .addServersItem(new Server()
                        .url("https://dev.flightontime.local")
                        .description("Dev"))
                .components(new Components().addSecuritySchemes("bearer-key",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"));
    }
}
