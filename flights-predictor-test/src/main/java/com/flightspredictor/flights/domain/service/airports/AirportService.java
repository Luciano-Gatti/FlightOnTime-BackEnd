package com.flightspredictor.flights.domain.service.airports;

import com.flightspredictor.flights.infra.external.airports.client.AirportApiClient;
import com.flightspredictor.flights.domain.dto.airports.AirportData;
import com.flightspredictor.flights.domain.entities.Airport;
import com.flightspredictor.flights.domain.error.AirportNotFoundException;
import com.flightspredictor.flights.domain.repository.AirportRepository;
import com.flightspredictor.flights.domain.validations.IAirportsValidations;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AirportService {

    private final AirportApiClient apiClient;
    private final AirportRepository repository;
    private final List<IAirportsValidations> validations;
    private final boolean airportLookupTraceEnabled;

    public AirportService(AirportApiClient apiClient,
                          AirportRepository repository,
                          List<IAirportsValidations> validations,
                          @Value("${app.debug.airport-lookup-trace:false}") boolean airportLookupTraceEnabled) {
        this.apiClient = apiClient;
        this.repository = repository;
        this.validations = validations;
        this.airportLookupTraceEnabled = airportLookupTraceEnabled;
    }

      /**
     * Verifica si existe un aeropuerto registrado con el código IATA.
     *
     * La búsqueda se realiza ignorando mayúsculas y minúsculas.
     *
     * @param iata código IATA del aeropuerto
     * @return true si existe, false en caso contrario
     */
    public boolean existsAirportIata(String iata) {
        return repository.existsByAirportIataIgnoreCase(iata);
    }


    public Airport getAirport (String iata) {
        String normalizedIata = normalizeIata(iata);

        // Busca primero el aeropuerto en la base de datos si ya existe
        Optional<Airport> existingAirport = repository.findByAirportIata(normalizedIata);
        logAirportLookupTrace(normalizedIata, existingAirport.isPresent() ? "DB" : "EXTERNAL_FETCH");
        if (existingAirport.isPresent()) {
            return existingAirport.get();
        }

        // Si no existe, trae los datos de la API
        var apiResponse = apiClient.airportResponse(iata);

        if (apiResponse == null) {
            throw new AirportNotFoundException(
                    "Aeropuerto no encontrado con código IATA: " + iata
            );
        }

        var data = new AirportData(apiResponse);
        log.info("Airport API response for iata {}: {}", iata, apiResponse);
        log.info("AirportData built from API response for iata {}: {}", iata, data);

        // Se aplican las validaciones para los campos necesarios para el modelo
        validations.forEach(v -> v.validate(data));

        // Guarda en la base de datos y devuelve la información.
        var airport = new Airport(data);
        return repository.save(airport);
    }

    private void logAirportLookupTrace(String normalizedIata, String source) {
        if (!airportLookupTraceEnabled) {
            return;
        }
        String correlationId = MDC.get("correlationId");
        String caller = resolveCaller();
        log.debug("AirportLookup correlationId={} iata={} source={} caller={}",
                correlationId, normalizedIata, source, caller);
    }

    private String resolveCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (shouldSkipFrame(className)) {
                continue;
            }
            return className + "." + element.getMethodName();
        }
        return "UNKNOWN";
    }

    private boolean shouldSkipFrame(String className) {
        return className.equals(AirportService.class.getName())
                || className.equals(AirportRepository.class.getName())
                || className.startsWith("java.")
                || className.startsWith("org.springframework.")
                || className.startsWith("org.hibernate.")
                || className.startsWith("jakarta.");
    }

    private String normalizeIata(String iata) {
        if (iata == null) {
            return null;
        }
        return iata.trim().toUpperCase();
    }

}
