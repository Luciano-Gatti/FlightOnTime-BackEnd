package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionSnapshotEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface UserPredictionSnapshotJpaRepository extends JpaRepository<UserPredictionSnapshotEntity, Long> {
    @Query("""
            select count(distinct userPrediction.userId)
            from UserPredictionSnapshotEntity userPrediction
            join FlightPredictionEntity prediction
              on userPrediction.flightPredictionId = prediction.id
            where prediction.flightRequestId = :flightRequestId
            """)
    long countDistinctUsersByRequestId(@Param("flightRequestId") Long flightRequestId);

    @Query("""
            select distinct userPrediction.userId
            from UserPredictionSnapshotEntity userPrediction
            join FlightPredictionEntity prediction
              on userPrediction.flightPredictionId = prediction.id
            where prediction.flightRequestId = :flightRequestId
            """)
    List<Long> findDistinctUserIdsByRequestId(@Param("flightRequestId") Long flightRequestId);

    @Query("""
            select userPrediction
            from UserPredictionSnapshotEntity userPrediction
            join FlightPredictionEntity prediction
              on userPrediction.flightPredictionId = prediction.id
            where userPrediction.userId = :userId
              and prediction.flightRequestId = :flightRequestId
            order by userPrediction.createdAt desc
            """)
    Optional<UserPredictionSnapshotEntity> findTopByUserIdAndFlightRequestIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("flightRequestId") Long flightRequestId
    );
}
