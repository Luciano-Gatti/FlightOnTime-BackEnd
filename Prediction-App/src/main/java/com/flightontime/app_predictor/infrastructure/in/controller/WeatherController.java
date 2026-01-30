package com.flightontime.app_predictor.infrastructure.in.controller;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.domain.ports.out.WeatherPort;
import com.flightontime.app_predictor.infrastructure.out.adapters.WeatherProviderException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/airports")
public class WeatherController {
    private static final Pattern IATA_PATTERN = Pattern.compile("^[A-Za-z]{3}$");

    private final WeatherPort weatherPort;

    public WeatherController(WeatherPort weatherPort) {
        this.weatherPort = weatherPort;
    }

    @GetMapping("/{iata}/weather")
    public ResponseEntity<AirportWeatherDTO> getWeather(@PathVariable("iata") String iata) {
        if (iata == null || !IATA_PATTERN.matcher(iata).matches()) {
            throw new IllegalArgumentException("IATA must be exactly 3 letters");
        }
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        return ResponseEntity.ok(weatherPort.getCurrentWeather(iata, nowUtc));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(WeatherProviderException.class)
    public ResponseEntity<ErrorResponse> handleProviderError(WeatherProviderException ex) {
        return ResponseEntity.status(503).body(new ErrorResponse("Weather provider unavailable"));
    }

    public record ErrorResponse(String message) {
    }
}
