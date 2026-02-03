package com.flightontime.app_predictor.domain.exception;

/**
 * Excepción para aeropuertos no encontrados en el dominio.
 */
public class AirportNotFoundException extends DomainException {

    public AirportNotFoundException(String message) {
        super(message);
    }
}
