package com.flightspredictor.flights.domain.service.prediction;

import com.flightspredictor.flights.domain.entities.Airport;
import com.flightspredictor.flights.domain.service.airports.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
public class AirportLookupService {

    private final AirportService airportService;

    /**
     * Verifica si existe un aeropuerto registrado con el código IATA indicado.
     *
     * @param iata código IATA del aeropuerto (3 letras, ej. "JFK")
     * @return true si el aeropuerto existe, false en caso contrario
     */
    @Transactional
    public boolean existsAirportIata(String iata){
        return airportService.existsAirportIata(iata);
    }

    @Transactional
    public Airport getAirport(String iata) {
        return airportService.getOrFetchByIata(iata);
    }
}
