package com.flightontime.app_predictor.domain.exception;

/**
 * Excepción base del dominio.
 *
 * Representa errores derivados de reglas del negocio, sin depender
 * de frameworks o detalles de transporte.
 */
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String message) {
        super(message);
        this.code = null;
    }

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
    }

    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
