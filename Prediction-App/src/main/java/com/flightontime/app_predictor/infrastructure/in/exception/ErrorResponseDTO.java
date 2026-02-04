package com.flightontime.app_predictor.infrastructure.in.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

/**
 * Ejecuta la operación error response dto.
 * @param timestamp variable de entrada timestamp.
 * @param status variable de entrada status.
 * @param error variable de entrada error.
 * @param message variable de entrada message.
 * @param path variable de entrada path.
 * @param details variable de entrada details.
 * @return resultado de la operación error response dto.
 */
/**
 * Record ErrorResponseDTO.
 *
 * <p>Responsable de error response dto.</p>
 * @param timestamp variable de entrada timestamp.
 * @param status variable de entrada status.
 * @param error variable de entrada error.
 * @param message variable de entrada message.
 * @param path variable de entrada path.
 * @param details variable de entrada details.
 * @return resultado de la operación resultado.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Object details
) {
}
