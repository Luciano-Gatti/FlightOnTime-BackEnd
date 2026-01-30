package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
