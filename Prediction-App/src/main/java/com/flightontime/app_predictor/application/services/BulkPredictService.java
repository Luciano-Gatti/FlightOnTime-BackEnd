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
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase BulkPredictService.
 */
@Service
public class BulkPredictService implements BulkPredictUseCase {
    private static final Logger log = LoggerFactory.getLogger(BulkPredictService.class);
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
    private final BulkPredictRowValidator rowValidator;

    /**
     * Construye el servicio de importación masiva por CSV.
     *
     * @param predictionWorkflowService servicio que orquesta predicciones y cache.
     * @param flightFollowRepositoryPort repositorio de seguimientos de vuelo.
     * @param userPredictionRepositoryPort repositorio de snapshots de predicción por usuario.
     */
    public BulkPredictService(
            PredictionWorkflowService predictionWorkflowService,
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort,
            CsvParser csvParser,
            BulkPredictRowValidator rowValidator
    ) {
        this.predictionWorkflowService = predictionWorkflowService;
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
        this.csvParser = csvParser;
        this.rowValidator = rowValidator;
    }

    @Override
    /**
     * Importa predicciones desde un CSV, validando filas y creando suscripciones.
     *
     * @param inputStream stream del CSV.
     * @param userId identificador del usuario que realiza la carga.
     * @param dryRun indica si se valida sin persistir ni llamar al modelo.
     * @return resultado con conteo de aceptados, rechazados y errores.
     */
    public BulkPredictResult importPredictionsFromCsv(InputStream inputStream, Long userId, boolean dryRun) {
        long useCaseStartMs = UseCaseLogSupport.start(
                log,
                "BulkPredictService.importPredictionsFromCsv",
                userId,
                "dryRun=" + dryRun
        );
        try {
            List<BulkPredictError> errors = new ArrayList<>();
            int accepted = 0;
            int rejected = 0;
            OffsetDateTime startTimestamp = OffsetDateTime.now(ZoneOffset.UTC);
            int cacheHits = 0;
            int modelCalls = 0;
            int subscriptionsCreated = 0;
            int unexpectedErrors = 0;

            CsvParser.CsvParseResult parseResult = csvParser.parse(inputStream);
            int totalRows = parseResult.rows().size() + parseResult.errors().size();
            log.info("Starting CSV import userId={} totalRows={} dryRun={}", userId, totalRows, dryRun);
            if (!EXPECTED_HEADER.equals(parseResult.header())) {
                throw new IllegalArgumentException(
                        "Invalid CSV header. Expected: " + formatHeader(EXPECTED_HEADER)
                                + " Received: " + formatHeader(parseResult.header())
                );
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
            try {
                BulkPredictRowValidator.ValidationResult validation = rowValidator.validate(row, startTimestamp);
                if (validation.hasError()) {
                    rejected++;
                    errors.add(validation.error());
                    continue;
                }
                BulkPredictRowValidator.ValidatedRow validatedRow = validation.row();
                if (!dryRun) {
                    OffsetDateTime flightDate = validatedRow.flightDateUtc();
                    String flightNumber = validatedRow.flightNumber();
                    var workflowResult = predictionWorkflowService.predict(
                            flightDate,
                            validatedRow.airlineCode(),
                            validatedRow.originIata(),
                            validatedRow.destIata(),
                            flightNumber,
                            userId,
                            true,
                            false
                    );
                    if (workflowResult.prediction() != null && workflowResult.prediction().createdAt() != null) {
                        boolean cacheHit = workflowResult.prediction().createdAt().isBefore(startTimestamp);
                        if (cacheHit) {
                            cacheHits++;
                        } else {
                            modelCalls++;
                        }
                        log.debug("CSV prediction resolved flight_request_id={} bucketStart={} cacheHit={}",
                                workflowResult.flightRequest() != null ? workflowResult.flightRequest().id() : null,
                                workflowResult.prediction().forecastBucketUtc(),
                                cacheHit);
                    }
                    if (userId != null && workflowResult.prediction() != null
                            && workflowResult.flightRequest() != null) {
                        UserPrediction snapshot = resolveUserSnapshot(
                                userId,
                                workflowResult.flightRequest().id(),
                                workflowResult.prediction().id(),
                                UserPredictionSource.CSV_IMPORT,
                                startTimestamp
                        );
                        if (upsertFlightFollow(
                                userId,
                                workflowResult.flightRequest().id(),
                                snapshot.id(),
                                RefreshMode.T12_ONLY
                        )) {
                            subscriptionsCreated++;
                        }
                    }
                }
                accepted++;
            } catch (Exception ex) {
                rejected++;
                unexpectedErrors++;
                errors.add(new BulkPredictError(
                        row.rowNumber(),
                        buildUnexpectedErrorMessage(ex),
                        row.rawRow()
                ));
                log.error("Unexpected error processing CSV row userId={} rowNumber={}",
                        userId,
                        row.rowNumber(),
                        ex);
            }
        }

            log.info("Finished CSV import userId={} totalRows={} accepted={} rejected={} modelCalls={} cacheHits={} "
                            + "subscriptionsCreated={} unexpectedErrors={}",
                    userId,
                    totalRows,
                    accepted,
                    rejected,
                    modelCalls,
                    cacheHits,
                    subscriptionsCreated,
                    unexpectedErrors);
            BulkPredictResult result = new BulkPredictResult(accepted, rejected, errors);
            UseCaseLogSupport.end(
                    log,
                    "BulkPredictService.importPredictionsFromCsv",
                    userId,
                    useCaseStartMs,
                    "accepted=" + accepted + ", rejected=" + rejected + ", errors=" + errors.size()
            );
            return result;
        } catch (Exception ex) {
            UseCaseLogSupport.fail(log, "BulkPredictService.importPredictionsFromCsv", userId, useCaseStartMs, ex);
            throw ex;
        }
    }

    private String formatHeader(List<String> header) {
        if (header == null || header.isEmpty()) {
            return "<empty>";
        }
        return String.join(",", header);
    }

    private String buildUnexpectedErrorMessage(Exception ex) {
        if (ex == null) {
            return "Unexpected error processing row";
        }
        String type = ex.getClass().getSimpleName();
        return "Unexpected error processing row: " + type;
    }

    /**
     * Inserta o actualiza un seguimiento de vuelo para habilitar notificaciones T-12h.
     *
     * @param userId identificador del usuario.
     * @param flightRequestId identificador de la solicitud de vuelo.
     * @param snapshotId snapshot base asociado a la predicción.
     * @param refreshMode modo de refresco configurado.
     * @return true si se creó un seguimiento nuevo.
     */
    private boolean upsertFlightFollow(
            Long userId,
            Long flightRequestId,
            Long snapshotId,
            RefreshMode refreshMode
    ) {
        boolean created = false;
        // Guarda la suscripción para habilitar el flujo de notificaciones posteriores.
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
        if (flightFollow.id() == null) {
            created = true;
        }
        flightFollowRepositoryPort.save(flightFollow);
        return created;
    }

    /**
     * Resuelve (o crea) el snapshot de predicción del usuario.
     *
     * @param userId identificador del usuario.
     * @param flightRequestId identificador de la solicitud de vuelo.
     * @param flightPredictionId identificador de la predicción.
     * @param source origen del snapshot (CSV, usuario, etc.).
     * @param now timestamp de creación.
     * @return snapshot existente si coincide o uno nuevo persistido.
     */
    private UserPrediction resolveUserSnapshot(
            Long userId,
            Long flightRequestId,
            Long flightPredictionId,
            UserPredictionSource source,
            OffsetDateTime now
    ) {
        // Evita duplicar snapshots cuando la predicción no cambió.
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

    /**
     * Determina el snapshot base, priorizando el existente si está presente.
     *
     * @param existingBaselineSnapshotId snapshot base ya registrado.
     * @param snapshotId snapshot actual a usar si no hay base previa.
     * @return id del snapshot base definitivo.
     */
    private Long resolveBaselineSnapshotId(Long existingBaselineSnapshotId, Long snapshotId) {
        return existingBaselineSnapshotId == null ? snapshotId : existingBaselineSnapshotId;
    }
}
