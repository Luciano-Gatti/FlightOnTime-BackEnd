package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionResult;
import com.flightontime.app_predictor.domain.model.PredictionSource;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.model.UserPredictionSource;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase PredictionWorkflowService.
 */
@Service
public class PredictionWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(PredictionWorkflowService.class);
    /**
     * Registro PredictionWorkflowResult.
     */
    public record PredictionWorkflowResult(
            FlightRequest flightRequest,
            Prediction prediction,
            PredictionResult result
    ) {
    }
    private final ModelPredictionPort modelPredictionPort;
    private final DistanceUseCase distanceUseCase;
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;

    public PredictionWorkflowService(
            ModelPredictionPort modelPredictionPort,
            DistanceUseCase distanceUseCase,
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort
    ) {
        this.modelPredictionPort = modelPredictionPort;
        this.distanceUseCase = distanceUseCase;
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
    }

    public PredictionWorkflowResult predict(
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata,
            String flightNumber,
            Long userId,
            boolean persistWhenAnonymous,
            boolean createUserPrediction
    ) {
        log.info("Starting prediction workflow userId={} origin={} dest={}", userId, originIata, destIata);
        double distance = distanceUseCase.calculateDistance(originIata, destIata);
        log.info("Calculated distance origin={} destination={} distanceKm={}", originIata, destIata, distance);
        PredictFlightCommand command = new PredictFlightCommand(
                flightDateUtc,
                airlineCode,
                originIata,
                destIata,
                flightNumber,
                distance
        );
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (userId == null && !persistWhenAnonymous) {
            log.info("Anonymous prediction request, skipping persistence.");
            var prediction = modelPredictionPort.requestPrediction(command);
            PredictionResult result = new PredictionResult(
                    prediction.predictedStatus(),
                    prediction.predictedProbability(),
                    prediction.confidence(),
                    prediction.thresholdUsed(),
                    prediction.modelVersion(),
                    now
            );
            log.info("Anonymous prediction workflow completed origin={} dest={}", originIata, destIata);
            return new PredictionWorkflowResult(null, null, result);
        }
        FlightRequest flightRequest = getOrCreateFlightRequest(
                userId,
                flightDateUtc,
                airlineCode,
                originIata,
                destIata,
                flightNumber,
                distance,
                now
        );
        log.info("Resolved flight request id={} userId={}", flightRequest.id(), userId);
        Prediction prediction = getOrCreatePrediction(flightRequest.id(), command, now);
        log.info("Resolved prediction id={} for flightRequestId={}", prediction.id(), flightRequest.id());
        if (createUserPrediction && userId != null) {
            userPredictionRepositoryPort.save(new UserPrediction(
                    null,
                    userId,
                    flightRequest.id(),
                    prediction.id(),
                    UserPredictionSource.USER_QUERY,
                    now
            ));
            log.info("Saved user prediction userId={} predictionId={}", userId, prediction.id());
        }
        PredictionResult result = new PredictionResult(
                prediction.predictedStatus(),
                prediction.predictedProbability(),
                prediction.confidence(),
                prediction.thresholdUsed(),
                prediction.modelVersion(),
                prediction.predictedAt()
        );
        log.info("Prediction workflow completed requestId={} predictionId={}",
                flightRequest.id(), prediction.id());
        return new PredictionWorkflowResult(flightRequest, prediction, result);
    }

    public PredictionWorkflowResult getOrCreatePredictionForRequest(FlightRequest flightRequest) {
        double distance = distanceUseCase.calculateDistance(flightRequest.originIata(), flightRequest.destIata());
        log.info("Calculated distance for existing requestId={} distanceKm={}", flightRequest.id(), distance);
        PredictFlightCommand command = new PredictFlightCommand(
                flightRequest.flightDateUtc(),
                flightRequest.airlineCode(),
                flightRequest.originIata(),
                flightRequest.destIata(),
                flightRequest.flightNumber(),
                distance
        );
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Prediction prediction = getOrCreatePrediction(flightRequest.id(), command, now);
        log.info("Resolved prediction id={} for flightRequestId={}", prediction.id(), flightRequest.id());
        PredictionResult result = new PredictionResult(
                prediction.predictedStatus(),
                prediction.predictedProbability(),
                prediction.confidence(),
                prediction.thresholdUsed(),
                prediction.modelVersion(),
                prediction.predictedAt()
        );
        return new PredictionWorkflowResult(flightRequest, prediction, result);
    }

    private FlightRequest getOrCreateFlightRequest(
            Long userId,
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata,
            String flightNumber,
            double distance,
            OffsetDateTime now
    ) {
        // Reutiliza el request existente para evitar duplicar solicitudes equivalentes.
        OffsetDateTime normalizedFlightDateUtc = toUtc(flightDateUtc);
        Optional<FlightRequest> existing = flightRequestRepositoryPort.findByFlight(
            normalizedFlightDateUtc,
            airlineCode,
            originIata,
            destIata
        );
        if (existing.isPresent()) {
            log.info("Reusing existing flight request id={} userId={}", existing.get().id(), userId);
            return existing.get();
        }
        FlightRequest flightRequest = new FlightRequest(
                null,
                userId,
                normalizedFlightDateUtc,
                airlineCode,
                originIata,
                destIata,
                distance,
                flightNumber,
                now,
                true,
                null
        );
        log.info("Creating new flight request userId={} origin={} dest={} flightNumber={}",
                userId, originIata, destIata, flightNumber);
        return flightRequestRepositoryPort.save(flightRequest);
    }

    private Prediction getOrCreatePrediction(Long flightRequestId, PredictFlightCommand command, OffsetDateTime now) {
        // Usa buckets temporales en UTC para decidir si se llama al modelo o se usa cache.
        OffsetDateTime bucketStart = resolveBucketStart(now);
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(
                flightRequestId,
                bucketStart
        );
        if (cached.isPresent()) {
            log.info("Using cached prediction id={} for flightRequestId={} bucketStart={}",
                    cached.get().id(), flightRequestId, bucketStart);
            return cached.get();
        }
        log.info("Requesting model prediction for flightRequestId={} bucketStart={}", flightRequestId, bucketStart);
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
        return predictionRepositoryPort.save(prediction);
    }

    private OffsetDateTime resolveBucketStart(OffsetDateTime timestamp) {
        // Normaliza el timestamp al inicio del bucket de 3 horas.
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
}
