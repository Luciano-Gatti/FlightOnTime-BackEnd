package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.infrastructure.out.dto.WeatherApiFallbackResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Clase WeatherFallbackClient.
 */
@Component
public class WeatherFallbackClient {
    private final WebClient fallbackWebClient;
    private final String apiKey;
    private final String baseUrl;
    private final Duration readTimeout;

    public WeatherFallbackClient(
            @Qualifier("fallbackWeatherWebClient") WebClient fallbackWebClient,
            @Value("${weather.fallback.api-key:}") String apiKey,
            @Value("${weather.fallback.base-url}") String baseUrl,
            @Value("${weather.fallback.timeout.read}") Duration readTimeout
    ) {
        this.fallbackWebClient = fallbackWebClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.readTimeout = readTimeout;
    }

    /**
     * Ejecuta la operación get weather for iata.
     * @param iata variable de entrada iata.
     * @return resultado de la operación get weather for iata.
     */

    public WeatherApiFallbackResponse getWeatherForIata(String iata) {
        ensureApiKey();
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("q", iata)
                .toUriString();
        try {
            return fallbackWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(WeatherApiFallbackResponse.class)
                    .timeout(readTimeout)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Fallback weather provider error for IATA " + iata, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Fallback weather provider connection error for IATA " + iata, ex);
        }
    }

    /**
     * Ejecuta la operación get weather for coordinates.
     * @param latitude variable de entrada latitude.
     * @param longitude variable de entrada longitude.
     * @return resultado de la operación get weather for coordinates.
     */

    public WeatherApiFallbackResponse getWeatherForCoordinates(double latitude, double longitude) {
        ensureApiKey();
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("q", latitude + "," + longitude)
                .toUriString();
        try {
            return fallbackWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(WeatherApiFallbackResponse.class)
                    .timeout(readTimeout)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Fallback weather provider error for coordinates " + latitude + "," + longitude, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Fallback weather provider connection error for coordinates " + latitude + "," + longitude, ex);
        }
    }

    /**
     * Ejecuta la operación ensure api key.
     */

    private void ensureApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("weather.fallback.api-key is not configured.");
        }
    }
}
