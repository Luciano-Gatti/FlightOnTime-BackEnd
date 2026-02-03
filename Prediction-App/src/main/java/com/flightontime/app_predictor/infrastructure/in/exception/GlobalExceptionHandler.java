package com.flightontime.app_predictor.infrastructure.in.exception;

import com.flightontime.app_predictor.domain.exception.AirportNotFoundException;
import com.flightontime.app_predictor.domain.exception.BusinessException;
import com.flightontime.app_predictor.domain.exception.DomainException;
import com.flightontime.app_predictor.domain.exception.ExternalApiException;
import com.flightontime.app_predictor.infrastructure.out.adapters.WeatherProviderException;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAirportNotFound(
            AirportNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

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

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalApi(
            ExternalApiException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "External service unavailable", request, null);
    }

    @ExceptionHandler(WeatherProviderException.class)
    public ResponseEntity<ErrorResponseDTO> handleWeatherProvider(
            WeatherProviderException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Weather provider unavailable", request, null);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponseDTO> handleWebClientResponse(
            WebClientResponseException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "Upstream service error", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, null);
    }

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
