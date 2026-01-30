package com.flightontime.app_predictor.infrastructure.in.web;

import com.flightontime.app_predictor.application.dto.AirportDTO;
import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/airports")
@Validated
public class AirportController {
    private final AirportRepositoryPort airportRepositoryPort;
    private final AirportInfoPort airportInfoPort;

    public AirportController(AirportRepositoryPort airportRepositoryPort, AirportInfoPort airportInfoPort) {
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
    }

    @GetMapping("/{iata}")
    public ResponseEntity<AirportDTO> getAirport(@PathVariable String iata) {
        String normalizedIata = normalizeIata(iata);
        Optional<Airport> storedAirport = airportRepositoryPort.findByIata(normalizedIata);
        if (storedAirport.isPresent()) {
            return ResponseEntity.ok(AirportDTO.fromDomain(storedAirport.get()));
        }
        Optional<Airport> externalAirport = airportInfoPort.findByIata(normalizedIata);
        if (externalAirport.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Airport> savedAirports = airportRepositoryPort.saveAll(List.of(externalAirport.get()));
        Airport airport = savedAirports.isEmpty() ? externalAirport.get() : savedAirports.getFirst();
        return ResponseEntity.ok(AirportDTO.fromDomain(airport));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    private String normalizeIata(String iata) {
        if (iata == null) {
            throw new IllegalArgumentException("IATA is required");
        }
        String normalized = iata.trim().toUpperCase();
        if (!normalized.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("IATA must have 3 uppercase letters");
        }
        return normalized;
    }

    public record ErrorResponse(String message) {
    }
}
