package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.infrastructure.out.entities.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaz UserJpaRepository.
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    /**
     * Ejecuta la operación find by email.
     * @param email variable de entrada email.
     * @return resultado de la operación find by email.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Ejecuta la operación exists by email.
     * @param email variable de entrada email.
     * @return resultado de la operación exists by email.
     */

    boolean existsByEmail(String email);
}
