package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.BulkPredictError;
import com.flightontime.app_predictor.application.dto.BulkPredictResult;
import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.model.UserPredictionSource;
import com.flightontime.app_predictor.domain.ports.in.BulkPredictUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.CsvParser;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BulkPredictService implements BulkPredictUseCase {
    private static final List<String> EXPECTED_HEADER = List.of(
            "fl_date_utc",
            "carrier",
            "origin",
            "dest",
            "flight_number"
    );

    private final PredictionWorkflowService predictionWorkflowService;
    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;
    private final CsvParser csvParser;

    public BulkPredictService(
            PredictionWorkflowService predictionWorkflowService,
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort
    ) {
        this.predictionWorkflowService = predictionWorkflowService;
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
        this.csvParser = new CsvParser();
    }

    @Override
    public BulkPredictResult importPredictionsFromCsv(InputStream inputStream, Long userId, boolean dryRun) {
        List<BulkPredictError> errors = new ArrayList<>();
        int accepted = 0;
        int rejected = 0;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        CsvParser.CsvParseResult parseResult = csvParser.parse(inputStream);
        if (!EXPECTED_HEADER.equals(parseResult.header())) {
            throw new IllegalArgumentException("Invalid CSV header. Expected: " + String.join(",", EXPECTED_HEADER));
        }

        for (CsvParser.CsvParseError parseError : parseResult.errors()) {
            rejected++;
            errors.add(new BulkPredictError(
                    parseError.rowNumber(),
                    parseError.message(),
                    parseError.rawRow()
            ));
        }

        for (CsvParser.CsvRow row : parseResult.rows()) {
            List<String> fields = row.fields();
            String flightDateRaw = fields.get(0);
            String airlineCode = fields.get(1);
            String originIata = fields.get(2);
            String destIata = fields.get(3);
            String flightNumber = fields.get(4);

            OffsetDateTime flightDate;
            try {
                flightDate = OffsetDateTime.parse(flightDateRaw);
            } catch (DateTimeParseException ex) {
                rejected++;
                errors.add(new BulkPredictError(
                        row.rowNumber(),
                        "fl_date_utc must be ISO-8601 with UTC offset",
                        row.rawRow()
                ));
                continue;
            }

            if (!ZoneOffset.UTC.equals(flightDate.getOffset())) {
                rejected++;
                errors.add(new BulkPredictError(
                        row.rowNumber(),
                        "fl_date_utc must be in UTC (offset Z)",
                        row.rawRow()
                ));
                continue;
            }
            if (!flightDate.isAfter(now)) {
                rejected++;
                errors.add(new BulkPredictError(
                        row.rowNumber(),
                        "fl_date_utc must be in the future",
                        row.rawRow()
                ));
                continue;
            }
            if (airlineCode.isBlank()) {
                rejected++;
                errors.add(new BulkPredictError(
                        row.rowNumber(),
                        "carrier is required",
                        row.rawRow()
                ));
                continue;
            }
            if (originIata.length() != 3) {
                rejected++;
                errors.add(new BulkPredictError(
                        row.rowNumber(),
                        "origin must be length 3",
                        row.rawRow()
                ));
                continue;
            }
            if (destIata.length() != 3) {
                rejected++;
                errors.add(new BulkPredictError(
                        row.rowNumber(),
                        "dest must be length 3",
                        row.rawRow()
                ));
                continue;
            }

            if (!dryRun) {
                var workflowResult = predictionWorkflowService.predict(
                        flightDate,
                        airlineCode,
                        originIata,
                        destIata,
                        flightNumber.isBlank() ? null : flightNumber,
                        userId,
                        true,
                        false
                );
                if (userId != null && workflowResult.prediction() != null && workflowResult.flightRequest() != null) {
                    UserPrediction snapshot = resolveUserSnapshot(
                            userId,
                            workflowResult.flightRequest().id(),
                            workflowResult.prediction().id(),
                            UserPredictionSource.CSV_IMPORT,
                            now
                    );
                    upsertFlightFollow(
                            userId,
                            workflowResult.flightRequest().id(),
                            snapshot.id(),
                            RefreshMode.T12_ONLY
                    );
                }
            }
            accepted++;
        }

        return new BulkPredictResult(accepted, rejected, errors);
    }

    private void upsertFlightFollow(
            Long userId,
            Long flightRequestId,
            Long snapshotId,
            RefreshMode refreshMode
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        FlightFollow flightFollow = flightFollowRepositoryPort
                .findByUserIdAndFlightRequestId(userId, flightRequestId)
                .map(existing -> new FlightFollow(
                        existing.id(),
                        userId,
                        flightRequestId,
                        refreshMode,
                        resolveBaselineSnapshotId(existing.baselineSnapshotId(), snapshotId),
                        existing.createdAt(),
                        now
                ))
                .orElseGet(() -> new FlightFollow(
                        null,
                        userId,
                        flightRequestId,
                        refreshMode,
                        snapshotId,
                        now,
                        now
                ));
        flightFollowRepositoryPort.save(flightFollow);
    }

    private UserPrediction resolveUserSnapshot(
            Long userId,
            Long flightRequestId,
            Long flightPredictionId,
            UserPredictionSource source,
            OffsetDateTime now
    ) {
        return userPredictionRepositoryPort.findLatestByUserIdAndRequestId(userId, flightRequestId)
                .filter(existing -> flightPredictionId.equals(existing.flightPredictionId()))
                .orElseGet(() -> userPredictionRepositoryPort.save(new UserPrediction(
                        null,
                        userId,
                        flightRequestId,
                        flightPredictionId,
                        source,
                        now
                )));
    }

    private Long resolveBaselineSnapshotId(Long existingBaselineSnapshotId, Long snapshotId) {
        return existingBaselineSnapshotId == null ? snapshotId : existingBaselineSnapshotId;
    }
}
