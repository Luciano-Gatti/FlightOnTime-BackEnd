package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class T72hRefreshJobService {
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final ModelPredictionPort modelPredictionPort;
    private final DistanceUseCase distanceUseCase;

    public T72hRefreshJobService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            ModelPredictionPort modelPredictionPort,
            DistanceUseCase distanceUseCase
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.modelPredictionPort = modelPredictionPort;
        this.distanceUseCase = distanceUseCase;
    }

    public void refreshPredictions() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = now.plusHours(72);
        List<FlightRequest> requests = flightRequestRepositoryPort
                .findByFlightDateBetweenWithUserPredictions(now, end);
        for (FlightRequest request : requests) {
            if (closeIfExpired(request, now)) {
                continue;
            }
            PredictFlightCommand command = buildCommand(request);
            getOrCreatePrediction(request.id(), command, now);
        }
    }

    private PredictFlightCommand buildCommand(FlightRequest request) {
        double distance = distanceUseCase.calculateDistance(request.originIata(), request.destIata());
        return new PredictFlightCommand(
                toUtc(request.flightDateUtc()),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                distance
        );
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
            return cached.get();
        }
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

    private boolean closeIfExpired(FlightRequest request, OffsetDateTime nowUtc) {
        if (request == null || request.flightDateUtc() == null || !request.active()) {
            return true;
        }
        if (request.flightDateUtc().isBefore(nowUtc)) {
            FlightRequest closedRequest = new FlightRequest(
                    request.id(),
                    request.userId(),
                    request.flightDateUtc(),
                    request.airlineCode(),
                    request.originIata(),
                    request.destIata(),
                    request.flightNumber(),
                    request.createdAt(),
                    false,
                    nowUtc
            );
            flightRequestRepositoryPort.save(closedRequest);
            return true;
        }
        return false;
    }
}
