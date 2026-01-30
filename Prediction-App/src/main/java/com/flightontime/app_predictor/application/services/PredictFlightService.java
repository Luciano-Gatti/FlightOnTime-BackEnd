package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

@Service
public class PredictFlightService implements PredictFlightUseCase {
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final PredictionWorkflowService predictionWorkflowService;

    public PredictFlightService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            PredictionWorkflowService predictionWorkflowService
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.predictionWorkflowService = predictionWorkflowService;
    }

    @Override
    public PredictResponseDTO predict(PredictRequestDTO request, Long userId) {
        validateRequest(request);
        var workflowResult = predictionWorkflowService.predict(
                request.flDate(),
                request.carrier(),
                request.origin(),
                request.dest(),
                request.flightNumber(),
                userId,
                false,
                true
        );
        if (userId != null && workflowResult.prediction() != null && workflowResult.flightRequest() != null) {
            upsertFlightFollow(
                    userId,
                    workflowResult.flightRequest().id(),
                    workflowResult.prediction().id(),
                    RefreshMode.T72_REFRESH
            );
        }
        return new PredictResponseDTO(
                workflowResult.result().status(),
                workflowResult.result().probability(),
                workflowResult.result().modelVersion(),
                workflowResult.result().predictedAt()
        );
    }

    @Override
    public PredictResponseDTO getLatestPrediction(Long requestId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        FlightRequest flightRequest = flightRequestRepositoryPort.findById(requestId)
                .filter(request -> userId.equals(request.userId()))
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        var result = predictionWorkflowService.getOrCreatePredictionForRequest(flightRequest);
        if (userId != null && result.prediction() != null) {
            upsertFlightFollow(
                    userId,
                    flightRequest.id(),
                    result.prediction().id(),
                    RefreshMode.T72_REFRESH
            );
        }
        return new PredictResponseDTO(
                result.result().status(),
                result.result().probability(),
                result.result().modelVersion(),
                result.result().predictedAt()
        );
    }

    private void validateRequest(PredictRequestDTO request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (request.flDate() == null || !request.flDate().isAfter(now)) {
            throw new IllegalArgumentException("flDate must be in the future");
        }
        if (request.origin() == null || request.origin().length() != 3) {
            throw new IllegalArgumentException("origin must be length 3");
        }
        if (request.dest() == null || request.dest().length() != 3) {
            throw new IllegalArgumentException("dest must be length 3");
        }
    }

    private FlightRequest getOrCreateFlightRequest(PredictRequestDTO request, Long userId, OffsetDateTime now) {
        OffsetDateTime flightDateUtc = toUtc(request.flDate());
        Optional<FlightRequest> existing = flightRequestRepositoryPort.findByUserAndFlight(
                userId,
                flightDateUtc,
                request.carrier(),
                request.origin(),
                request.dest(),
                request.flightNumber()
        );
        if (existing.isPresent()) {
            return existing.get();
        }
        FlightRequest flightRequest = new FlightRequest(
                null,
                userId,
                flightDateUtc,
                request.carrier(),
                request.origin(),
                request.dest(),
                request.flightNumber(),
                now,
                true,
                null
        );
        return flightRequestRepositoryPort.save(flightRequest);
    }

    private Prediction getOrCreatePrediction(Long requestId, PredictFlightCommand command, OffsetDateTime now) {
        OffsetDateTime bucketStart = resolveBucketStart(now);
        OffsetDateTime bucketEnd = bucketStart.plusHours(3);
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndPredictedAtBetween(
                requestId,
                bucketStart,
                bucketEnd
        );
        if (cached.isPresent()) {
            return cached.get();
        }
        var modelPrediction = modelPredictionPort.requestPrediction(command);
        Prediction prediction = new Prediction(
                null,
                requestId,
                modelPrediction.status(),
                modelPrediction.probability(),
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

}
