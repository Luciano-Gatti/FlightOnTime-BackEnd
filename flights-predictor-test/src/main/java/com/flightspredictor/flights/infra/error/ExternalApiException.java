package com.flightspredictor.flights.infra.error;

public class ExternalApiException extends RuntimeException{

    public ExternalApiException (String message) {
        super(message);
    }
}
