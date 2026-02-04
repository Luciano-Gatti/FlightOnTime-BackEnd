package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.BulkPredictError;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Clase BulkPredictRowValidator.
 */
@Component
public class BulkPredictRowValidator {
    private static final Pattern AIRLINE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2}$");
    private static final Pattern IATA_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final int MIN_COLUMNS = 5;

    /**
     * Ejecuta la operación validate.
     * @param row variable de entrada row.
     * @param startTimestamp variable de entrada startTimestamp.
     * @return resultado de la operación validate.
     */
    public ValidationResult validate(CsvParser.CsvRow row, OffsetDateTime startTimestamp) {
        List<String> fields = row.fields();
        if (fields == null || fields.size() < MIN_COLUMNS) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "Expected at least 5 columns",
                    row.rawRow()
            ));
        }

        String flightDateRaw = normalizeToken(fields.get(0), false);
        String airlineCode = normalizeToken(fields.get(1), true);
        String originIata = normalizeToken(fields.get(2), true);
        String destIata = normalizeToken(fields.get(3), true);
        String flightNumber = normalizeToken(fields.get(4), false);

        if (flightDateRaw == null) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "fl_date_utc is required",
                    row.rawRow()
            ));
        }

        OffsetDateTime flightDate;
        try {
            flightDate = OffsetDateTime.parse(flightDateRaw);
        } catch (DateTimeParseException ex) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "fl_date_utc must be ISO-8601 with UTC offset",
                    row.rawRow()
            ));
        }

        if (!ZoneOffset.UTC.equals(flightDate.getOffset())) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "fl_date_utc must be in UTC (offset Z)",
                    row.rawRow()
            ));
        }

        OffsetDateTime normalizedFlightDate = flightDate.withOffsetSameInstant(ZoneOffset.UTC);
        if (!normalizedFlightDate.isAfter(startTimestamp)) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "fl_date_utc must be in the future",
                    row.rawRow()
            ));
        }

        if (airlineCode == null || airlineCode.isBlank()) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "carrier is required",
                    row.rawRow()
            ));
        }

        if (hasInternalWhitespace(airlineCode) || !AIRLINE_CODE_PATTERN.matcher(airlineCode).matches()) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "carrier must be 2 uppercase IATA characters",
                    row.rawRow()
            ));
        }

        if (originIata == null || originIata.isBlank()) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "origin is required",
                    row.rawRow()
            ));
        }

        if (hasInternalWhitespace(originIata) || !IATA_CODE_PATTERN.matcher(originIata).matches()) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "origin must be a 3-letter IATA code",
                    row.rawRow()
            ));
        }

        if (destIata == null || destIata.isBlank()) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "dest is required",
                    row.rawRow()
            ));
        }

        if (hasInternalWhitespace(destIata) || !IATA_CODE_PATTERN.matcher(destIata).matches()) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "dest must be a 3-letter IATA code",
                    row.rawRow()
            ));
        }

        if (originIata.equalsIgnoreCase(destIata)) {
            return ValidationResult.error(new BulkPredictError(
                    row.rowNumber(),
                    "origin and dest cannot be the same",
                    row.rawRow()
            ));
        }

        return ValidationResult.valid(new ValidatedRow(
                normalizedFlightDate,
                airlineCode,
                originIata,
                destIata,
                normalizeFlightNumber(flightNumber),
                row.rawRow(),
                row.rowNumber()
        ));
    }

    private String normalizeToken(String value, boolean upperCase) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            return null;
        }
        return upperCase ? normalized.toUpperCase() : normalized;
    }

    private String normalizeFlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            return null;
        }
        return flightNumber;
    }

    private boolean hasInternalWhitespace(String value) {
        return value != null && value.chars().anyMatch(Character::isWhitespace);
    }

    /**
     * Registro ValidationResult.
     * @param row variable de entrada row.
     * @param error variable de entrada error.
     * @return resultado de la operación resultado.
     */
    public record ValidationResult(ValidatedRow row, BulkPredictError error) {
        static ValidationResult valid(ValidatedRow row) {
            return new ValidationResult(row, null);
        }

        static ValidationResult error(BulkPredictError error) {
            return new ValidationResult(null, error);
        }

        boolean hasError() {
            return error != null;
        }
    }

    /**
     * Registro ValidatedRow.
     * @param flightDateUtc variable de entrada flightDateUtc.
     * @param airlineCode variable de entrada airlineCode.
     * @param originIata variable de entrada originIata.
     * @param destIata variable de entrada destIata.
     * @param flightNumber variable de entrada flightNumber.
     * @param rawRow variable de entrada rawRow.
     * @param rowNumber variable de entrada rowNumber.
     * @return resultado de la operación resultado.
     */
    public record ValidatedRow(
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata,
            String flightNumber,
            String rawRow,
            int rowNumber
    ) {
    }
}
