package com.flightontime.app_predictor.infrastructure.security;

import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidad para resolver el usuario autenticado desde el contexto de seguridad.
 */
@Component
public class SecurityUserContext {

    /**
     * Obtiene el userId actual como Optional.
     *
     * @return Optional con userId cuando el principal es un subject numérico.
     */
    public Optional<Long> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String subject) || subject.isBlank()) {
            return Optional.empty();
        }
        if ("anonymousUser".equalsIgnoreCase(subject)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(subject));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene el userId actual o null si no existe.
     *
     * @return userId actual o null.
     */
    public Long currentUserIdOrNull() {
        return currentUserId().orElse(null);
    }
}

