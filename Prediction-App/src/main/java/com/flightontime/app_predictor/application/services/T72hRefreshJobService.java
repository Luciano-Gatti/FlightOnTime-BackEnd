package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionSource;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase T72hRefreshJobService.
 */
@Service
public class T72hRefreshJobService {
    private static final Logger log = LoggerFactory.getLogger(T72hRefreshJobService.class);
    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final ModelPredictionPort modelPredictionPort;
    private final DistanceUseCase distanceUseCase;
    private final Clock clock;

    /**
     * Construye el servicio que refresca predicciones en la ventana T-72h.
     *
     * @param flightFollowRepositoryPort repositorio de seguimientos de vuelo.
     * @param flightRequestRepositoryPort repositorio de solicitudes de vuelo.
     * @param predictionRepositoryPort repositorio de predicciones persistidas.
     * @param modelPredictionPort puerto hacia el modelo de predicción.
     * @param distanceUseCase caso de uso para calcular distancia entre aeropuertos.
     */
    public T72hRefreshJobService(
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            ModelPredictionPort modelPredictionPort,
            DistanceUseCase distanceUseCase,
            Clock clock
    ) {
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.modelPredictionPort = modelPredictionPort;
        this.distanceUseCase = distanceUseCase;
        this.clock = clock;
    }

    /**
     * Ejecuta el job de refresco T-72h para vuelos activos en la ventana de 72 horas.
     */
    public void refreshPredictions() {
        long startMillis = System.currentTimeMillis();
        OffsetDateTime startTimestamp = OffsetDateTime.now(clock);
        OffsetDateTime end = startTimestamp.plusHours(72);
        log.info("Starting T72h refresh job timestamp={} windowStart={} windowEnd={}",
                startTimestamp, startTimestamp, end);
        List<FlightFollow> follows = flightFollowRepositoryPort
                .findByRefreshModeAndFlightDateBetween(RefreshMode.T72_REFRESH, startTimestamp, end);
        int subscriptionsEvaluated = follows.size();
        int flightsInWindow = 0;
        int errors = 0;
        PredictionCacheStats cacheStats = new PredictionCacheStats();
        for (FlightFollow follow : follows) {
            try {
                FlightRequest request = flightRequestRepositoryPort.findById(follow.flightRequestId())
                        .orElse(null);
                if (request == null || !request.active()) {
                    continue;
                }
                if (request.flightDateUtc().isBefore(startTimestamp) || request.flightDateUtc().isAfter(end)) {
                    continue;
                }
                flightsInWindow++;
                PredictFlightCommand command = buildCommand(request);
                getOrCreatePrediction(request.id(), command, startTimestamp, cacheStats);
            } catch (Exception ex) {
                errors++;
                log.error("Error refreshing prediction flight_request_id={} user_id={}",
                        follow.flightRequestId(), follow.userId(), ex);
            }
        }
        long durationMs = System.currentTimeMillis() - startMillis;
        log.info("Finished T72h refresh job timestamp={} durationMs={} subscriptionsEvaluated={} flightsInWindow={} "
                        + "cacheHits={} predictionsCreated={} errors={}",
                OffsetDateTime.now(clock),
                durationMs,
                subscriptionsEvaluated,
                flightsInWindow,
                cacheStats.cacheHits,
                cacheStats.predictionsCreated,
                errors);
    }

    /**
     * Construye el comando de predicción a partir de una solicitud de vuelo.
     *
     * @param request solicitud de vuelo persistida.
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
     * Obtiene una predicción del cache por bucket o la crea llamando al modelo.
     *
     * @param flightRequestId identificador de la solicitud de vuelo.
     * @param command comando con datos del vuelo.
     * @param now timestamp actual en UTC.
     * @param cacheStats acumulador de métricas de cache.
     * @return predicción persistida (cacheada o nueva).
     */
    private Prediction getOrCreatePrediction(
            Long flightRequestId,
            PredictFlightCommand command,
            OffsetDateTime now,
            PredictionCacheStats cacheStats
    ) {
        // Bucketiza las predicciones para minimizar llamadas redundantes al modelo.
        OffsetDateTime bucketStart = resolveBucketStart(now);
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(
                flightRequestId,
                bucketStart
        );
        if (cached.isPresent()) {
            // Si hay cache para el bucket actual, reutiliza la predicción guardada.
            cacheStats.cacheHits++;
            return cached.get();
        }
        var modelPrediction = modelPredictionPort.requestPrediction(command);
        Prediction prediction = new Prediction(
                null,
                flightRequestId,
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
     * Normaliza un timestamp al inicio del bucket de 3 horas.
     *
     * @param timestamp timestamp base.
     * @return inicio del bucket correspondiente.
     */
    private OffsetDateTime resolveBucketStart(OffsetDateTime timestamp) {
        // Normaliza a intervalos de 3 horas en UTC para alinear la predicción.
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
     * Acumulador simple de métricas de cache en memoria.
     */
    private static class PredictionCacheStats {
        private int cacheHits;
        private int predictionsCreated;
    }

}
