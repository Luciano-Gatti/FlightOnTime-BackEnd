package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface UserPredictionJpaRepository extends JpaRepository<UserPredictionEntity, Long> {
    @Query("""
            select count(distinct userPrediction.userId)
            from UserPredictionEntity userPrediction
            join PredictionEntity prediction
              on userPrediction.predictionId = prediction.id
            where prediction.requestId = :requestId
            """)
    long countDistinctUsersByRequestId(@Param("requestId") Long requestId);

    @Query("""
            select distinct userPrediction.userId
            from UserPredictionEntity userPrediction
            join PredictionEntity prediction
              on userPrediction.predictionId = prediction.id
            where prediction.requestId = :requestId
            """)
    List<Long> findDistinctUserIdsByRequestId(@Param("requestId") Long requestId);

    @Query("""
            select userPrediction
            from UserPredictionEntity userPrediction
            join PredictionEntity prediction
              on userPrediction.predictionId = prediction.id
            where userPrediction.userId = :userId
              and prediction.requestId = :requestId
            order by userPrediction.createdAt desc
            """)
    Optional<UserPredictionEntity> findTopByUserIdAndRequestIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("requestId") Long requestId
    );
}
