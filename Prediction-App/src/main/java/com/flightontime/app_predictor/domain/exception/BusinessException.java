package com.flightontime.app_predictor.domain.exception;

/**
 * Excepción de negocio.
 *
 * Representa errores derivados de reglas del dominio,
 * no errores técnicos ni de validación.
 */
public class BusinessException extends DomainException {

    public BusinessException(String code, String message) {
        super(code, message);
    }
}
