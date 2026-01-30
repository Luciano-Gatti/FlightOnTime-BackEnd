package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.PredictionEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.PredictionJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PredictionJpaAdapter implements PredictionRepositoryPort {
    private final PredictionJpaRepository predictionJpaRepository;
    private final PredictionMapper predictionMapper = new PredictionMapper();

    public PredictionJpaAdapter(PredictionJpaRepository predictionJpaRepository) {
        this.predictionJpaRepository = predictionJpaRepository;
    }

    @Override
    public Prediction save(Prediction prediction) {
        if (prediction == null) {
            throw new IllegalArgumentException("Prediction is required");
        }
        PredictionEntity entity = resolveEntity(prediction.id());
        predictionMapper.toEntity(prediction, entity);
        return predictionMapper.toDomain(predictionJpaRepository.save(entity));
    }

    @Override
    public Optional<Prediction> findById(Long id) {
        return predictionJpaRepository.findById(id)
                .map(predictionMapper::toDomain);
    }

    @Override
    public Optional<Prediction> findByRequestIdAndPredictedAtBetween(
            Long requestId,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        return predictionJpaRepository.findFirstByRequestIdAndPredictedAtBetweenOrderByPredictedAtDesc(
                requestId,
                start,
                end
        ).map(predictionMapper::toDomain);
    }

    @Override
    public List<Prediction> findByRequestIdAndUserId(Long requestId, Long userId) {
        return predictionJpaRepository.findByRequestIdAndUserId(requestId, userId)
                .stream()
                .map(predictionMapper::toDomain)
                .collect(Collectors.toList());
    }

    private PredictionEntity resolveEntity(Long id) {
        if (id == null) {
            return new PredictionEntity();
        }
        return predictionJpaRepository.findById(id).orElseGet(PredictionEntity::new);
    }
}
