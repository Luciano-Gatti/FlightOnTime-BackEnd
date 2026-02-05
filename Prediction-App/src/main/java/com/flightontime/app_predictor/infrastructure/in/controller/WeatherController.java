package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.application.services.WeatherService;
import com.flightontime.app_predictor.application.exception.WeatherProviderException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase WeatherController.
 */
@RestController
@RequestMapping("/airports")
@Tag(name = "Airports", description = "Consulta de clima en aeropuertos")
public class WeatherController {
    private static final Pattern IATA_PATTERN = Pattern.compile("^[A-Za-z]{3}$");

    private final WeatherService weatherService;

    /**
     * Ejecuta la operación weather controller.
     * @param weatherService variable de entrada weatherService.
     */

    /**
     * Ejecuta la operación weather controller.
     * @param weatherService variable de entrada weatherService.
     * @return resultado de la operación weather controller.
     */

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{iata}/weather")
    @Operation(
            summary = "Consultar clima por aeropuerto",
            description = "Devuelve el clima actual del aeropuerto indicado en UTC."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clima encontrado"),
            @ApiResponse(responseCode = "400", description = "Código IATA inválido"),
            @ApiResponse(responseCode = "503", description = "Proveedor de clima no disponible")
    })
    public ResponseEntity<AirportWeatherDTO> getWeather(
            @Parameter(description = "Código IATA de 3 letras", example = "EZE")
            @PathVariable("iata") String iata
    ) {
        if (iata == null || !IATA_PATTERN.matcher(iata).matches()) {
            throw new IllegalArgumentException("IATA must be exactly 3 letters");
        }
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        return ResponseEntity.ok(weatherService.getCurrentWeather(iata, nowUtc));
    }

    /**
     * Ejecuta la operación handle validation error.
     * @param ex variable de entrada ex.
     * @return resultado de la operación handle validation error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Ejecuta la operación handle provider error.
     * @param ex variable de entrada ex.
     * @return resultado de la operación handle provider error.
     */
    @ExceptionHandler(WeatherProviderException.class)
    public ResponseEntity<ErrorResponse> handleProviderError(WeatherProviderException ex) {
        return ResponseEntity.status(503).body(new ErrorResponse("Weather provider unavailable"));
    }

/**
 * Registro ErrorResponse.
     * @param message variable de entrada message.
     * @return resultado de la operación resultado.
 */
    public record ErrorResponse(String message) {
    }
}
