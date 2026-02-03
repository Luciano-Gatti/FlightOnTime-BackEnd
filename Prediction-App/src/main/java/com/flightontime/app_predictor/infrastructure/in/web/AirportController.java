package com.flightontime.app_predictor.infrastructure.in.web;

import com.flightontime.app_predictor.application.dto.AirportDTO;
import com.flightontime.app_predictor.application.services.AirportNotFoundException;
import com.flightontime.app_predictor.application.services.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase AirportController.
 */
@RestController
@RequestMapping("/airports")
@Tag(name = "Airports", description = "Consulta de aeropuertos")
@Validated
public class AirportController {
    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping("/{iata}")
    @Operation(
            summary = "Consultar aeropuerto por IATA",
            description = "Devuelve la información del aeropuerto asociado al código IATA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aeropuerto encontrado"),
            @ApiResponse(responseCode = "400", description = "Código IATA inválido"),
            @ApiResponse(responseCode = "404", description = "Aeropuerto no encontrado")
    })
    public ResponseEntity<AirportDTO> getAirport(
            @Parameter(description = "Código IATA de 3 letras", example = "EZE")
            @PathVariable String iata
    ) {
        String normalizedIata = normalizeIata(iata);
        return ResponseEntity.ok(airportService.getAirportByIata(normalizedIata));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AirportNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }

    private String normalizeIata(String iata) {
        if (iata == null) {
            throw new IllegalArgumentException("iata must be length 3");
        }
        String normalized = iata.trim().toUpperCase();
        if (!normalized.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("iata must be length 3");
        }
        return normalized;
    }

/**
 * Registro ErrorResponse.
 */
    public record ErrorResponse(String message) {
    }
}
