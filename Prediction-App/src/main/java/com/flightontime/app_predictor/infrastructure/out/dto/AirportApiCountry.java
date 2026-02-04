package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Registro AirportApiCountry.
 * @param name variable de entrada name.
 * @return resultado de la operación resultado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportApiCountry(String name) {
}
