package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightPredictionEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Clase PredictionMapper.
 */
public class PredictionMapper {
    public Prediction toDomain(FlightPredictionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Prediction(
                entity.getId(),
                entity.getFlightRequestId(),
                entity.getForecastBucketUtc(),
                entity.getPredictedStatus(),
                entity.getPredictedProbability(),
                entity.getConfidence(),
                entity.getThresholdUsed(),
                entity.getModelVersion(),
                entity.getSource(),
                entity.getPredictedAt(),
                entity.getCreatedAt()
        );
    }

    public FlightPredictionEntity toEntity(Prediction prediction, FlightPredictionEntity entity) {
        if (prediction == null) {
            return entity;
        }
        FlightPredictionEntity target = entity == null ? new FlightPredictionEntity() : entity;
        target.setFlightRequestId(prediction.flightRequestId());
        target.setForecastBucketUtc(toUtc(prediction.forecastBucketUtc()));
        target.setPredictedStatus(prediction.predictedStatus());
        target.setPredictedProbability(prediction.predictedProbability());
        target.setConfidence(prediction.confidence());
        target.setThresholdUsed(prediction.thresholdUsed());
        target.setModelVersion(prediction.modelVersion());
        target.setSource(prediction.source());
        target.setPredictedAt(toUtc(prediction.predictedAt()));
        if (prediction.createdAt() != null) {
            target.setCreatedAt(toUtc(prediction.createdAt()));
        }
        return target;
    }

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
