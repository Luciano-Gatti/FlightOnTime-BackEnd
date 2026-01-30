package com.flightontime.app_predictor.infrastructure.in.web;

import com.flightontime.app_predictor.application.dto.AirportDTO;
import com.flightontime.app_predictor.application.services.AirportNotFoundException;
import com.flightontime.app_predictor.application.services.AirportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/airports")
public class AirportController {
    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping("/{iata}")
    public ResponseEntity<AirportDTO> getAirport(@PathVariable String iata) {
        return ResponseEntity.ok(airportService.getAirportByIata(iata));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AirportNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }

    public record ErrorResponse(String message) {
    }
}
