package com.flightspredictor.flights.infra.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI flightsOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Flights API").version("v1"))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }

    @Bean
    public OpenApiCustomizer securityRequirementsCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            SecurityRequirement requirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
            openApi.getPaths().forEach((path, pathItem) -> {
                boolean requiresAuth = requiresAuthentication(path);
                pathItem.readOperations().forEach(operation -> {
                    if (requiresAuth) {
                        operation.addSecurityItem(requirement);
                    } else if ("/predict".equals(path) && operation.getDescription() == null) {
                        operation.setDescription("Endpoint público. Authorization Bearer es opcional.");
                    }
                });
            });
        };
    }

    private boolean requiresAuthentication(String path) {
        List<String> protectedPrefixes = List.of("/history", "/subscriptions", "/admin");
        return protectedPrefixes.stream().anyMatch(path::startsWith);
    }
}
