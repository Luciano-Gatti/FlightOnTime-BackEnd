package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz FlightRequestRepositoryPort.
 */
public interface FlightRequestRepositoryPort {
    /**
     * Ejecuta la operación save.
     * @param request variable de entrada request.
     * @return resultado de la operación save.
     */
    FlightRequest save(FlightRequest request);

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */

    Optional<FlightRequest> findById(Long id);

    /**
     * Ejecuta la operación find by flight.
     * @param flightDateUtc variable de entrada flightDateUtc.
     * @param airlineCode variable de entrada airlineCode.
     * @param originIata variable de entrada originIata.
     * @param destIata variable de entrada destIata.
     * @return resultado de la operación find by flight.
     */

    Optional<FlightRequest> findByFlight(
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata
    );

    /**
     * Ejecuta la operación find by user id.
     * @param userId variable de entrada userId.
     * @return resultado de la operación find by user id.
     */

    List<FlightRequest> findByUserId(Long userId);

    /**
     * Ejecuta la operación find by ids.
     * @param ids variable de entrada ids.
     * @return resultado de la operación find by ids.
     */

    List<FlightRequest> findByIds(List<Long> ids);

    /**
     * Ejecuta la operación find by flight date between with user predictions.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by flight date between with user predictions.
     */

    List<FlightRequest> findByFlightDateBetweenWithUserPredictions(
            OffsetDateTime start,
            OffsetDateTime end
    );

    /**
     * Ejecuta la operación find by flight date between without actuals.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by flight date between without actuals.
     */

    List<FlightRequest> findByFlightDateBetweenWithoutActuals(
            OffsetDateTime start,
            OffsetDateTime end
    );

    /**
     * Ejecuta la operación find by flight date before and active.
     * @param cutoff variable de entrada cutoff.
     * @return resultado de la operación find by flight date before and active.
     */

    List<FlightRequest> findByFlightDateBeforeAndActive(OffsetDateTime cutoff);
}
