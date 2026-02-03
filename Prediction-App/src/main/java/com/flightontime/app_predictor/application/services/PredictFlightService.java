package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.PredictFlightRequest;
import com.flightontime.app_predictor.domain.model.PredictionResult;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.model.UserPredictionSource;
import com.flightontime.app_predictor.domain.exception.BusinessErrorCodes;
import com.flightontime.app_predictor.domain.exception.BusinessException;
import com.flightontime.app_predictor.domain.exception.DomainException;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase PredictFlightService.
 */
@Service
public class PredictFlightService implements PredictFlightUseCase {
    private static final Logger log = LoggerFactory.getLogger(PredictFlightService.class);
    private static final Pattern AIRLINE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2}$");
    private static final Pattern IATA_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final PredictionWorkflowService predictionWorkflowService;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;

    /**
     * Construye el servicio de predicción de vuelos.
     *
     * @param flightRequestRepositoryPort repositorio de solicitudes de vuelo.
     * @param flightFollowRepositoryPort repositorio de suscripciones/seguimientos de vuelo.
     * @param predictionWorkflowService servicio que orquesta el flujo de predicción y cache.
     * @param userPredictionRepositoryPort repositorio de snapshots de predicciones de usuario.
     */
    public PredictFlightService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            PredictionWorkflowService predictionWorkflowService,
            UserPredictionRepositoryPort userPredictionRepositoryPort
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.predictionWorkflowService = predictionWorkflowService;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
    }

    @Override
    /**
     * Ejecuta una predicción de vuelo a demanda para un usuario.
     *
     * @param request solicitud con datos del vuelo (fecha, aerolínea, rutas y número opcional).
     * @param userId identificador del usuario que realiza la consulta (puede ser null).
     * @return resultado de la predicción (estado, probabilidad, confianza, umbral, versión y timestamp).
     */
    public PredictionResult predict(PredictFlightRequest request, Long userId) {
        // Valida los campos mínimos del request antes de iniciar el flujo.
        PredictFlightRequest normalizedRequest = normalizeRequest(request);
        validateRequest(normalizedRequest);
        OffsetDateTime startTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
        log.info("Starting prediction workflow userId={} request={}", userId, normalizedRequest);
        // Orquesta la resolución de predicción (cache por bucket o llamado al modelo).
        var workflowResult = predictionWorkflowService.predict(
                normalizedRequest.flightDateUtc(),
                normalizedRequest.airlineCode(),
                normalizedRequest.originIata(),
                normalizedRequest.destIata(),
                normalizedRequest.flightNumber(),
                userId,
                true,
                false
        );
        if (workflowResult.prediction() != null) {
            boolean cacheHit = workflowResult.prediction().createdAt() != null
                    && workflowResult.prediction().createdAt().isBefore(startTimestamp);
            log.info("Prediction resolved flight_request_id={} bucketStart={} cacheHit={}",
                    workflowResult.flightRequest() != null ? workflowResult.flightRequest().id() : null,
                    workflowResult.prediction().forecastBucketUtc(),
                    cacheHit);
        }
        log.info("Prediction workflow completed userId={} predictionId={} requestId={}",
                userId,
                workflowResult.prediction() != null ? workflowResult.prediction().id() : null,
                workflowResult.flightRequest() != null ? workflowResult.flightRequest().id() : null);
        // Si hay usuario autenticado, registra snapshot y suscripción para futuros refrescos.
        if (userId != null && workflowResult.prediction() != null && workflowResult.flightRequest() != null) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            UserPrediction snapshot = resolveUserSnapshot(
                    userId,
                    workflowResult.flightRequest().id(),
                    workflowResult.prediction().id(),
                    UserPredictionSource.USER_QUERY,
                    now
            );
            log.debug("User snapshot resolved userId={} flight_request_id={} snapshotId={}",
                    userId, workflowResult.flightRequest().id(), snapshot.id());
            upsertFlightFollow(
                    userId,
                    workflowResult.flightRequest().id(),
                    snapshot.id(),
                    RefreshMode.T72_REFRESH
            );
            log.debug("Subscription upserted userId={} flight_request_id={} refreshMode={}",
                    userId, workflowResult.flightRequest().id(), RefreshMode.T72_REFRESH);
        }
        return new PredictionResult(
                workflowResult.result().predictedStatus(),
                workflowResult.result().predictedProbability(),
                workflowResult.result().confidence(),
                workflowResult.result().thresholdUsed(),
                workflowResult.result().modelVersion(),
                workflowResult.result().predictedAt()
        );
    }

    @Override
    /**
     * Obtiene la última predicción disponible para una solicitud de vuelo de un usuario.
     *
     * @param requestId identificador de la solicitud de vuelo.
     * @param userId identificador del usuario dueño de la solicitud.
     * @return resultado de la predicción más reciente (estado, probabilidad, confianza, umbral, versión y timestamp).
     */
    public PredictionResult getLatestPrediction(Long requestId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        OffsetDateTime startTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
        log.info("Fetching latest prediction userId={} requestId={}", userId, requestId);
        // Verifica existencia de la solicitud y de una predicción previa del usuario.
        FlightRequest flightRequest = flightRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        userPredictionRepositoryPort.findLatestByUserIdAndRequestId(userId, requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        // Reutiliza cache por bucket o genera una nueva predicción.
        var result = predictionWorkflowService.getOrCreatePredictionForRequest(flightRequest);
        if (result.prediction() != null) {
            boolean cacheHit = result.prediction().createdAt() != null
                    && result.prediction().createdAt().isBefore(startTimestamp);
            log.info("Latest prediction resolved flight_request_id={} bucketStart={} cacheHit={}",
                    requestId,
                    result.prediction().forecastBucketUtc(),
                    cacheHit);
        }
        log.info("Latest prediction resolved userId={} requestId={} predictionId={}",
                userId, requestId, result.prediction() != null ? result.prediction().id() : null);
        return new PredictionResult(
                result.result().predictedStatus(),
                result.result().predictedProbability(),
                result.result().confidence(),
                result.result().thresholdUsed(),
                result.result().modelVersion(),
                result.result().predictedAt()
        );
    }

    /**
     * Valida la consistencia básica del request de predicción.
     *
     * @param request solicitud de predicción a validar.
     */
    private void validateRequest(PredictFlightRequest request) {
        if (request == null) {
            throw new DomainException("predict request is required");
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (request.flightDateUtc() == null || !request.flightDateUtc().isAfter(now)) {
            throw new DomainException("flightDateUtc must be in the future");
        }
        if (request.airlineCode() == null || request.airlineCode().isBlank()) {
            throw new DomainException("airlineCode is required");
        }
        if (hasInternalWhitespace(request.airlineCode())) {
            throw new DomainException("airlineCode must not contain spaces");
        }
        if (!AIRLINE_CODE_PATTERN.matcher(request.airlineCode()).matches()) {
            throw new BusinessException(
                    BusinessErrorCodes.INVALID_AIRLINE_CODE,
                    "airlineCode must be 2 uppercase IATA characters"
            );
        }
        if (request.originIata() == null || request.originIata().isBlank()) {
            throw new DomainException("originIata is required");
        }
        if (hasInternalWhitespace(request.originIata())) {
            throw new DomainException("originIata must not contain spaces");
        }
        if (!IATA_CODE_PATTERN.matcher(request.originIata()).matches()) {
            throw new BusinessException(
                    BusinessErrorCodes.INVALID_IATA,
                    "originIata must be a 3-letter IATA code"
            );
        }
        if (request.destIata() == null || request.destIata().isBlank()) {
            throw new DomainException("destIata is required");
        }
        if (hasInternalWhitespace(request.destIata())) {
            throw new DomainException("destIata must not contain spaces");
        }
        if (!IATA_CODE_PATTERN.matcher(request.destIata()).matches()) {
            throw new BusinessException(
                    BusinessErrorCodes.INVALID_IATA,
                    "destIata must be a 3-letter IATA code"
            );
        }
        if (request.originIata().equalsIgnoreCase(request.destIata())) {
            throw new BusinessException(
                    BusinessErrorCodes.INVALID_ROUTE,
                    "origin and destination cannot be the same"
            );
        }
    }

    private PredictFlightRequest normalizeRequest(PredictFlightRequest request) {
        if (request == null) {
            return null;
        }
        return new PredictFlightRequest(
                request.flightDateUtc(),
                normalizeToken(request.airlineCode(), true),
                normalizeToken(request.originIata(), true),
                normalizeToken(request.destIata(), true),
                normalizeToken(request.flightNumber(), false)
        );
    }

    private String normalizeToken(String value, boolean upperCase) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            return null;
        }
        return upperCase ? normalized.toUpperCase() : normalized;
    }

    private boolean hasInternalWhitespace(String value) {
        return value != null && value.chars().anyMatch(Character::isWhitespace);
    }

    /**
     * Inserta o actualiza el seguimiento del usuario para un vuelo.
     *
     * @param userId identificador del usuario.
     * @param flightRequestId identificador de la solicitud de vuelo.
     * @param snapshotId identificador del snapshot base de predicción.
     * @param refreshMode modo de refresco configurado para el seguimiento.
     */
    private void upsertFlightFollow(
            Long userId,
            Long flightRequestId,
            Long snapshotId,
            RefreshMode refreshMode
    ) {
        // Mantiene la suscripción activa del usuario para notificaciones futuras.
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        FlightFollow flightFollow = flightFollowRepositoryPort
                .findByUserIdAndFlightRequestId(userId, flightRequestId)
                .map(existing -> new FlightFollow(
                        existing.id(),
                        userId,
                        flightRequestId,
                        refreshMode,
                        resolveBaselineSnapshotId(existing.baselineSnapshotId(), snapshotId),
                        existing.createdAt(),
                        now
                ))
                .orElseGet(() -> new FlightFollow(
                        null,
                        userId,
                        flightRequestId,
                        refreshMode,
                        snapshotId,
                        now,
                        now
                ));
        flightFollowRepositoryPort.save(flightFollow);
    }

    /**
     * Resuelve (o crea) el snapshot de predicción del usuario.
     *
     * @param userId identificador del usuario.
     * @param flightRequestId identificador de la solicitud de vuelo.
     * @param flightPredictionId identificador de la predicción asociada.
     * @param source origen del snapshot (consulta usuario, CSV, etc.).
     * @param now timestamp de creación.
     * @return snapshot existente si corresponde o uno nuevo persistido.
     */
    private UserPrediction resolveUserSnapshot(
            Long userId,
            Long flightRequestId,
            Long flightPredictionId,
            UserPredictionSource source,
            OffsetDateTime now
    ) {
        // Crea un snapshot solo si no existe uno con la misma predicción.
        return userPredictionRepositoryPort.findLatestByUserIdAndRequestId(userId, flightRequestId)
                .filter(existing -> flightPredictionId.equals(existing.flightPredictionId()))
                .orElseGet(() -> userPredictionRepositoryPort.save(new UserPrediction(
                        null,
                        userId,
                        flightRequestId,
                        flightPredictionId,
                        source,
                        now
                )));
    }

    /**
     * Determina el snapshot base, priorizando el existente si está presente.
     *
     * @param existingBaselineSnapshotId snapshot base ya registrado.
     * @param snapshotId snapshot actual a usar si no hay base previa.
     * @return id del snapshot base definitivo.
     */
    private Long resolveBaselineSnapshotId(Long existingBaselineSnapshotId, Long snapshotId) {
        return existingBaselineSnapshotId == null ? snapshotId : existingBaselineSnapshotId;
    }

}
