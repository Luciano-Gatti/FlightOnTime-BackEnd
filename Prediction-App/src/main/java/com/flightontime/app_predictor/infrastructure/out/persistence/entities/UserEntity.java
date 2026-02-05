package com.flightontime.app_predictor.infrastructure.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Clase UserEntity.
 */
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "roles", nullable = false)
    private String roles;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /**
     * Ejecuta la operación on create.
     */
    @PrePersist
    public void onCreate() {
        if (roles == null || roles.isBlank()) {
            roles = "ROLE_USER";
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    /**
     * Ejecuta la operación get id.
     * @return resultado de la operación get id.
     */

    public Long getId() {
        return id;
    }

    /**
     * Ejecuta la operación set id.
     * @param id variable de entrada id.
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Ejecuta la operación get email.
     * @return resultado de la operación get email.
     */

    public String getEmail() {
        return email;
    }

    /**
     * Ejecuta la operación set email.
     * @param email variable de entrada email.
     */

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Ejecuta la operación get password hash.
     * @return resultado de la operación get password hash.
     */

    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Ejecuta la operación set password hash.
     * @param passwordHash variable de entrada passwordHash.
     */

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Ejecuta la operación get first name.
     * @return resultado de la operación get first name.
     */

    public String getFirstName() {
        return firstName;
    }

    /**
     * Ejecuta la operación set first name.
     * @param firstName variable de entrada firstName.
     */

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Ejecuta la operación get last name.
     * @return resultado de la operación get last name.
     */

    public String getLastName() {
        return lastName;
    }

    /**
     * Ejecuta la operación set last name.
     * @param lastName variable de entrada lastName.
     */

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Ejecuta la operación get roles.
     * @return resultado de la operación get roles.
     */

    public String getRoles() {
        return roles;
    }

    /**
     * Ejecuta la operación set roles.
     * @param roles variable de entrada roles.
     */

    public void setRoles(String roles) {
        this.roles = roles;
    }

    /**
     * Ejecuta la operación get created at.
     * @return resultado de la operación get created at.
     */

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ejecuta la operación set created at.
     * @param createdAt variable de entrada createdAt.
     */

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
