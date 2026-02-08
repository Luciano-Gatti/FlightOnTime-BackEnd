package com.flightspredictor.flights.infra.security;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
