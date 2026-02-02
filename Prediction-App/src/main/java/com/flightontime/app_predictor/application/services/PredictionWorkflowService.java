package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionResult;
import com.flightontime.app_predictor.domain.model.UserPrediction;
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

@Service
public class PredictionWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(PredictionWorkflowService.class);
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
            return new PredictionWorkflowResult(null, null, result);
        }
        FlightRequest flightRequest = getOrCreateFlightRequest(
                userId,
                flightDateUtc,
                airlineCode,
                originIata,
                destIata,
                flightNumber,
                now
        );
        log.info("Resolved flight request id={} userId={}", flightRequest.id(), userId);
        Prediction prediction = getOrCreatePrediction(flightRequest.id(), command, now);
        log.info("Resolved prediction id={} for flightRequestId={}", prediction.id(), flightRequest.id());
        if (createUserPrediction && userId != null) {
            userPredictionRepositoryPort.save(new UserPrediction(null, userId, prediction.id(), now));
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
            OffsetDateTime now
    ) {
        OffsetDateTime normalizedFlightDateUtc = toUtc(flightDateUtc);
        Optional<FlightRequest> existing = flightRequestRepositoryPort.findByUserAndFlight(
                userId,
                normalizedFlightDateUtc,
                airlineCode,
                originIata,
                destIata,
                flightNumber
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
        OffsetDateTime bucketStart = resolveBucketStart(now);
        OffsetDateTime bucketEnd = bucketStart.plusHours(3);
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndPredictedAtBetween(
                flightRequestId,
                bucketStart,
                bucketEnd
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
                modelPrediction.predictedStatus(),
                modelPrediction.predictedProbability(),
                modelPrediction.confidence(),
                modelPrediction.thresholdUsed(),
                modelPrediction.modelVersion(),
                now,
                now
        );
        return predictionRepositoryPort.save(prediction);
    }

    private OffsetDateTime resolveBucketStart(OffsetDateTime timestamp) {
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
