package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.WeatherPort;
import com.flightontime.app_predictor.infrastructure.out.dto.OpenMeteoWeatherResponse;
import com.flightontime.app_predictor.infrastructure.out.dto.WeatherApiFallbackResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Clase WeatherApiClient.
 */
@Component
public class WeatherApiClient implements WeatherPort {
    private static final Logger log = LoggerFactory.getLogger(WeatherApiClient.class);
    private final WebClient openMeteoWeatherWebClient;
    private final WeatherFallbackClient weatherFallbackClient;
    private final AirportRepositoryPort airportRepositoryPort;
    private final AirportInfoPort airportInfoPort;
    private final Duration cacheTtl;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherApiClient(
            @Qualifier("openMeteoWeatherWebClient") WebClient openMeteoWeatherWebClient,
            WeatherFallbackClient weatherFallbackClient,
            AirportRepositoryPort airportRepositoryPort,
            AirportInfoPort airportInfoPort,
            @Value("${weather.cache.ttl}") Duration cacheTtl,
            ObjectMapper objectMapper
    ) {
        this.openMeteoWeatherWebClient = openMeteoWeatherWebClient;
        this.weatherFallbackClient = weatherFallbackClient;
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
        this.cacheTtl = cacheTtl;
        this.objectMapper = objectMapper;
    }

    @Override
    public AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc) {
        String normalizedIata = iata.toUpperCase(Locale.ROOT);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        // Usa cache temporal para evitar llamadas repetidas a proveedores externos.
        CacheEntry cached = cache.get(normalizedIata);
        if (cached != null && now.isBefore(cached.expiresAt())) {
            log.info("Weather cache hit for iata={}", normalizedIata);
            return cached.weather();
        }
        log.info("Weather lookup start iata={} instantUtc={}", normalizedIata, instantUtc);
        Airport airport = resolveAirport(normalizedIata);

        try {
            AirportWeatherDTO dto = fetchPrimary(airport);
            cache.put(normalizedIata, new CacheEntry(dto, now.plus(cacheTtl)));
            return dto;
        } catch (RuntimeException primaryError) {
            log.warn("Primary weather provider failed iata={} message={}", normalizedIata, primaryError.getMessage());
            try {
                AirportWeatherDTO dto = fetchFallback(airport);
                cache.put(normalizedIata, new CacheEntry(dto, now.plus(cacheTtl)));
                return dto;
            } catch (RuntimeException fallbackError) {
                log.error("Fallback weather provider failed iata={} message={}", normalizedIata, fallbackError.getMessage());
                WeatherProviderException providerException = new WeatherProviderException(
                        "Weather provider error for IATA " + normalizedIata,
                        primaryError
                );
                providerException.addSuppressed(fallbackError);
                throw providerException;
            }
        }
    }

    private AirportWeatherDTO fetchPrimary(Airport airport) {
        log.info("Calling OpenMeteo weather for iata={} lat={} lon={}",
                airport.airportIata(), airport.latitude(), airport.longitude());
        OpenMeteoWeatherResponse response = openMeteoWeatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", airport.latitude())
                        .queryParam("longitude", airport.longitude())
                        .queryParam("current", "temperature_2m,wind_speed_10m,visibility,precipitation")
                        .queryParam("timezone", "UTC")
                        .build())
                .retrieve()
                .bodyToMono(OpenMeteoWeatherResponse.class)
                .block();
        OpenMeteoWeatherResponse safeResponse = Objects.requireNonNull(response, "OpenMeteo response is required");
        logJson("OpenMeteo response payload", safeResponse);
        return mapToDto(safeResponse);
    }

    private AirportWeatherDTO fetchFallback(Airport airport) {
        log.info("Calling fallback weather for iata={} lat={} lon={}",
                airport.airportIata(), airport.latitude(), airport.longitude());
        WeatherApiFallbackResponse response = weatherFallbackClient.getWeatherForCoordinates(
                airport.latitude(),
                airport.longitude()
        );
        WeatherApiFallbackResponse safeResponse = Objects.requireNonNull(response, "Fallback weather response is required");
        logJson("Fallback weather response payload", safeResponse);
        return mapToDto(safeResponse);
    }

    private AirportWeatherDTO mapToDto(OpenMeteoWeatherResponse response) {
        OpenMeteoWeatherResponse.Current current = Objects.requireNonNull(response.current(), "OpenMeteo current weather is required");
        OffsetDateTime updatedAt = parseOpenMeteoTimestamp(current.time());
        double temperature = Objects.requireNonNull(current.temperature(), "OpenMeteo temperature is required");
        double windSpeed = Objects.requireNonNull(current.windSpeed(), "OpenMeteo wind speed is required");
        double visibilityKm = current.visibilityMeters() != null ? current.visibilityMeters() / 1000.0 : 0.0;
        boolean precipitationFlag = current.precipitation() != null && current.precipitation() > 0;
        return new AirportWeatherDTO(
                temperature,
                windSpeed,
                visibilityKm,
                precipitationFlag,
                updatedAt
        );
    }

    private AirportWeatherDTO mapToDto(WeatherApiFallbackResponse response) {
        WeatherApiFallbackResponse.Current current = Objects.requireNonNull(response.current(), "Fallback current weather is required");
        OffsetDateTime updatedAt = parseWeatherApiTimestamp(current.lastUpdated(),
                response.location() != null ? response.location().localtime() : null);
        double temperature = Objects.requireNonNull(current.temperatureCelsius(), "Fallback temperature is required");
        double windSpeed = Objects.requireNonNull(current.windSpeedKmh(), "Fallback wind speed is required");
        double visibilityKm = current.visibilityKm() != null ? current.visibilityKm() : 0.0;
        boolean precipitationFlag = current.precipitationMm() != null && current.precipitationMm() > 0;
        return new AirportWeatherDTO(
                temperature,
                windSpeed,
                visibilityKm,
                precipitationFlag,
                updatedAt
        );
    }

    private Airport resolveAirport(String normalizedIata) {
        Airport airport = airportRepositoryPort.findByIata(normalizedIata)
                .orElseGet(() -> airportInfoPort.findByIata(normalizedIata)
                        .map(this::storeAirport)
                        .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + normalizedIata)));
        if (airport.latitude() == null || airport.longitude() == null) {
            throw new IllegalArgumentException("Airport coordinates are required for weather data: " + normalizedIata);
        }
        return airport;
    }

    private Airport storeAirport(Airport airport) {
        log.info("Storing airport from external source for weather iata={}", airport.airportIata());
        airportRepositoryPort.saveAll(List.of(airport));
        return airport;
    }

    private void logJson(String message, Object payload) {
        try {
            log.info("{}: {}", message, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.warn("{}: <failed to serialize payload>", message, ex);
        }
    }

    private OffsetDateTime parseOpenMeteoTimestamp(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            LocalDateTime localTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return localTime.atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private OffsetDateTime parseWeatherApiTimestamp(String lastUpdated, String localtime) {
        String timeValue = lastUpdated != null && !lastUpdated.isBlank() ? lastUpdated : localtime;
        if (timeValue == null || timeValue.isBlank()) {
            return null;
        }
        try {
            LocalDateTime localTime = LocalDateTime.parse(timeValue, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return localTime.atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private record CacheEntry(AirportWeatherDTO weather, OffsetDateTime expiresAt) {
    }
}
