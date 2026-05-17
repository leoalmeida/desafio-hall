package com.example.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.io.Serial;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa uma release de aplicação.
 */
@Entity
@Table(
    name = "RELEASE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_release_application_version_env",
            columnNames = {"application_id", "version", "env"})
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Release {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_VERSION_LENGTH = 50;
    private static final int MAX_URL_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "version", nullable = false, length = MAX_VERSION_LENGTH)
    private String version;

    @Column(name = "env", nullable = false, length = MAX_VERSION_LENGTH)
    @Enumerated(EnumType.STRING)
    private EnvironmentEnum env;

    @Column(name = "status", nullable = false, length = MAX_VERSION_LENGTH)
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @Column(name = "evidence_url", length = MAX_URL_LENGTH)
    private String evidenceUrl;

    @Version
    @Column(name = "version_row", nullable = false)
    private Integer versionRow;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "deployed_at")
    private java.time.LocalDateTime deployedAt;
}
