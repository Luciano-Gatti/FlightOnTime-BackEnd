package com.flightontime.app_predictor.domain.exception;

/**
 * Excepción de negocio.
 *
 * Representa errores derivados de reglas del dominio,
 * no errores técnicos ni de validación.
 */
public class BusinessException extends DomainException {

    /**
     * Ejecuta la operación business exception.
     * @param code variable de entrada code.
     * @param message variable de entrada message.
     */

    /**
     * Ejecuta la operación business exception.
     * @param code variable de entrada code.
     * @param message variable de entrada message.
     * @return resultado de la operación business exception.
     */

    public BusinessException(String code, String message) {
        super(code, message);
    }
}
