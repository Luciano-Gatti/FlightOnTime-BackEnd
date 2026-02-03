package com.flightontime.app_predictor.application.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase CsvParser.
 */
public class CsvParser {
/**
 * Registro CsvRow.
 */
    public record CsvRow(int rowNumber, String rawRow, List<String> fields) {
    }

/**
 * Registro CsvParseError.
 */
    public record CsvParseError(int rowNumber, String message, String rawRow) {
    }

/**
 * Registro CsvParseResult.
 */
    public record CsvParseResult(List<String> header, List<CsvRow> rows, List<CsvParseError> errors) {
    }

    public CsvParseResult parse(InputStream inputStream) {
        List<String> headerFields = new ArrayList<>();
        List<CsvRow> rows = new ArrayList<>();
        List<CsvParseError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Missing CSV header");
            }
            if (!headerLine.isBlank()) {
                headerFields.addAll(parseLine(headerLine));
            }
            if (headerFields.isEmpty()) {
                throw new IllegalArgumentException("Missing CSV header");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != headerFields.size()) {
                    errors.add(new CsvParseError(
                            lineNumber,
                            "Expected " + headerFields.size() + " columns but got " + fields.size(),
                            line
                    ));
                    continue;
                }
                rows.add(new CsvRow(lineNumber, line, fields));
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read CSV input", ex);
        }

        return new CsvParseResult(headerFields, rows, errors);
    }

    private List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int index = 0;
        while (index < line.length()) {
            char ch = line.charAt(index);
            if (ch == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index += 2;
                    continue;
                }
                inQuotes = !inQuotes;
                index++;
                continue;
            }
            if (ch == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
                index++;
                continue;
            }
            current.append(ch);
            index++;
        }
        fields.add(current.toString().trim());
        return fields;
    }
}
