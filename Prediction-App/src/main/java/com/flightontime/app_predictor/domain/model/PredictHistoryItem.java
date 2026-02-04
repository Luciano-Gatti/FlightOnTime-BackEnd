package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro PredictHistoryItem.
 * @param flightRequestId variable de entrada flightRequestId.
 * @param flightDateUtc variable de entrada flightDateUtc.
 * @param airlineCode variable de entrada airlineCode.
 * @param originIata variable de entrada originIata.
 * @param destIata variable de entrada destIata.
 * @param flightNumber variable de entrada flightNumber.
 * @param predictedStatus variable de entrada predictedStatus.
 * @param predictedProbability variable de entrada predictedProbability.
 * @param confidence variable de entrada confidence.
 * @param thresholdUsed variable de entrada thresholdUsed.
 * @param modelVersion variable de entrada modelVersion.
 * @param predictedAt variable de entrada predictedAt.
 * @param uniqueUsersCount variable de entrada uniqueUsersCount.
 * @return resultado de la operación resultado.
 */
public record PredictHistoryItem(
        Long flightRequestId,
        OffsetDateTime flightDateUtc,
        String airlineCode,
        String originIata,
        String destIata,
        String flightNumber,
        String predictedStatus,
        Double predictedProbability,
        String confidence,
        Double thresholdUsed,
        String modelVersion,
        OffsetDateTime predictedAt,
        long uniqueUsersCount
) {
}
