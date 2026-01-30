package com.flightontime.app_predictor.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient modelWebClient(@Value("${model.service.url}") String modelServiceUrl) {
        return WebClient.builder()
                .baseUrl(modelServiceUrl)
                .build();
    }
}
