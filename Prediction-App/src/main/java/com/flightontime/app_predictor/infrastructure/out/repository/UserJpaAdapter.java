package com.flightontime.app_predictor.infrastructure.out.repository;

import com.flightontime.app_predictor.domain.model.User;
import com.flightontime.app_predictor.domain.ports.out.UserRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.UserEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Clase UserJpaAdapter.
 */
@Component
public class UserJpaAdapter implements UserRepositoryPort {
    private final UserJpaRepository userJpaRepository;

    public UserJpaAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public User save(User user, String passwordHash) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (user.email() == null || user.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.firstName() == null || user.firstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.lastName() == null || user.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        UserEntity entity = resolveEntity(user.id());
        entity.setEmail(user.email());
        entity.setFirstName(user.firstName());
        entity.setLastName(user.lastName());
        entity.setRoles(resolveRoles(user.roles()));

        OffsetDateTime createdAt = user.createdAt();
        if (createdAt == null && entity.getCreatedAt() == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (createdAt != null) {
            entity.setCreatedAt(createdAt.withOffsetSameInstant(ZoneOffset.UTC));
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            if (entity.getPasswordHash() == null || entity.getPasswordHash().isBlank()) {
                throw new IllegalArgumentException("Password hash is required");
            }
        } else {
            entity.setPasswordHash(passwordHash);
        }

        return toDomain(userJpaRepository.save(entity));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    private UserEntity resolveEntity(Long id) {
        if (id == null) {
            return new UserEntity();
        }
        return userJpaRepository.findById(id).orElseGet(UserEntity::new);
    }

    private String resolveRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return "ROLE_USER";
        }
        return roles;
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getRoles(),
                entity.getCreatedAt()
        );
    }
}
