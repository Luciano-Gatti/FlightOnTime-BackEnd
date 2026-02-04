package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.domain.model.UserAuthData;
import java.util.Optional;

/**
 * Interfaz UserRepositoryPort.
 */
public interface UserRepositoryPort {
    /**
     * Ejecuta la operación find by email.
     * @param email variable de entrada email.
     * @return resultado de la operación find by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Ejecuta la operación find auth data by email.
     * @param email variable de entrada email.
     * @return resultado de la operación find auth data by email.
     */

    Optional<UserAuthData> findAuthDataByEmail(String email);

    /**
     * Ejecuta la operación save.
     * @param user variable de entrada user.
     * @param passwordHash variable de entrada passwordHash.
     * @return resultado de la operación save.
     */

    User save(User user, String passwordHash);

    /**
     * Ejecuta la operación exists by email.
     * @param email variable de entrada email.
     * @return resultado de la operación exists by email.
     */

    boolean existsByEmail(String email);
}
