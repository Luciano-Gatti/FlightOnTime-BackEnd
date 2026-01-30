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
    public Optional<Prediction> findByRequestIdAndPredictedAtBetween(
            Long requestId,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        return flightPredictionJpaRepository.findFirstByRequestIdAndPredictedAtBetweenOrderByPredictedAtDesc(
                requestId,
                start,
                end
        ).map(predictionMapper::toDomain);
    }

    @Override
    public List<Prediction> findByRequestIdAndUserId(Long requestId, Long userId) {
        return flightPredictionJpaRepository.findByRequestIdAndUserId(requestId, userId)
                .stream()
                .map(predictionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return flightPredictionJpaRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        return flightPredictionJpaRepository.countByStatus(status);
    }

    @Override
    public List<PredictionAccuracySample> findAccuracySamplesExcludingCancelled() {
        return flightPredictionJpaRepository.findAccuracySamplesExcludingCancelled()
                .stream()
                .map(view -> new PredictionAccuracySample(
                        view.getFlightDate(),
                        view.getPredictedAt(),
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
