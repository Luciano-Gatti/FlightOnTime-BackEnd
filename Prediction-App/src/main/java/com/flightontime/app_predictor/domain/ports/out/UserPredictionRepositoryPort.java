package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.model.FlightRequestPopularity;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz UserPredictionRepositoryPort.
 */
public interface UserPredictionRepositoryPort {
    /**
     * Ejecuta la operación save.
     * @param userPrediction variable de entrada userPrediction.
     * @return resultado de la operación save.
     */
    UserPrediction save(UserPrediction userPrediction);

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */

    Optional<UserPrediction> findById(Long id);

    /**
     * Ejecuta la operación count distinct users by request id.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación count distinct users by request id.
     */

    long countDistinctUsersByRequestId(Long flightRequestId);

    /**
     * Ejecuta la operación find distinct user ids by request id.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación find distinct user ids by request id.
     */

    List<Long> findDistinctUserIdsByRequestId(Long flightRequestId);

    /**
     * Ejecuta la operación find latest by user id and request id.
     * @param userId variable de entrada userId.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación find latest by user id and request id.
     */

    Optional<UserPrediction> findLatestByUserIdAndRequestId(Long userId, Long flightRequestId);

    /**
     * Ejecuta la operación find top request popularity.
     * @param topN variable de entrada topN.
     * @return resultado de la operación find top request popularity.
     */

    List<FlightRequestPopularity> findTopRequestPopularity(int topN);
}
