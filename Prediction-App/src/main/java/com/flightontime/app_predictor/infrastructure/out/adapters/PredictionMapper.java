package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightPredictionEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class PredictionMapper {
    public Prediction toDomain(FlightPredictionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Prediction(
                entity.getId(),
                entity.getRequestId(),
                entity.getStatus(),
                entity.getProbability(),
                entity.getModelVersion(),
                entity.getPredictedAt(),
                entity.getCreatedAt()
        );
    }

    public FlightPredictionEntity toEntity(Prediction prediction, FlightPredictionEntity entity) {
        if (prediction == null) {
            return entity;
        }
        FlightPredictionEntity target = entity == null ? new FlightPredictionEntity() : entity;
        target.setRequestId(prediction.requestId());
        target.setStatus(prediction.status());
        target.setProbability(prediction.probability());
        target.setModelVersion(prediction.modelVersion());
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
