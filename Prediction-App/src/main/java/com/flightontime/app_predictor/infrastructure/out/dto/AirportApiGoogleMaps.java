package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Registro AirportApiGoogleMaps.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportApiGoogleMaps(
        @JsonProperty("googleMaps")
        String googleMaps
) {
}
