package com.flightontime.app_predictor.infrastructure.in.web;

import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("/predict")
@Validated
public class PredictController {
    private final PredictFlightUseCase predictFlightUseCase;

    public PredictController(PredictFlightUseCase predictFlightUseCase) {
        this.predictFlightUseCase = predictFlightUseCase;
    }

    @PostMapping
    public ResponseEntity<PredictResponseDTO> predict(@Valid @RequestBody PredictRequestDTO request) {
        return ResponseEntity.ok(predictFlightUseCase.predict(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleModelError(WebClientResponseException ex) {
        return ResponseEntity.status(503).body(new ErrorResponse("Model service unavailable"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
        return ResponseEntity.status(503).body(new ErrorResponse("Model service unavailable"));
    }

    public record ErrorResponse(String message) {
    }
}
