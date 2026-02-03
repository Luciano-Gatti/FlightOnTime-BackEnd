package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Registro AirportApiLocation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportApiLocation(Double lat, Double lon) {
}
