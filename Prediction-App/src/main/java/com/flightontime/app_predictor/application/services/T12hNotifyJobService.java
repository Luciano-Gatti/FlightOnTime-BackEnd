package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionSource;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationLogRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase T12hNotifyJobService.
 */
@Service
public class T12hNotifyJobService {
    private static final Logger log = LoggerFactory.getLogger(T12hNotifyJobService.class);
    private static final String NOTIFICATION_TYPE = "T12H";
    private static final String CHANNEL = "SYSTEM";
    private static final Duration EARLY_WINDOW_START = Duration.ofHours(11);
    private static final Duration WINDOW_END = Duration.ofHours(13);

    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final ModelPredictionPort modelPredictionPort;
    private final NotificationPort notificationPort;
    private final NotificationLogRepositoryPort notificationLogRepositoryPort;
    private final DistanceUseCase distanceUseCase;
    private final Clock clock;

    /**
     * Construye el servicio de notificaciones T-12h.
     *
     * @param flightFollowRepositoryPort repositorio de seguimientos de vuelo.
     * @param flightRequestRepositoryPort repositorio de solicitudes de vuelo.
     * @param userPredictionRepositoryPort repositorio de snapshots de predicción por usuario.
     * @param predictionRepositoryPort repositorio de predicciones persistidas.
     * @param modelPredictionPort puerto hacia el modelo de predicción.
     * @param notificationPort puerto de envío de notificaciones.
     * @param notificationLogRepositoryPort repositorio de logs de notificaciones.
     * @param distanceUseCase caso de uso para calcular distancia entre aeropuertos.
     * @param clock variable de entrada clock.
     */
    public T12hNotifyJobService(
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            ModelPredictionPort modelPredictionPort,
            NotificationPort notificationPort,
            NotificationLogRepositoryPort notificationLogRepositoryPort,
            DistanceUseCase distanceUseCase,
            Clock clock
    ) {
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.modelPredictionPort = modelPredictionPort;
        this.notificationPort = notificationPort;
        this.notificationLogRepositoryPort = notificationLogRepositoryPort;
        this.distanceUseCase = distanceUseCase;
        this.clock = clock;
    }

    /**
     * Ejecuta el job de notificaciones T-12h para vuelos dentro de la ventana temporal.
     */
    public void notifyUsers() {
        long startMillis = System.currentTimeMillis();
        OffsetDateTime startTimestamp = OffsetDateTime.now(clock);
        OffsetDateTime windowStart = startTimestamp;
        OffsetDateTime windowEnd = startTimestamp.plusHours(13);
        log.info("Starting T12h notification job timestamp={} windowStart={} windowEnd={}",
                startTimestamp, windowStart, windowEnd);
        var follows = flightFollowRepositoryPort.findByFlightDateBetween(windowStart, windowEnd);
        Map<Long, List<NotificationCandidate>> notificationsByUser = new HashMap<>();
        int subscriptionsEvaluated = follows.size();
        int flightsInWindow = 0;
        int changesDetected = 0;
        int errors = 0;
        PredictionCacheStats cacheStats = new PredictionCacheStats();
        for (var follow : follows) {
            try {
                if (!isRelevantRefreshMode(follow.refreshMode())) {
                    continue;
                }
                FlightRequest request = flightRequestRepositoryPort.findById(follow.flightRequestId())
                        .orElse(null);
                if (request == null || !request.active() || request.flightDateUtc() == null) {
                    continue;
                }
                Duration timeToDeparture = Duration.between(startTimestamp, request.flightDateUtc());
                if (timeToDeparture.isNegative()) {
                    continue;
                }
                if (timeToDeparture.compareTo(WINDOW_END) > 0) {
                    continue;
                }
                flightsInWindow++;
                boolean lateNotification = timeToDeparture.compareTo(EARLY_WINDOW_START) < 0;
                int before = notificationsByUser.getOrDefault(follow.userId(), List.of()).size();
                processUserNotification(
                        follow.userId(),
                        follow.baselineSnapshotId(),
                        request,
                        startTimestamp,
                        lateNotification,
                        notificationsByUser,
                        cacheStats
                );
                int after = notificationsByUser.getOrDefault(follow.userId(), List.of()).size();
                if (after > before) {
                    changesDetected++;
                }
            } catch (Exception ex) {
                errors++;
                log.error("Error processing notification flight_request_id={} user_id={}",
                        follow.flightRequestId(), follow.userId(), ex);
            }
        }
        int emailsSent = dispatchNotifications(notificationsByUser, startTimestamp);
        long durationMs = System.currentTimeMillis() - startMillis;
        log.info("Finished T12h notification job timestamp={} durationMs={} subscriptionsEvaluated={} flightsInWindow={} "
                        + "cacheHits={} predictionsCreated={} changesDetected={} emailsSent={} errors={}",
                OffsetDateTime.now(clock),
                durationMs,
                subscriptionsEvaluated,
                flightsInWindow,
                cacheStats.cacheHits,
                cacheStats.predictionsCreated,
                changesDetected,
                emailsSent,
                errors);
    }

    /**
     * Evalúa si corresponde notificar a un usuario por cambios en la predicción.
     *
     * @param userId identificador del usuario.
     * @param baselineSnapshotId snapshot base de referencia.
     * @param request solicitud del vuelo.
     * @param now timestamp actual en UTC.
     * @param lateNotification indica si la notificación es tardía.
     * @param notificationsByUser mapa acumulador de notificaciones por usuario.
     * @param cacheStats acumulador de métricas de cache.
     * @param Map<Long variable de entrada Map<Long.
     */
    private void processUserNotification(
            Long userId,
            Long baselineSnapshotId,
            FlightRequest request,
            OffsetDateTime now,
            boolean lateNotification,
            Map<Long, List<NotificationCandidate>> notificationsByUser,
            PredictionCacheStats cacheStats
    ) {
        // Evita duplicados consultando el notification_log antes de construir mensajes.
        Optional<NotificationLog> existing = notificationLogRepositoryPort
                .findByUserIdAndRequestIdAndType(userId, request.id(), NOTIFICATION_TYPE);
        if (existing.isPresent()) {
            return;
        }
        Optional<UserPrediction> baselineUserPrediction = resolveBaselineSnapshot(userId, baselineSnapshotId, request.id());
        if (baselineUserPrediction.isEmpty()) {
            return;
        }
        Optional<Prediction> baselinePrediction = predictionRepositoryPort
                .findById(baselineUserPrediction.get().flightPredictionId());
        if (baselinePrediction.isEmpty()) {
            return;
        }
        Prediction currentPrediction = getOrCreateCurrentPrediction(request, now, cacheStats);
        if (!isRelevantStatusChange(
                baselinePrediction.get().predictedStatus(),
                currentPrediction.predictedStatus()
        )) {
            return;
        }
        NotificationCandidate candidate = new NotificationCandidate(
                userId,
                request,
                baselineUserPrediction.get().id(),
                currentPrediction.predictedStatus(),
                buildMessage(request, baselinePrediction.get(), currentPrediction, lateNotification)
        );
        notificationsByUser
                .computeIfAbsent(userId, ignored -> new ArrayList<>())
                .add(candidate);
    }

    /**
     * Envía notificaciones agrupadas por usuario y persiste el log de envío.
     *
     * @param notificationsByUser mapa de notificaciones agrupadas por usuario.
     * @param now timestamp actual en UTC.
     * @return cantidad total de mensajes enviados.
     * @param Map<Long variable de entrada Map<Long.
     */
    private int dispatchNotifications(
            Map<Long, List<NotificationCandidate>> notificationsByUser,
            OffsetDateTime now
    ) {
        int emailsSent = 0;
        for (Map.Entry<Long, List<NotificationCandidate>> entry : notificationsByUser.entrySet()) {
            List<NotificationCandidate> candidates = entry.getValue();
            if (candidates == null || candidates.isEmpty()) {
                continue;
            }
            List<String> messages = candidates.stream()
                    .map(NotificationCandidate::message)
                    .toList();
            notificationPort.sendT12hSummary(entry.getKey(), messages);
            emailsSent += messages.size();
            for (NotificationCandidate candidate : candidates) {
                NotificationLog log = new NotificationLog(
                        null,
                        candidate.userId(),
                        candidate.request().id(),
                        NOTIFICATION_TYPE,
                        candidate.userPredictionId(),
                        CHANNEL,
                        candidate.predictedStatus(),
                        candidate.message(),
                        now,
                        now
                );
                notificationLogRepositoryPort.save(log);
            }
        }
        return emailsSent;
    }

    /**
     * Construye el mensaje de notificación en base al cambio de estado.
     *
     * @param request solicitud del vuelo.
     * @param baseline predicción base.
     * @param current predicción actual.
     * @param lateNotification indica si la notificación es tardía.
     * @return mensaje listo para enviar.
     */
    private String buildMessage(
            FlightRequest request,
            Prediction baseline,
            Prediction current,
            boolean lateNotification
    ) {
        String flightLabel = hasFlightNumber(request.flightNumber())
                ? request.flightNumber()
                : "Request " + request.id();
        String baseMessage = "Flight " + flightLabel + " cambió de " + baseline.predictedStatus()
                + " a " + current.predictedStatus();
        if (!lateNotification) {
            return baseMessage;
        }
        return baseMessage + " (notificación tardía)";
    }

    /**
     * Verifica si el cambio de estado entre baseline y actual es relevante.
     *
     * @param baselineStatus estado base.
     * @param currentStatus estado actual.
     * @return true si el cambio es entre estados soportados y distintos.
     */
    private boolean isRelevantStatusChange(String baselineStatus, String currentStatus) {
        if (baselineStatus == null || currentStatus == null) {
            return false;
        }
        boolean baselineSupported = isOnTimeOrDelayed(baselineStatus);
        boolean currentSupported = isOnTimeOrDelayed(currentStatus);
        return baselineSupported && currentSupported && !baselineStatus.equals(currentStatus);
    }

    /**
     * Indica si el estado es uno de los soportados para comparación (ON_TIME/DELAYED).
     *
     * @param status estado a evaluar.
     * @return true si el estado es ON_TIME o DELAYED.
     */
    private boolean isOnTimeOrDelayed(String status) {
        return "ON_TIME".equals(status) || "DELAYED".equals(status);
    }

    /**
     * Verifica si el número de vuelo es válido y no está vacío.
     *
     * @param flightNumber número de vuelo recibido.
     * @return true si existe y no está en blanco.
     */
    private boolean hasFlightNumber(String flightNumber) {
        return flightNumber != null && !flightNumber.isBlank();
    }

    /**
     * Obtiene una predicción actual usando cache por bucket o invocando el modelo.
     *
     * @param request solicitud del vuelo.
     * @param now timestamp actual en UTC.
     * @param cacheStats acumulador de métricas de cache.
     * @return predicción persistida (cacheada o nueva).
     */
    private Prediction getOrCreateCurrentPrediction(
            FlightRequest request,
            OffsetDateTime now,
            PredictionCacheStats cacheStats
    ) {
        // Las predicciones se agrupan por ventanas fijas para reutilizar resultados recientes.
        OffsetDateTime bucketStart = resolveBucketStart(now);
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(
                request.id(),
                bucketStart
        );
        if (cached.isPresent()) {
            // Si existe en cache, no se llama al modelo para esta ventana.
            cacheStats.cacheHits++;
            return cached.get();
        }
        PredictFlightCommand command = buildCommand(request);
        var modelPrediction = modelPredictionPort.requestPrediction(command);
        Prediction prediction = new Prediction(
                null,
                request.id(),
                bucketStart,
                modelPrediction.predictedStatus(),
                modelPrediction.predictedProbability(),
                modelPrediction.confidence(),
                modelPrediction.thresholdUsed(),
                modelPrediction.modelVersion(),
                PredictionSource.SYSTEM,
                now,
                now
        );
        cacheStats.predictionsCreated++;
        return predictionRepositoryPort.save(prediction);
    }

    /**
     * Construye el comando de predicción a partir de una solicitud de vuelo.
     *
     * @param request solicitud del vuelo.
     * @return comando listo para invocar el modelo.
     */
    private PredictFlightCommand buildCommand(FlightRequest request) {
        double distance = request.distance() > 0 ? request.distance()
                : distanceUseCase.calculateDistance(request.originIata(), request.destIata());
        return new PredictFlightCommand(
                toUtc(request.flightDateUtc()),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                distance
        );
    }

    /**
     * Normaliza un timestamp al inicio del bucket de 3 horas.
     *
     * @param timestamp timestamp base.
     * @return inicio del bucket correspondiente.
     */
    private OffsetDateTime resolveBucketStart(OffsetDateTime timestamp) {
        // Ajusta a buckets de 3 horas para mantener consistencia en las predicciones.
        OffsetDateTime normalized = timestamp.withMinute(0).withSecond(0).withNano(0);
        int offset = normalized.getHour() % 3;
        return normalized.minusHours(offset);
    }

    /**
     * Convierte una fecha/hora a UTC conservando el instante.
     *
     * @param value fecha/hora original.
     * @return fecha/hora en UTC o null si el valor era null.
     */
    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }

    /**
     * Resuelve el snapshot base de referencia para comparar cambios.
     *
     * @param userId identificador del usuario.
     * @param baselineSnapshotId snapshot base explícito (opcional).
     * @param flightRequestId identificador de la solicitud de vuelo.
     * @return snapshot base si existe y es válido.
     */
    private Optional<UserPrediction> resolveBaselineSnapshot(
            Long userId,
            Long baselineSnapshotId,
            Long flightRequestId
    ) {
        if (baselineSnapshotId != null) {
            return userPredictionRepositoryPort.findById(baselineSnapshotId)
                    .filter(snapshot -> userId.equals(snapshot.userId())
                            && flightRequestId.equals(snapshot.flightRequestId()));
        }
        // Usa el último snapshot disponible como baseline cuando no se especifica uno.
        return userPredictionRepositoryPort.findLatestByUserIdAndRequestId(userId, flightRequestId);
    }

    /**
     * Verifica si el modo de refresco habilita notificaciones T-12h.
     *
     * @param refreshMode modo de refresco configurado.
     * @return true si corresponde notificar.
     */
    private boolean isRelevantRefreshMode(RefreshMode refreshMode) {
        return RefreshMode.T72_REFRESH.equals(refreshMode) || RefreshMode.T12_ONLY.equals(refreshMode);
    }

    /**
     * Contenedor de datos para notificaciones pendientes.
     * @param userId variable de entrada userId.
     * @param request variable de entrada request.
     * @param userPredictionId variable de entrada userPredictionId.
     * @param predictedStatus variable de entrada predictedStatus.
     * @param message variable de entrada message.
     * @return resultado de la operación resultado.
     */
    private record NotificationCandidate(
            Long userId,
            FlightRequest request,
            Long userPredictionId,
            String predictedStatus,
            String message
    ) {
    }

    /**
     * Acumulador simple de métricas de cache en memoria.
     */
    private static class PredictionCacheStats {
        private int cacheHits;
        private int predictionsCreated;
    }
}
