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

    /**
     * Construye el controlador de predicciones.
     *
     * @param predictFlightUseCase caso de uso para predecir un vuelo.
     * @param predictHistoryUseCase caso de uso para historial de predicciones.
     * @param bulkPredictUseCase caso de uso para importación masiva por CSV.
     * @param objectMapper serializador JSON para logging.
     * @param userLookupService servicio de resolución de usuario.
     * @param jwtTokenProvider proveedor de JWT para validación.
     */
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
    /**
     * Genera una predicción de vuelo para el request recibido.
     *
     * @param request DTO con datos del vuelo.
     * @param httpRequest request HTTP para resolver usuario.
     * @return respuesta con la predicción calculada.
     */
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
    /**
     * Importa predicciones en lote desde un archivo CSV.
     *
     * @param file archivo CSV con vuelos.
     * @param dryRun indica validación sin persistencia.
     * @param httpRequest request HTTP para resolver usuario.
     * @return respuesta con el resultado de la importación.
     */
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
    /**
     * Devuelve el historial de predicciones del usuario autenticado.
     *
     * @param httpRequest request HTTP para resolver usuario.
     * @return lista de ítems de historial o 401 si no está autenticado.
     */
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
    /**
     * Devuelve el detalle del historial para un request específico.
     *
     * @param requestId id del request de vuelo.
     * @param httpRequest request HTTP para resolver usuario.
     * @return detalle del historial o 401 si no está autenticado.
     */
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
    /**
     * Obtiene la última predicción para un request del usuario.
     *
     * @param requestId id del request de vuelo.
     * @param httpRequest request HTTP para resolver usuario.
     * @return predicción más reciente o 401 si no está autenticado.
     */
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
    /**
     * Maneja errores de validación de datos de entrada.
     *
     * @param ex excepción lanzada por validaciones.
     * @return respuesta con detalle del error.
     */
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        log.warn("Validation error in predict flow: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /**
     * Maneja errores de validación de argumentos anotados.
     *
     * @param ex excepción de validación de argumentos.
     * @return respuesta con mensaje de error.
     */
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        log.warn("Request validation failed: {}", message);
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    /**
     * Maneja el caso de archivos CSV que exceden el tamaño permitido.
     *
     * @param ex excepción por tamaño máximo excedido.
     * @return respuesta 413 con mensaje.
     */
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        log.warn("CSV upload too large: {}", ex.getMessage());
        return ResponseEntity.status(413).body(new ErrorResponse("CSV file is too large"));
    }

    @ExceptionHandler(WebClientResponseException.class)
    /**
     * Maneja errores devueltos por el servicio de modelo.
     *
     * @param ex excepción del cliente web.
     * @return respuesta 503.
     */
    public ResponseEntity<ErrorResponse> handleModelError(WebClientResponseException ex) {
        log.error("Model service error status={} body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
        return ResponseEntity.status(503).body(new ErrorResponse("Model service unavailable"));
    }

    @ExceptionHandler(Exception.class)
    /**
     * Maneja errores inesperados dentro del flujo de predicción.
     *
     * @param ex excepción inesperada.
     * @return respuesta 503.
     */
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
        log.error("Unexpected error in predict flow", ex);
        return ResponseEntity.status(503).body(new ErrorResponse("Model service unavailable"));
    }

    /**
     * Resuelve el userId desde el contexto de seguridad o desde el token JWT.
     *
     * @param httpRequest request HTTP para leer el header Authorization si es necesario.
     * @return userId resuelto o null si no se pudo determinar.
     */
    private Long resolveUserId(HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Si el contexto no está autenticado, intenta resolver desde el token.
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
        // Si el principal es UserDetails, intenta interpretar como id o por email.
        if (principal instanceof UserDetails userDetails) {
            return parseUserId(userDetails.getUsername())
                    .or(() -> resolveUserIdByEmail(userDetails.getUsername()))
                    .orElse(null);
        }
        return parseUserId(authentication.getName())
                .or(() -> resolveUserIdByEmail(authentication.getName()))
                .orElse(null);
    }

    /**
     * Intenta convertir un string a userId numérico.
     *
     * @param value string a convertir.
     * @return optional con el userId si aplica.
     */
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

    /**
     * Resuelve un userId consultando por email.
     *
     * @param email email del usuario.
     * @return optional con el userId si existe.
     */
    private Optional<Long> resolveUserIdByEmail(String email) {
        return userLookupService.findUserIdByEmail(email);
    }

    /**
     * Resuelve el userId desde un token JWT en el header Authorization.
     *
     * @param httpRequest request HTTP con headers.
     * @return optional con el userId si el token es válido.
     */
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

    /**
     * Loggea un payload JSON en modo info, con manejo de errores de serialización.
     *
     * @param message mensaje base de log.
     * @param payload objeto a serializar.
     */
    private void logJson(String message, Object payload) {
        try {
            log.info("{} payload={}", message, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.warn("{} payload=<failed to serialize>", message, ex);
        }
    }

    /**
     * Convierte el DTO de request en el modelo de dominio de predicción.
     *
     * @param request DTO de entrada.
     * @return request de dominio o null si el DTO es null.
     */
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

    /**
     * Convierte el resultado de predicción a DTO de salida.
     *
     * @param result resultado de dominio.
     * @return DTO de respuesta o null si result es null.
     */
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

    /**
     * Convierte un ítem de historial de dominio a DTO.
     *
     * @param item ítem de historial.
     * @return DTO correspondiente o null si item es null.
     */
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

    /**
     * Convierte el detalle de historial a DTO de salida.
     *
     * @param detail detalle de historial.
     * @return DTO correspondiente o null si detail es null.
     */
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

    /**
     * Convierte una predicción histórica a DTO.
     *
     * @param prediction predicción histórica.
     * @return DTO correspondiente o null si prediction es null.
     */
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
