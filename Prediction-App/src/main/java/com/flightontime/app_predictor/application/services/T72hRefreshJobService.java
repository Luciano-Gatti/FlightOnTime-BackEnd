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
import org.springframework.stereotype.Service;

@Service
public class T72hRefreshJobService {
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
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = now.plusHours(72);
        List<FlightFollow> follows = flightFollowRepositoryPort
                .findByRefreshModeAndFlightDateBetween(RefreshMode.T72_REFRESH, now, end);
        for (FlightFollow follow : follows) {
            FlightRequest request = flightRequestRepositoryPort.findById(follow.flightRequestId())
                    .orElse(null);
            if (request == null || !request.active()) {
                continue;
            }
            if (request.flightDateUtc().isBefore(now) || request.flightDateUtc().isAfter(end)) {
                continue;
            }
            PredictFlightCommand command = buildCommand(request);
            getOrCreatePrediction(request.id(), command, now);
        }
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

    private Prediction getOrCreatePrediction(Long flightRequestId, PredictFlightCommand command, OffsetDateTime now) {
        OffsetDateTime bucketStart = resolveBucketStart(now);
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(
                flightRequestId,
                bucketStart
        );
        if (cached.isPresent()) {
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
