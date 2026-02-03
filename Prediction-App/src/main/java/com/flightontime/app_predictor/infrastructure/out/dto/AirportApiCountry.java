package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Registro AirportApiCountry.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportApiCountry(String name) {
}
