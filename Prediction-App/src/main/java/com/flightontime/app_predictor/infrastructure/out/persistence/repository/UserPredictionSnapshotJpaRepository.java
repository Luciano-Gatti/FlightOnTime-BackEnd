package com.flightontime.app_predictor.infrastructure.out.persistence.repository;

import com.flightontime.app_predictor.infrastructure.out.persistence.entities.UserPredictionSnapshotEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

/**
 * Interfaz UserPredictionSnapshotJpaRepository.
 */
public interface UserPredictionSnapshotJpaRepository extends JpaRepository<UserPredictionSnapshotEntity, Long> {
    @Query("""
            select count(distinct userPrediction.userId)
            from UserPredictionSnapshotEntity userPrediction
            where userPrediction.flightRequestId = :flightRequestId
            """)
    long countDistinctUsersByRequestId(@Param("flightRequestId") Long flightRequestId);

    @Query("""
            select distinct userPrediction.userId
            from UserPredictionSnapshotEntity userPrediction
            where userPrediction.flightRequestId = :flightRequestId
            """)
    List<Long> findDistinctUserIdsByRequestId(@Param("flightRequestId") Long flightRequestId);

    @Query("""
            select userPrediction
            from UserPredictionSnapshotEntity userPrediction
            where userPrediction.userId = :userId
              and userPrediction.flightRequestId = :flightRequestId
            order by userPrediction.createdAt desc
            """)
    Optional<UserPredictionSnapshotEntity> findTopByUserIdAndFlightRequestIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("flightRequestId") Long flightRequestId
    );

    @Query("""
            select userPrediction.flightRequestId, count(distinct userPrediction.userId)
            from UserPredictionSnapshotEntity userPrediction
            group by userPrediction.flightRequestId
            order by count(distinct userPrediction.userId) desc
            """)
    /**
     * Ejecuta la operación find top request popularity.
     * @param pageable variable de entrada pageable.
     * @return resultado de la operación find top request popularity.
     */
    List<Object[]> findTopRequestPopularity(Pageable pageable);
}
