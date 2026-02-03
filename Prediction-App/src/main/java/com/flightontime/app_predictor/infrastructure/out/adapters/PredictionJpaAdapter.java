package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionAccuracySample;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightPredictionEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightPredictionJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Clase PredictionJpaAdapter.
 */
@Component
public class PredictionJpaAdapter implements PredictionRepositoryPort {
    private final FlightPredictionJpaRepository flightPredictionJpaRepository;
    private final PredictionMapper predictionMapper = new PredictionMapper();

    public PredictionJpaAdapter(FlightPredictionJpaRepository flightPredictionJpaRepository) {
        this.flightPredictionJpaRepository = flightPredictionJpaRepository;
    }

    @Override
    public Prediction save(Prediction prediction) {
        if (prediction == null) {
            throw new IllegalArgumentException("Prediction is required");
        }
        FlightPredictionEntity entity = resolveEntity(prediction.id());
        predictionMapper.toEntity(prediction, entity);
        return predictionMapper.toDomain(flightPredictionJpaRepository.save(entity));
    }

    @Override
    public Optional<Prediction> findById(Long id) {
        return flightPredictionJpaRepository.findById(id)
                .map(predictionMapper::toDomain);
    }

    @Override
    public Optional<Prediction> findByRequestIdAndForecastBucketUtc(
            Long flightRequestId,
            OffsetDateTime forecastBucketUtc
    ) {
        return flightPredictionJpaRepository.findByFlightRequestIdAndForecastBucketUtc(
                flightRequestId,
                forecastBucketUtc
        ).map(predictionMapper::toDomain);
    }

    @Override
    public List<Prediction> findByRequestIdAndUserId(Long flightRequestId, Long userId) {
        return flightPredictionJpaRepository.findByFlightRequestIdAndUserId(flightRequestId, userId)
                .stream()
                .map(predictionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return flightPredictionJpaRepository.count();
    }

    @Override
    public long countByStatus(String predictedStatus) {
        return flightPredictionJpaRepository.countByPredictedStatus(predictedStatus);
    }

    @Override
    public List<PredictionAccuracySample> findAccuracySamplesExcludingCancelled() {
        return flightPredictionJpaRepository.findAccuracySamplesExcludingCancelled()
                .stream()
                .map(view -> new PredictionAccuracySample(
                        view.getFlightDateUtc(),
                        view.getForecastBucketUtc(),
                        view.getPredictedStatus(),
                        view.getActualStatus()
                ))
                .collect(Collectors.toList());
    }

    private FlightPredictionEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightPredictionEntity();
        }
        return flightPredictionJpaRepository.findById(id).orElseGet(FlightPredictionEntity::new);
    }
}
