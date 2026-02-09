package com.flightspredictor.flights.infra.security;

public class UserEmailAlreadyExistsException extends RuntimeException {

    public UserEmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}
