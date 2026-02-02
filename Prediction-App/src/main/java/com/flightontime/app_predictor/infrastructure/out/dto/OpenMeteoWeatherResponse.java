package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoWeatherResponse(Current current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            String time,
            @JsonProperty("temperature_2m") Double temperature,
            @JsonProperty("wind_speed_10m") Double windSpeed,
            @JsonProperty("visibility") Double visibilityMeters,
            @JsonProperty("precipitation") Double precipitation
    ) {
    }
}
