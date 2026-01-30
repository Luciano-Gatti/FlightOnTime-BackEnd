package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.domain.ports.out.WeatherPort;
import com.flightontime.app_predictor.infrastructure.out.dto.WeatherApiResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WeatherApiClient implements WeatherPort {
    private final WebClient weatherWebClient;
    private final WeatherFallbackClient weatherFallbackClient;
    private final Duration cacheTtl;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherApiClient(
            WebClient weatherWebClient,
            WeatherFallbackClient weatherFallbackClient,
            @Value("${weather.cache.ttl}") Duration cacheTtl
    ) {
        this.weatherWebClient = weatherWebClient;
        this.weatherFallbackClient = weatherFallbackClient;
        this.cacheTtl = cacheTtl;
    }

    @Override
    public AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc) {
        String normalizedIata = iata.toUpperCase(Locale.ROOT);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CacheEntry cached = cache.get(normalizedIata);
        if (cached != null && now.isBefore(cached.expiresAt())) {
            return cached.weather();
        }

        try {
            AirportWeatherDTO dto = fetchPrimary(normalizedIata, instantUtc);
            cache.put(normalizedIata, new CacheEntry(dto, now.plus(cacheTtl)));
            return dto;
        } catch (RuntimeException primaryError) {
            try {
                AirportWeatherDTO dto = fetchFallback(normalizedIata);
                cache.put(normalizedIata, new CacheEntry(dto, now.plus(cacheTtl)));
                return dto;
            } catch (RuntimeException fallbackError) {
                WeatherProviderException providerException = new WeatherProviderException(
                        "Weather provider error for IATA " + normalizedIata,
                        primaryError
                );
                providerException.addSuppressed(fallbackError);
                throw providerException;
            }
        }
    }

    private AirportWeatherDTO fetchPrimary(String normalizedIata, OffsetDateTime instantUtc) {
        OffsetDateTime requestInstant = instantUtc.withOffsetSameInstant(ZoneOffset.UTC);
        WeatherApiResponse response = weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("iata", normalizedIata)
                        .queryParam("instantUtc", requestInstant.toString())
                        .build())
                .retrieve()
                .bodyToMono(WeatherApiResponse.class)
                .block();
        WeatherApiResponse safeResponse = Objects.requireNonNull(response, "Weather response is required");
        return mapToDto(safeResponse);
    }

    private AirportWeatherDTO fetchFallback(String normalizedIata) {
        WeatherApiResponse response = weatherFallbackClient.getWeatherForIata(normalizedIata);
        WeatherApiResponse safeResponse = Objects.requireNonNull(response, "Fallback weather response is required");
        return mapToDto(safeResponse);
    }

    private AirportWeatherDTO mapToDto(WeatherApiResponse response) {
        OffsetDateTime updatedAt = response.updatedAt() == null
                ? null
                : response.updatedAt().withOffsetSameInstant(ZoneOffset.UTC);
        return new AirportWeatherDTO(
                response.temp(),
                response.windSpeed(),
                response.visibility(),
                response.precipitationFlag(),
                updatedAt
        );
    }

    private record CacheEntry(AirportWeatherDTO weather, OffsetDateTime expiresAt) {
    }
}
