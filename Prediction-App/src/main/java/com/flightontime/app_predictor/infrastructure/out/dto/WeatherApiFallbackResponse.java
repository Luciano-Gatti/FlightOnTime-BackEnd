package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registro WeatherApiFallbackResponse.
 * @param location variable de entrada location.
 * @param current variable de entrada current.
 * @return resultado de la operación resultado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiFallbackResponse(
        Location location,
        Current current
) {
/**
 * Registro Location.
 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(
            String name,
            String country,
            @JsonProperty("lat") Double latitude,
            @JsonProperty("lon") Double longitude,
            @JsonProperty("localtime") String localtime
    ) {
    }

/**
 * Registro Current.
 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            @JsonProperty("temp_c") Double temperatureCelsius,
            @JsonProperty("wind_kph") Double windSpeedKmh,
            @JsonProperty("vis_km") Double visibilityKm,
            @JsonProperty("precip_mm") Double precipitationMm,
            @JsonProperty("last_updated") String lastUpdated
    ) {
    }
}
