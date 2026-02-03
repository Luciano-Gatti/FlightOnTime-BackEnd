package com.flightontime.app_predictor.application.services;

/**
 * Clase AirportNotFoundException.
 */
public class AirportNotFoundException extends RuntimeException {
    public AirportNotFoundException(String message) {
        super(message);
    }
}
