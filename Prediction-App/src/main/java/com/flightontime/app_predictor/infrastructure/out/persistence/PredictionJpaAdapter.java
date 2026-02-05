package com.flightontime.app_predictor.infrastructure.out.persistence;

import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionAccuracySample;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.mapper.PredictionMapper;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightPredictionEntity;
import com.flightontime.app_predictor.infrastructure.out.persistence.repository.FlightPredictionJpaRepository;
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

    /**
     * Ejecuta la operación prediction jpa adapter.
     * @param flightPredictionJpaRepository variable de entrada flightPredictionJpaRepository.
     */

    /**
     * Ejecuta la operación prediction jpa adapter.
     * @param flightPredictionJpaRepository variable de entrada flightPredictionJpaRepository.
     * @return resultado de la operación prediction jpa adapter.
     */

    public PredictionJpaAdapter(FlightPredictionJpaRepository flightPredictionJpaRepository) {
        this.flightPredictionJpaRepository = flightPredictionJpaRepository;
    }

    /**
     * Ejecuta la operación save.
     * @param prediction variable de entrada prediction.
     * @return resultado de la operación save.
     */
    @Override
    public Prediction save(Prediction prediction) {
        if (prediction == null) {
            throw new IllegalArgumentException("Prediction is required");
        }
        FlightPredictionEntity entity = resolveEntity(prediction.id());
        predictionMapper.toEntity(prediction, entity);
        return predictionMapper.toDomain(flightPredictionJpaRepository.save(entity));
    }

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */
    @Override
    public Optional<Prediction> findById(Long id) {
        return flightPredictionJpaRepository.findById(id)
                .map(predictionMapper::toDomain);
    }

    /**
     * Ejecuta la operación find by request id and forecast bucket utc.
     * @param flightRequestId variable de entrada flightRequestId.
     * @param forecastBucketUtc variable de entrada forecastBucketUtc.
     * @return resultado de la operación find by request id and forecast bucket utc.
     */
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

    /**
     * Ejecuta la operación find by request id and user id.
     * @param flightRequestId variable de entrada flightRequestId.
     * @param userId variable de entrada userId.
     * @return resultado de la operación find by request id and user id.
     */
    @Override
    public List<Prediction> findByRequestIdAndUserId(Long flightRequestId, Long userId) {
        return flightPredictionJpaRepository.findByFlightRequestIdAndUserId(flightRequestId, userId)
                .stream()
                .map(predictionMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación count all.
     * @return resultado de la operación count all.
     */
    @Override
    public long countAll() {
        return flightPredictionJpaRepository.count();
    }

    /**
     * Ejecuta la operación count by status.
     * @param predictedStatus variable de entrada predictedStatus.
     * @return resultado de la operación count by status.
     */
    @Override
    public long countByStatus(String predictedStatus) {
        return flightPredictionJpaRepository.countByPredictedStatus(predictedStatus);
    }

    /**
     * Ejecuta la operación find accuracy samples excluding cancelled.
     * @return resultado de la operación find accuracy samples excluding cancelled.
     */
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

    /**
     * Ejecuta la operación resolve entity.
     * @param id variable de entrada id.
     * @return resultado de la operación resolve entity.
     */

    private FlightPredictionEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightPredictionEntity();
        }
        return flightPredictionJpaRepository.findById(id).orElseGet(FlightPredictionEntity::new);
    }
}
