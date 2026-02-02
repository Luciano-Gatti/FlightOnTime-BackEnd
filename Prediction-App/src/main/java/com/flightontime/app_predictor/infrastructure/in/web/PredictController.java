package com.flightontime.app_predictor.infrastructure.in.web;

import com.flightontime.app_predictor.domain.ports.in.BulkPredictUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictHistoryUseCase;
import com.flightontime.app_predictor.infrastructure.in.dto.BulkPredictCsvUploadResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.BulkPredictErrorDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryDetailDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryItemDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestController
@RequestMapping("/predict")
@Tag(name = "Predicciones", description = "Endpoints para generar predicciones y consultar historiales")
@Validated
public class PredictController {
    private static final Logger log = LoggerFactory.getLogger(PredictController.class);
    private final PredictFlightUseCase predictFlightUseCase;
    private final PredictHistoryUseCase predictHistoryUseCase;
    private final BulkPredictUseCase bulkPredictUseCase;
    private final ObjectMapper objectMapper;

    public PredictController(
            PredictFlightUseCase predictFlightUseCase,
            PredictHistoryUseCase predictHistoryUseCase,
            BulkPredictUseCase bulkPredictUseCase,
            ObjectMapper objectMapper
    ) {
        this.predictFlightUseCase = predictFlightUseCase;
        this.predictHistoryUseCase = predictHistoryUseCase;
        this.bulkPredictUseCase = bulkPredictUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<PredictResponseDTO> predict(@Valid @RequestBody PredictRequestDTO request) {
        Long userId = resolveUserId();
        logJson("Prediction request received userId=" + userId, request);
        PredictResponseDTO response = predictFlightUseCase.predict(request, userId);
        logJson("Prediction response userId=" + userId, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/bulk-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<BulkPredictCsvUploadResponseDTO> bulkImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new BulkPredictCsvUploadResponseDTO(
                    0,
                    0,
                    0,
                    List.of(new BulkPredictErrorDTO(0, "CSV file is required", null))
            ));
        }
        Long userId = resolveUserId();
        try {
            var result = bulkPredictUseCase.importPredictionsFromCsv(file.getInputStream(), userId, dryRun);
            int totalRows = result.accepted() + result.rejected();
            List<BulkPredictErrorDTO> errors = result.errors().stream()
                    .map(error -> new BulkPredictErrorDTO(error.rowNumber(), error.message(), error.rawRow()))
                    .toList();
            return ResponseEntity.ok(new BulkPredictCsvUploadResponseDTO(
                    totalRows,
                    result.accepted(),
                    result.rejected(),
                    errors
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new BulkPredictCsvUploadResponseDTO(
                    0,
                    0,
                    0,
                    List.of(new BulkPredictErrorDTO(0, ex.getMessage(), null))
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new BulkPredictCsvUploadResponseDTO(
                    0,
                    0,
                    0,
                    List.of(new BulkPredictErrorDTO(0, "Unexpected error processing CSV", null))
            ));
        }
    }

    @GetMapping("/history")
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<List<PredictHistoryItemDTO>> getHistory() {
        Long userId = resolveUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(predictHistoryUseCase.getHistory(userId));
    }

    @GetMapping("/history/{requestId}")
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<PredictHistoryDetailDTO> getHistoryDetail(@PathVariable Long requestId) {
        Long userId = resolveUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(predictHistoryUseCase.getHistoryDetail(userId, requestId));
    }

    @GetMapping("/{requestId}/latest")
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<PredictResponseDTO> getLatest(@PathVariable Long requestId) {
        Long userId = resolveUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(predictFlightUseCase.getLatestPrediction(requestId, userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        log.warn("Validation error in predict flow: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        log.warn("Request validation failed: {}", message);
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        log.warn("CSV upload too large: {}", ex.getMessage());
        return ResponseEntity.status(413).body(new ErrorResponse("CSV file is too large"));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleModelError(WebClientResponseException ex) {
        log.error("Model service error status={} body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
        return ResponseEntity.status(503).body(new ErrorResponse("Model service unavailable"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
        log.error("Unexpected error in predict flow", ex);
        return ResponseEntity.status(503).body(new ErrorResponse("Model service unavailable"));
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
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
        if (principal instanceof UserDetails userDetails) {
            return parseUserId(userDetails.getUsername());
        }
        return parseUserId(authentication.getName());
    }

    private Long parseUserId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record ErrorResponse(String message) {
    }

    private void logJson(String message, Object payload) {
        try {
            log.info("{} payload={}", message, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.warn("{} payload=<failed to serialize>", message, ex);
        }
    }
}
