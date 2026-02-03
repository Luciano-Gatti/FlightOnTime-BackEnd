package com.flightontime.app_predictor.domain.exception;

/**
 * Excepción para errores al consumir APIs externas desde el dominio.
 */
public class ExternalApiException extends DomainException {

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
