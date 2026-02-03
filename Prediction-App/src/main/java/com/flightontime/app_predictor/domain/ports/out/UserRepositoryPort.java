package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.domain.model.UserAuthData;
import java.util.Optional;

/**
 * Interfaz UserRepositoryPort.
 */
public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);

    Optional<UserAuthData> findAuthDataByEmail(String email);

    User save(User user, String passwordHash);

    boolean existsByEmail(String email);
}
