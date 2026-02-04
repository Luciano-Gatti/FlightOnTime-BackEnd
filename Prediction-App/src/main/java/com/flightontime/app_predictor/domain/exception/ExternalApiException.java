package com.flightontime.app_predictor.domain.exception;

/**
 * Excepción para errores al consumir APIs externas desde el dominio.
 */
public class ExternalApiException extends DomainException {

    /**
     * Ejecuta la operación external api exception.
     * @param message variable de entrada message.
     * @param cause variable de entrada cause.
     */

    /**
     * Ejecuta la operación external api exception.
     * @param message variable de entrada message.
     * @param cause variable de entrada cause.
     * @return resultado de la operación external api exception.
     */

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
