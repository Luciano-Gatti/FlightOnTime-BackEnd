package com.flightontime.app_predictor.domain.exception;

/**
 * Excepción base del dominio.
 *
 * Representa errores derivados de reglas del negocio, sin depender
 * de frameworks o detalles de transporte.
 */
public class DomainException extends RuntimeException {

    private final String code;

    /**
     * Ejecuta la operación domain exception.
     * @param message variable de entrada message.
     */

    /**
     * Ejecuta la operación domain exception.
     * @param message variable de entrada message.
     * @return resultado de la operación domain exception.
     */

    public DomainException(String message) {
        super(message);
        this.code = null;
    }

    /**
     * Ejecuta la operación domain exception.
     * @param code variable de entrada code.
     * @param message variable de entrada message.
     */

    /**
     * Ejecuta la operación domain exception.
     * @param code variable de entrada code.
     * @param message variable de entrada message.
     * @return resultado de la operación domain exception.
     */

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Ejecuta la operación domain exception.
     * @param message variable de entrada message.
     * @param cause variable de entrada cause.
     */

    /**
     * Ejecuta la operación domain exception.
     * @param message variable de entrada message.
     * @param cause variable de entrada cause.
     * @return resultado de la operación domain exception.
     */

    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
    }

    /**
     * Ejecuta la operación domain exception.
     * @param code variable de entrada code.
     * @param message variable de entrada message.
     * @param cause variable de entrada cause.
     */

    /**
     * Ejecuta la operación domain exception.
     * @param code variable de entrada code.
     * @param message variable de entrada message.
     * @param cause variable de entrada cause.
     * @return resultado de la operación domain exception.
     */

    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Ejecuta la operación get code.
     * @return resultado de la operación get code.
     */

    public String getCode() {
        return code;
    }
}
