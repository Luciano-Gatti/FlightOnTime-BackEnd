package com.flightspredictor.flights.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightspredictor.flights.domain.entities.Airport;

/**
 * 🚨 NO USAR DIRECTAMENTE 🚨
 *
 * Todas las consultas a aeropuertos deben pasar por AirportService
 * para asegurar normalización de IATA y conteo de lookup.
 */
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByAirportIata(String iata);
    
    /**
     * Verifica si existe un aeropuerto con el código IATA proporcionado.
     *
     * El método es interpretado automáticamente por Spring Data JPA
     * a partir de su nombre (Query Method).
     *
     * - existsBy → retorna true o false
     * - AirportIata → campo de la entidad
     * - IgnoreCase → ignora mayúsculas y minúsculas
     *
     * @param airportIata código IATA del aeropuerto (3 letras)
     * @return true si el aeropuerto existe en la base de datos,
     *         false en caso contrario
     */
    boolean existsByAirportIataIgnoreCase(String airportIata);
}
