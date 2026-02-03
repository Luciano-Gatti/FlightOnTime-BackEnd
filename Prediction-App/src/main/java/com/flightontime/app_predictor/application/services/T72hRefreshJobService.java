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

    public T72hRefreshJobService(
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            ModelPredictionPort modelPredictionPort,
            DistanceUseCase distanceUseCase
    ) {
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.modelPredictionPort = modelPredictionPort;
        this.distanceUseCase = distanceUseCase;
    }

    public void refreshPredictions() {
        long startMillis = System.currentTimeMillis();
        OffsetDateTime startTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
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
                OffsetDateTime.now(ZoneOffset.UTC),
                durationMs,
                subscriptionsEvaluated,
                flightsInWindow,
                cacheStats.cacheHits,
                cacheStats.predictionsCreated,
                errors);
    }

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

    private OffsetDateTime resolveBucketStart(OffsetDateTime timestamp) {
        // Normaliza a intervalos de 3 horas en UTC para alinear la predicción.
        OffsetDateTime normalized = timestamp.withMinute(0).withSecond(0).withNano(0);
        int offset = normalized.getHour() % 3;
        return normalized.minusHours(offset);
    }

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }

    private static class PredictionCacheStats {
        private int cacheHits;
        private int predictionsCreated;
    }

}
