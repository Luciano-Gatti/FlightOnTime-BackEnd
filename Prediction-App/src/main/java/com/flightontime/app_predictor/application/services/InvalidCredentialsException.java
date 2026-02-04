package com.flightontime.app_predictor.application.services;

/**
 * Clase InvalidCredentialsException.
 */
public class InvalidCredentialsException extends RuntimeException {
    /**
     * Ejecuta la operación invalid credentials exception.
     * @param message variable de entrada message.
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
