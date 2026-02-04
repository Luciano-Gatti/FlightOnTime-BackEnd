package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Registro AirportApiLocation.
 * @param lat variable de entrada lat.
 * @param lon variable de entrada lon.
 * @return resultado de la operación resultado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportApiLocation(Double lat, Double lon) {
}
