package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.domain.ports.out.WeatherPrimaryProviderPort;
import com.flightontime.app_predictor.infrastructure.out.dto.OpenMeteoWeatherResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Cliente de proveedor primario OpenMeteo.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "false", matchIfMissing = true)
public class OpenMeteoClient implements WeatherPrimaryProviderPort {
    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);
    private final WebClient openMeteoWeatherWebClient;

    public OpenMeteoClient(@Qualifier("openMeteoWeatherWebClient") WebClient openMeteoWeatherWebClient) {
        this.openMeteoWeatherWebClient = openMeteoWeatherWebClient;
    }

    @Override
    public AirportWeatherDTO fetchCurrentWeather(double lat, double lon) {
        OpenMeteoWeatherResponse response;
        try {
            response = openMeteoWeatherWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", lat)
                            .queryParam("longitude", lon)
                            .queryParam("current", "temperature_2m,wind_speed_10m,visibility,precipitation")
                            .queryParam("timezone", "UTC")
                            .build())
                    .retrieve()
                    .bodyToMono(OpenMeteoWeatherResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            ExternalProviderException providerException = new ExternalProviderException(
                    "open-meteo-api",
                    ex.getStatusCode().value(),
                    "OpenMeteo weather provider error for coordinates " + lat + "," + lon,
                    ex.getResponseBodyAsString(),
                    ex
            );
            log.error("OpenMeteo weather provider error provider={} coordinates={},{} status={} body={}",
                    providerException.getProvider(),
                    lat,
                    lon,
                    providerException.getStatusCode(),
                    providerException.getBodyTruncated());
            throw providerException;
        }
        return mapToDto(Objects.requireNonNull(response, "OpenMeteo response is required"));
    }

    private AirportWeatherDTO mapToDto(OpenMeteoWeatherResponse response) {
        OpenMeteoWeatherResponse.Current current = Objects.requireNonNull(response.current(), "OpenMeteo current weather is required");
        OffsetDateTime updatedAt = parseOpenMeteoTimestamp(current.time());
        double temperature = Objects.requireNonNull(current.temperature(), "OpenMeteo temperature is required");
        double windSpeed = Objects.requireNonNull(current.windSpeed(), "OpenMeteo wind speed is required");
        double visibilityKm = current.visibilityMeters() != null ? current.visibilityMeters() / 1000.0 : 0.0;
        boolean precipitationFlag = current.precipitation() != null && current.precipitation() > 0;
        return new AirportWeatherDTO(temperature, windSpeed, visibilityKm, precipitationFlag, updatedAt);
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
}
