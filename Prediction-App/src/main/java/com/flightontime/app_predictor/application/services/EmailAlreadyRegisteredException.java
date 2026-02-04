package com.flightontime.app_predictor.application.services;

/**
 * Clase EmailAlreadyRegisteredException.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {
    /**
     * Ejecuta la operación email already registered exception.
     * @param message variable de entrada message.
     */
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
