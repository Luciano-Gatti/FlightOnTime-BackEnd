package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.AirportDTO;
import com.flightontime.app_predictor.domain.exception.ExternalApiException;
import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.http.ExternalProviderException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase AirportService.
 */
@Service
public class AirportService {
    private static final Logger log = LoggerFactory.getLogger(AirportService.class);
    private final AirportRepositoryPort airportRepositoryPort;
    private final AirportInfoPort airportInfoPort;

    /**
     * Construye el servicio de aeropuertos.
     *
     * @param airportRepositoryPort repositorio local de aeropuertos.
     * @param airportInfoPort puerto de consulta externa de aeropuertos.
     */
    public AirportService(AirportRepositoryPort airportRepositoryPort, AirportInfoPort airportInfoPort) {
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
    }

    /**
     * Obtiene un aeropuerto por IATA, consultando fuente externa si no existe localmente.
     *
     * @param airportIata IATA del aeropuerto.
     * @return DTO con los datos del aeropuerto.
     */
    public AirportDTO getAirportByIata(String airportIata) {
        long startMs = UseCaseLogSupport.start(
                log,
                "AirportService.getAirportByIata",
                null,
                "iata=" + airportIata
        );
        try {
            String normalizedIata = normalizeIata(airportIata);
            Airport airport = airportRepositoryPort.findByIata(normalizedIata)
                    .orElseGet(() -> fetchAndStoreAirport(normalizedIata));
            AirportDTO result = AirportDTO.fromDomain(airport);
            UseCaseLogSupport.end(
                    log,
                    "AirportService.getAirportByIata",
                    null,
                    startMs,
                    "iata=" + normalizedIata + ", found=true"
            );
            return result;
        } catch (Exception ex) {
            UseCaseLogSupport.fail(log, "AirportService.getAirportByIata", null, startMs, ex);
            throw ex;
        }
    }

    /**
     * Consulta un aeropuerto en fuente externa y lo persiste localmente.
     *
     * @param airportIata IATA normalizado.
     * @return aeropuerto encontrado o guardado.
     */
    private Airport fetchAndStoreAirport(String airportIata) {
        log.info("Airport not present in DB, fetching from external provider iata={}", airportIata);

        try {
            Airport airport = airportInfoPort.findByIata(airportIata)
                    .orElseThrow(() -> new AirportNotFoundException("Airport not found: " + airportIata));
            return airportRepositoryPort.save(airport);
        } catch (ExternalProviderException ex) {
            throw new ExternalApiException("Airport provider unavailable", ex);
        }
    }

    /**
     * Normaliza un IATA (trim y uppercase) y valida su longitud.
     *
     * @param airportIata IATA original.
     * @return IATA normalizado.
     */
    private String normalizeIata(String airportIata) {
        if (airportIata == null) {
            throw new IllegalArgumentException("iata is required");
        }
        String normalized = airportIata.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) throw new IllegalArgumentException("iata is required");
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("iata must not contain whitespace");
        }
        
        validateIata(normalized);
        return normalized;
    }

    /**
     * Valida que el IATA tenga longitud 3.
     *
     * @param airportIata IATA a validar.
     */
    private void validateIata(String airportIata) {
        if (airportIata.length() != 3) {
            throw new IllegalArgumentException("iata must be length 3");
        }
    }

}
