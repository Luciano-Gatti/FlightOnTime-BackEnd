package com.flightontime.app_predictor.infrastructure.in.web;

import com.flightontime.app_predictor.application.services.UserLookupService;
import com.flightontime.app_predictor.domain.model.PredictFlightRequest;
import com.flightontime.app_predictor.domain.model.PredictHistoryDetail;
import com.flightontime.app_predictor.domain.model.PredictHistoryItem;
import com.flightontime.app_predictor.domain.model.PredictHistoryPrediction;
import com.flightontime.app_predictor.domain.model.PredictionResult;
import com.flightontime.app_predictor.domain.ports.in.BulkPredictUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictHistoryUseCase;
import com.flightontime.app_predictor.infrastructure.in.dto.BulkPredictCsvUploadResponseDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.BulkPredictErrorDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryDetailDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryItemDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import com.flightontime.app_predictor.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
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

/**
 * Clase PredictController.
 */
@RestController
@RequestMapping("/predict")
@Tag(name = "Predict", description = "Endpoints para generar predicciones y consultar historiales")
@Validated
public class PredictController {
    private static final Logger log = LoggerFactory.getLogger(PredictController.class);
    private final PredictFlightUseCase predictFlightUseCase;
    private final PredictHistoryUseCase predictHistoryUseCase;
    private final BulkPredictUseCase bulkPredictUseCase;
    private final ObjectMapper objectMapper;
    private final UserLookupService userLookupService;
    private final JwtTokenProvider jwtTokenProvider;

    public PredictController(
            PredictFlightUseCase predictFlightUseCase,
            PredictHistoryUseCase predictHistoryUseCase,
            BulkPredictUseCase bulkPredictUseCase,
            ObjectMapper objectMapper,
            UserLookupService userLookupService,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.predictFlightUseCase = predictFlightUseCase;
        this.predictHistoryUseCase = predictHistoryUseCase;
        this.bulkPredictUseCase = bulkPredictUseCase;
        this.objectMapper = objectMapper;
        this.userLookupService = userLookupService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    @Operation(
            summary = "Generar predicción",
            description = "Genera una predicción de estado para el vuelo indicado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Predicción generada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "503", description = "Servicio de modelo no disponible")
    })
    public ResponseEntity<PredictResponseDTO> predict(
            @Valid @RequestBody PredictRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(httpRequest);
        logJson("Prediction request received userId=" + userId, request);
        PredictionResult result = predictFlightUseCase.predict(toPredictFlightRequest(request), userId);
        PredictResponseDTO response = toPredictResponseDto(result);
        logJson("Prediction response userId=" + userId, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/bulk-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Importar predicciones desde CSV",
            description = "Carga un archivo CSV para generar predicciones en lote."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Importación procesada"),
            @ApiResponse(responseCode = "400", description = "Archivo o parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "503", description = "Servicio de modelo no disponible")
    })
    public ResponseEntity<BulkPredictCsvUploadResponseDTO> bulkImport(
            @Parameter(description = "Archivo CSV con vuelos", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Ejecuta validación sin persistir", example = "false")
            @RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun,
            HttpServletRequest httpRequest
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new BulkPredictCsvUploadResponseDTO(
                    0,
                    0,
                    0,
                    List.of(new BulkPredictErrorDTO(0, "CSV file is required", null))
            ));
        }
        Long userId = resolveUserId(httpRequest);
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
    @Operation(
            summary = "Historial de predicciones",
            description = "Devuelve el historial de predicciones del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<List<PredictHistoryItemDTO>> getHistory(HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<PredictHistoryItemDTO> items = predictHistoryUseCase.getHistory(userId).stream()
                .map(this::toHistoryItemDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/history/{requestId}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Detalle de historial",
            description = "Devuelve el detalle de predicciones para un request específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Historial no encontrado")
    })
    public ResponseEntity<PredictHistoryDetailDTO> getHistoryDetail(
            @Parameter(description = "ID del request de vuelo", example = "123")
            @PathVariable Long requestId,
            HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        PredictHistoryDetail detail = predictHistoryUseCase.getHistoryDetail(userId, requestId);
        return ResponseEntity.ok(toHistoryDetailDto(detail));
    }

    @GetMapping("/{requestId}/latest")
    @Operation(
            summary = "Última predicción",
            description = "Devuelve la última predicción para un request específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Predicción encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Predicción no encontrada")
    })
    public ResponseEntity<PredictResponseDTO> getLatest(
            @Parameter(description = "ID del request de vuelo", example = "123")
            @PathVariable Long requestId,
            HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        PredictionResult result = predictFlightUseCase.getLatestPrediction(requestId, userId);
        return ResponseEntity.ok(toPredictResponseDto(result));
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

    private Long resolveUserId(HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return resolveUserIdFromToken(httpRequest).orElse(null);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalString) {
            if ("anonymousUser".equalsIgnoreCase(principalString)) {
                return resolveUserIdFromToken(httpRequest).orElse(null);
            }
            try {
                return Long.valueOf(principalString);
            } catch (NumberFormatException ex) {
                return resolveUserIdFromToken(httpRequest).orElse(null);
            }
        }
        if (principal instanceof UserDetails userDetails) {
            return parseUserId(userDetails.getUsername())
                    .or(() -> resolveUserIdByEmail(userDetails.getUsername()))
                    .orElse(null);
        }
        return parseUserId(authentication.getName())
                .or(() -> resolveUserIdByEmail(authentication.getName()))
                .orElse(null);
    }

    private Optional<Long> parseUserId(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Optional<Long> resolveUserIdByEmail(String email) {
        return userLookupService.findUserIdByEmail(email);
    }

    private Optional<Long> resolveUserIdFromToken(HttpServletRequest httpRequest) {
        if (httpRequest == null) {
            return Optional.empty();
        }
        String header = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = header.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return Optional.empty();
        }
        return Optional.ofNullable(jwtTokenProvider.getUserIdFromToken(token));
    }

/**
 * Registro ErrorResponse.
 */
    public record ErrorResponse(String message) {
    }

    private void logJson(String message, Object payload) {
        try {
            log.info("{} payload={}", message, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.warn("{} payload=<failed to serialize>", message, ex);
        }
    }

    private PredictFlightRequest toPredictFlightRequest(PredictRequestDTO request) {
        if (request == null) {
            return null;
        }
        return new PredictFlightRequest(
                request.flightDateUtc(),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber()
        );
    }

    private PredictResponseDTO toPredictResponseDto(PredictionResult result) {
        if (result == null) {
            return null;
        }
        return new PredictResponseDTO(
                result.predictedStatus(),
                result.predictedProbability(),
                result.confidence(),
                result.thresholdUsed(),
                result.modelVersion(),
                result.predictedAt()
        );
    }

    private PredictHistoryItemDTO toHistoryItemDto(PredictHistoryItem item) {
        if (item == null) {
            return null;
        }
        return new PredictHistoryItemDTO(
                item.flightRequestId(),
                item.flightDateUtc(),
                item.airlineCode(),
                item.originIata(),
                item.destIata(),
                item.flightNumber(),
                item.predictedStatus(),
                item.predictedProbability(),
                item.confidence(),
                item.thresholdUsed(),
                item.modelVersion(),
                item.predictedAt(),
                item.uniqueUsersCount()
        );
    }

    private PredictHistoryDetailDTO toHistoryDetailDto(PredictHistoryDetail detail) {
        if (detail == null) {
            return null;
        }
        List<PredictHistoryPredictionDTO> predictions = detail.predictions().stream()
                .map(this::toHistoryPredictionDto)
                .toList();
        return new PredictHistoryDetailDTO(
                detail.flightRequestId(),
                detail.flightDateUtc(),
                detail.airlineCode(),
                detail.originIata(),
                detail.destIata(),
                detail.flightNumber(),
                detail.uniqueUsersCount(),
                predictions
        );
    }

    private PredictHistoryPredictionDTO toHistoryPredictionDto(PredictHistoryPrediction prediction) {
        if (prediction == null) {
            return null;
        }
        return new PredictHistoryPredictionDTO(
                prediction.predictedStatus(),
                prediction.predictedProbability(),
                prediction.confidence(),
                prediction.thresholdUsed(),
                prediction.modelVersion(),
                prediction.predictedAt()
        );
    }
}
