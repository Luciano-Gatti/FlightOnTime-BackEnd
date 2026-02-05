package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.domain.ports.out.WeatherFallbackProviderPort;
import com.flightontime.app_predictor.infrastructure.out.dto.WeatherApiFallbackResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Cliente de proveedor fallback de clima.
 */
@Component
public class WeatherApiFallbackProviderClient implements WeatherFallbackProviderPort {
    private final WeatherFallbackClient weatherFallbackClient;

    public WeatherApiFallbackProviderClient(WeatherFallbackClient weatherFallbackClient) {
        this.weatherFallbackClient = weatherFallbackClient;
    }

    @Override
    public AirportWeatherDTO fetchCurrentWeather(double lat, double lon) {
        WeatherApiFallbackResponse response = weatherFallbackClient.getWeatherForCoordinates(lat, lon);
        return mapToDto(Objects.requireNonNull(response, "Fallback weather response is required"));
    }

    private AirportWeatherDTO mapToDto(WeatherApiFallbackResponse response) {
        WeatherApiFallbackResponse.Current current = Objects.requireNonNull(response.current(), "Fallback current weather is required");
        OffsetDateTime updatedAt = parseWeatherApiTimestamp(current.lastUpdated(),
                response.location() != null ? response.location().localtime() : null);
        double temperature = Objects.requireNonNull(current.temperatureCelsius(), "Fallback temperature is required");
        double windSpeed = Objects.requireNonNull(current.windSpeedKmh(), "Fallback wind speed is required");
        double visibilityKm = current.visibilityKm() != null ? current.visibilityKm() : 0.0;
        boolean precipitationFlag = current.precipitationMm() != null && current.precipitationMm() > 0;
        return new AirportWeatherDTO(temperature, windSpeed, visibilityKm, precipitationFlag, updatedAt);
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
}
