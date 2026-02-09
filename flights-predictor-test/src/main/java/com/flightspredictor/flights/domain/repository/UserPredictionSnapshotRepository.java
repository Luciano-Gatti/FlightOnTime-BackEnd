package com.flightspredictor.flights.domain.repository;

import com.flightspredictor.flights.domain.entities.UserPredictionSnapshot;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPredictionSnapshotRepository extends JpaRepository<UserPredictionSnapshot, Long> {
    boolean existsByUserIdAndFlightPredictionId(Long userId, Long flightPredictionId);

    @EntityGraph(attributePaths = {"flightRequest", "flightPrediction"})
    @Query(
            value = """
                    select s from UserPredictionSnapshot s
                    join s.flightRequest r
                    join s.flightPrediction p
                    where s.user.id = :userId
                      and (:fromDate is null or r.flightDateUtc >= :fromDate)
                      and (:toDate is null or r.flightDateUtc <= :toDate)
                      and (:origin is null or r.originIata = :origin)
                      and (:dest is null or r.destIata = :dest)
                      and (:status is null or p.predictedStatus = :status)
                    """,
            countQuery = """
                    select count(s) from UserPredictionSnapshot s
                    join s.flightRequest r
                    join s.flightPrediction p
                    where s.user.id = :userId
                      and (:fromDate is null or r.flightDateUtc >= :fromDate)
                      and (:toDate is null or r.flightDateUtc <= :toDate)
                      and (:origin is null or r.originIata = :origin)
                      and (:dest is null or r.destIata = :dest)
                      and (:status is null or p.predictedStatus = :status)
                    """
    )
    Page<UserPredictionSnapshot> findHistory(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("origin") String origin,
            @Param("dest") String dest,
            @Param("status") PredictedStatus status,
            Pageable pageable
    );
}
