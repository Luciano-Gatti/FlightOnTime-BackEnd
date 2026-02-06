package com.flightspredictor.flights.domain.service.prediction;

import com.flightspredictor.flights.domain.entities.Airport;
import com.flightspredictor.flights.domain.repository.AirportRepository;
import com.flightspredictor.flights.domain.service.airports.AirportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de consulta para aeropuertos.
 *
 * Define operaciones de solo lectura relacionadas con aeropuertos,
 * sin exponer detalles de persistencia ni implementación.
 *
 * Esta interfaz permite validar la existencia de aeropuertos
 * a partir de su código IATA.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class  AirportLookupService {

    private final AirportRepository airportRepository;
    private final AirportService airportService;
    @Value("${app.debug.airport-lookup-trace:false}")
    private boolean airportLookupTraceEnabled;

    /**
     * Verifica si existe un aeropuerto registrado con el código IATA indicado.
     *
     * @param iata código IATA del aeropuerto (3 letras, ej. "JFK")
     * @return true si el aeropuerto existe, false en caso contrario
     */
    @Transactional
    public boolean existsAirportIata(String iata){
        return airportRepository.existsByAirportIataIgnoreCase(iata);
    };

    @Transactional
    public void getAirportExist(String origin, String dest) {
        Optional<Airport> originAirport = airportRepository.findByAirportIata(origin);
        logAirportLookupTrace(normalizeIata(origin), originAirport.isPresent() ? "DB" : "EXTERNAL_FETCH");
        originAirport.orElseGet(() -> airportRepository.save(airportService.getAirport(origin)));

        Optional<Airport> destAirport = airportRepository.findByAirportIata(dest);
        logAirportLookupTrace(normalizeIata(dest), destAirport.isPresent() ? "DB" : "EXTERNAL_FETCH");
        destAirport.orElseGet(() -> airportRepository.save(airportService.getAirport(dest)));
    }

    public Optional<Airport> getAirport(String iata) {
        Optional<Airport> airport = airportRepository.findByAirportIata(iata);
        logAirportLookupTrace(normalizeIata(iata), airport.isPresent() ? "DB" : "EXTERNAL_FETCH");
        return airport;
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
        return className.equals(AirportLookupService.class.getName())
                || className.equals(AirportService.class.getName())
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
