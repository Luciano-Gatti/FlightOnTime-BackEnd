package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.application.services.EmailAlreadyRegisteredException;
import com.flightontime.app_predictor.application.services.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Clase AuthExceptionHandler.
 */
@RestControllerAdvice
public class AuthExceptionHandler {
    /**
     * Ejecuta la operación handle email already registered.
     * @param ex variable de entrada ex.
     */
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
    }

    /**
     * Ejecuta la operación handle invalid credentials.
     * @param ex variable de entrada ex.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleInvalidCredentials(InvalidCredentialsException ex) {
    }
}
