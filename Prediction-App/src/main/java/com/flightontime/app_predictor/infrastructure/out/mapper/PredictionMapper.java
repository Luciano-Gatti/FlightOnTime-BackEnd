package com.flightontime.app_predictor.infrastructure.out.mapper;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightPredictionEntity;
import static com.flightontime.app_predictor.infrastructure.common.time.UtcTimes.toUtc;

/**
 * Clase PredictionMapper.
 */
public class PredictionMapper {
    /**
     * Ejecuta la operación to domain.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to domain.
     */
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

    /**
     * Ejecuta la operación to entity.
     * @param prediction variable de entrada prediction.
     * @param entity variable de entrada entity.
     * @return resultado de la operación to entity.
     */

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

}
