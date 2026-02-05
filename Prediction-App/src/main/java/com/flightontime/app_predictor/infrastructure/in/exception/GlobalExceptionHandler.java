package com.flightontime.app_predictor.infrastructure.in.exception;

import com.flightontime.app_predictor.domain.exception.AirportNotFoundException;
import com.flightontime.app_predictor.domain.exception.BusinessException;
import com.flightontime.app_predictor.domain.exception.DomainException;
import com.flightontime.app_predictor.domain.exception.ExternalApiException;
import com.flightontime.app_predictor.application.exception.WeatherProviderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Class GlobalExceptionHandler.
 *
 * <p>Responsable de global exception handler.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Ejecuta la operación handle method argument not valid.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle method argument not valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        /**
         * Ejecuta la operación build response.
         * @param HttpStatus.BAD_REQUEST variable de entrada HttpStatus.BAD_REQUEST.
         * @param error" variable de entrada error".
         * @param request variable de entrada request.
         * @param details variable de entrada details.
         * @return resultado de la operación build response.
         */
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", request, details);
    }

    /**
     * Ejecuta la operación handle constraint violation.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle constraint violation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        /**
         * Ejecuta la operación build response.
         * @param HttpStatus.BAD_REQUEST variable de entrada HttpStatus.BAD_REQUEST.
         * @param message variable de entrada message.
         * @param request variable de entrada request.
         * @param null variable de entrada null.
         * @return resultado de la operación build response.
         */
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
    }

    /**
     * Ejecuta la operación handle airport not found.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle airport not found.
     */
    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAirportNotFound(
            AirportNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /**
     * Ejecuta la operación handle business exception.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle business exception.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        Map<String, String> details = new HashMap<>();
        if (ex.getCode() != null) {
            details.put("code", ex.getCode());
        }
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, details.isEmpty() ? null : details);
    }

    /**
     * Ejecuta la operación handle domain exception.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle domain exception.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponseDTO> handleDomainException(
            DomainException ex,
            HttpServletRequest request
    ) {
        Map<String, String> details = new HashMap<>();
        if (ex.getCode() != null) {
            details.put("code", ex.getCode());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, details.isEmpty() ? null : details);
    }

    /**
     * Ejecuta la operación handle external api.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle external api.
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalApi(
            ExternalApiException ex,
            HttpServletRequest request
    ) {
        /**
         * Ejecuta la operación build response.
         * @param HttpStatus.BAD_GATEWAY variable de entrada HttpStatus.BAD_GATEWAY.
         * @param unavailable" variable de entrada unavailable".
         * @param request variable de entrada request.
         * @param null variable de entrada null.
         * @return resultado de la operación build response.
         */
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "External service unavailable", request, null);
    }

    /**
     * Ejecuta la operación handle weather provider.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle weather provider.
     */
    @ExceptionHandler(WeatherProviderException.class)
    public ResponseEntity<ErrorResponseDTO> handleWeatherProvider(
            WeatherProviderException ex,
            HttpServletRequest request
    ) {
        /**
         * Ejecuta la operación build response.
         * @param HttpStatus.SERVICE_UNAVAILABLE variable de entrada HttpStatus.SERVICE_UNAVAILABLE.
         * @param unavailable" variable de entrada unavailable".
         * @param request variable de entrada request.
         * @param null variable de entrada null.
         * @return resultado de la operación build response.
         */
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Weather provider unavailable", request, null);
    }

    /**
     * Ejecuta la operación handle web client response.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle web client response.
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponseDTO> handleWebClientResponse(
            WebClientResponseException ex,
            HttpServletRequest request
    ) {
        /**
         * Ejecuta la operación build response.
         * @param HttpStatus.BAD_GATEWAY variable de entrada HttpStatus.BAD_GATEWAY.
         * @param error" variable de entrada error".
         * @param request variable de entrada request.
         * @param null variable de entrada null.
         * @return resultado de la operación build response.
         */
        return buildResponse(HttpStatus.BAD_GATEWAY, "Upstream service error", request, null);
    }

    /**
     * Ejecuta la operación handle unexpected.
     * @param ex variable de entrada ex.
     * @param request variable de entrada request.
     * @return resultado de la operación handle unexpected.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        /**
         * Ejecuta la operación build response.
         * @param HttpStatus.INTERNAL_SERVER_ERROR variable de entrada HttpStatus.INTERNAL_SERVER_ERROR.
         * @param error" variable de entrada error".
         * @param request variable de entrada request.
         * @param null variable de entrada null.
         * @return resultado de la operación build response.
         */
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, null);
    }

    /**
     * Ejecuta la operación build response.
     * @param status variable de entrada status.
     * @param message variable de entrada message.
     * @param request variable de entrada request.
     * @param details variable de entrada details.
     * @return resultado de la operación build response.
     */

    private ResponseEntity<ErrorResponseDTO> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Object details
    ) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request != null ? request.getRequestURI() : null,
                details
        );
        return ResponseEntity.status(status).body(response);
    }
}
