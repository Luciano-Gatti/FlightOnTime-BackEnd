package com.flightontime.app_predictor.infrastructure.security;

import com.flightontime.app_predictor.infrastructure.out.entities.UserEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.UserJpaRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Clase CustomUserDetailsService.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserJpaRepository userJpaRepository;

    /**
     * Ejecuta la operación custom user details service.
     * @param userJpaRepository variable de entrada userJpaRepository.
     */

    /**
     * Ejecuta la operación custom user details service.
     * @param userJpaRepository variable de entrada userJpaRepository.
     * @return resultado de la operación custom user details service.
     */

    public CustomUserDetailsService(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * Ejecuta la operación load user by username.
     * @param email variable de entrada email.
     * @return resultado de la operación load user by username.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        return new User(user.getEmail(), user.getPasswordHash(), resolveAuthorities(user.getRoles()));
    }

    /**
     * Ejecuta la operación resolve authorities.
     * @param roles variable de entrada roles.
     * @return resultado de la operación resolve authorities.
     */

    private List<GrantedAuthority> resolveAuthorities(String roles) {
        if (roles == null || roles.isBlank()) {
            return List.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
