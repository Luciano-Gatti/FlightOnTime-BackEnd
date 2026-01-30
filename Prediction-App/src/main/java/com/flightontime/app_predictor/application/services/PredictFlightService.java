package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PredictFlightService implements PredictFlightUseCase {
    private final ModelPredictionPort modelPredictionPort;
    private final DistanceUseCase distanceUseCase;
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;

    public PredictFlightService(
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

    @Override
    public PredictResponseDTO predict(PredictRequestDTO request, Long userId) {
        validateRequest(request);
        double distance = distanceUseCase.calculateDistance(request.origin(), request.dest());
        PredictFlightCommand command = new PredictFlightCommand(
                request.flDate(),
                request.carrier(),
                request.origin(),
                request.dest(),
                request.flightNumber(),
                distance
        );
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (userId == null) {
            var prediction = modelPredictionPort.requestPrediction(command);
            return new PredictResponseDTO(
                    prediction.status(),
                    prediction.probability(),
                    prediction.modelVersion(),
                    now
            );
        }

        FlightRequest flightRequest = getOrCreateFlightRequest(request, userId, now);
        Prediction prediction = getOrCreatePrediction(flightRequest.id(), command, now);
        userPredictionRepositoryPort.save(new UserPrediction(null, userId, prediction.id(), now));
        return new PredictResponseDTO(
                prediction.status(),
                prediction.probability(),
                prediction.modelVersion(),
                now
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
        double distance = distanceUseCase.calculateDistance(flightRequest.origin(), flightRequest.destination());
        PredictFlightCommand command = new PredictFlightCommand(
                flightRequest.flightDate(),
                flightRequest.carrier(),
                flightRequest.origin(),
                flightRequest.destination(),
                flightRequest.flightNumber(),
                distance
        );
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Prediction prediction = getOrCreatePrediction(flightRequest.id(), command, now);
        return new PredictResponseDTO(
                prediction.status(),
                prediction.probability(),
                prediction.modelVersion(),
                now
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

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
