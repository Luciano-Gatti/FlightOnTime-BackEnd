package com.flightontime.app_predictor.application.services;

/**
 * Clase AirportNotFoundException.
 */
public class AirportNotFoundException extends RuntimeException {
    /**
     * Ejecuta la operación airport not found exception.
     * @param message variable de entrada message.
     */
    /**
     * Ejecuta la operación airport not found exception.
     * @param message variable de entrada message.
     * @return resultado de la operación airport not found exception.
     */
    public AirportNotFoundException(String message) {
        super(message);
    }
}
