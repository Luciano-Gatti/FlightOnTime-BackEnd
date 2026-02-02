package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.infrastructure.out.dto.WeatherApiResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WeatherFallbackClient {
    private final WebClient fallbackWebClient;
    private final String apiKey;
    private final String baseUrl;
    private final Duration readTimeout;

    public WeatherFallbackClient(
            WebClient fallbackWebClient,
            @Value("${weather.fallback.api-key:}") String apiKey,
            @Value("${weather.fallback.base-url}") String baseUrl,
            @Value("${weather.fallback.timeout.read}") Duration readTimeout
    ) {
        this.fallbackWebClient = fallbackWebClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.readTimeout = readTimeout;
    }

    public WeatherApiResponse getWeatherForIata(String iata) {
        ensureApiKey();
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("q", iata)
                .toUriString();
        try {
            return fallbackWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(WeatherApiResponse.class)
                    .timeout(readTimeout)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Fallback weather provider error for IATA " + iata, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Fallback weather provider connection error for IATA " + iata, ex);
        }
    }

    private void ensureApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("weather.fallback.api-key is not configured.");
        }
    }
}
