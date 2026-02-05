package com.flightontime.app_predictor.infrastructure.out.persistence;

import com.flightontime.app_predictor.domain.model.FlightRequestPopularity;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.mapper.UserPredictionMapper;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.UserPredictionSnapshotEntity;
import com.flightontime.app_predictor.infrastructure.out.persistence.repository.UserPredictionSnapshotJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Clase UserPredictionJpaAdapter.
 */
@Component
public class UserPredictionJpaAdapter implements UserPredictionRepositoryPort {
    private final UserPredictionSnapshotJpaRepository userPredictionSnapshotJpaRepository;
    private final UserPredictionMapper userPredictionMapper = new UserPredictionMapper();

    /**
     * Ejecuta la operación user prediction jpa adapter.
     * @param userPredictionSnapshotJpaRepository variable de entrada userPredictionSnapshotJpaRepository.
     */

    /**
     * Ejecuta la operación user prediction jpa adapter.
     * @param userPredictionSnapshotJpaRepository variable de entrada userPredictionSnapshotJpaRepository.
     * @return resultado de la operación user prediction jpa adapter.
     */

    public UserPredictionJpaAdapter(UserPredictionSnapshotJpaRepository userPredictionSnapshotJpaRepository) {
        this.userPredictionSnapshotJpaRepository = userPredictionSnapshotJpaRepository;
    }

    /**
     * Ejecuta la operación save.
     * @param userPrediction variable de entrada userPrediction.
     * @return resultado de la operación save.
     */
    @Override
    public UserPrediction save(UserPrediction userPrediction) {
        if (userPrediction == null) {
            throw new IllegalArgumentException("User prediction is required");
        }
        UserPredictionSnapshotEntity entity = resolveEntity(userPrediction.id());
        userPredictionMapper.toEntity(userPrediction, entity);
        return userPredictionMapper.toDomain(userPredictionSnapshotJpaRepository.save(entity));
    }

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */
    @Override
    public Optional<UserPrediction> findById(Long id) {
        return userPredictionSnapshotJpaRepository.findById(id)
                .map(userPredictionMapper::toDomain);
    }

    /**
     * Ejecuta la operación count distinct users by request id.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación count distinct users by request id.
     */
    @Override
    public long countDistinctUsersByRequestId(Long flightRequestId) {
        return userPredictionSnapshotJpaRepository.countDistinctUsersByRequestId(flightRequestId);
    }

    /**
     * Ejecuta la operación find distinct user ids by request id.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación find distinct user ids by request id.
     */
    @Override
    public List<Long> findDistinctUserIdsByRequestId(Long flightRequestId) {
        return userPredictionSnapshotJpaRepository.findDistinctUserIdsByRequestId(flightRequestId);
    }

    /**
     * Ejecuta la operación find latest by user id and request id.
     * @param userId variable de entrada userId.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación find latest by user id and request id.
     */
    @Override
    public Optional<UserPrediction> findLatestByUserIdAndRequestId(Long userId, Long flightRequestId) {
        return userPredictionSnapshotJpaRepository.findTopByUserIdAndFlightRequestIdOrderByCreatedAtDesc(
                userId,
                flightRequestId
        )
                .map(userPredictionMapper::toDomain);
    }

    /**
     * Ejecuta la operación find top request popularity.
     * @param topN variable de entrada topN.
     * @return resultado de la operación find top request popularity.
     */
    @Override
    public List<FlightRequestPopularity> findTopRequestPopularity(int topN) {
        if (topN <= 0) {
            return List.of();
        }
        List<Object[]> rows = userPredictionSnapshotJpaRepository.findTopRequestPopularity(PageRequest.of(0, topN));
        return rows.stream()
                .map(row -> new FlightRequestPopularity((Long) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación resolve entity.
     * @param id variable de entrada id.
     * @return resultado de la operación resolve entity.
     */

    private UserPredictionSnapshotEntity resolveEntity(Long id) {
        if (id == null) {
            return new UserPredictionSnapshotEntity();
        }
        return userPredictionSnapshotJpaRepository.findById(id).orElseGet(UserPredictionSnapshotEntity::new);
    }
}
