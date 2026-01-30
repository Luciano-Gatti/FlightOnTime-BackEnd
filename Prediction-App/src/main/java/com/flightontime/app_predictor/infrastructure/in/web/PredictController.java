package com.flightontime.app_predictor.infrastructure.in.web;

import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictHistoryUseCase;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryDetailDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryItemDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final PredictHistoryUseCase predictHistoryUseCase;

    public PredictController(PredictFlightUseCase predictFlightUseCase, PredictHistoryUseCase predictHistoryUseCase) {
        this.predictFlightUseCase = predictFlightUseCase;
        this.predictHistoryUseCase = predictHistoryUseCase;
    }

    @PostMapping
    public ResponseEntity<PredictResponseDTO> predict(@Valid @RequestBody PredictRequestDTO request) {
        Long userId = resolveUserId();
        return ResponseEntity.ok(predictFlightUseCase.predict(request, userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PredictHistoryItemDTO>> getHistory() {
        Long userId = resolveUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(predictHistoryUseCase.getHistory(userId));
    }

    @GetMapping("/history/{requestId}")
    public ResponseEntity<PredictHistoryDetailDTO> getHistoryDetail(@PathVariable Long requestId) {
        Long userId = resolveUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(predictHistoryUseCase.getHistoryDetail(userId, requestId));
    }

    @GetMapping("/{requestId}/latest")
    public ResponseEntity<PredictResponseDTO> getLatest(@PathVariable Long requestId) {
        Long userId = resolveUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(predictFlightUseCase.getLatestPrediction(requestId, userId));
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

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalString) {
            if ("anonymousUser".equalsIgnoreCase(principalString)) {
                return null;
            }
            try {
                return Long.valueOf(principalString);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    public record ErrorResponse(String message) {
    }
}
