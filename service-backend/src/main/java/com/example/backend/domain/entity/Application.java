package com.example.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa uma aplicação.
 */
@Entity
@Table(name = "APPLICATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_TEXT_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = MAX_TEXT_LENGTH)
    private String name;

    @Column(name = "owner_team", nullable = false, length = MAX_TEXT_LENGTH)
    private String ownerTeam;

    @Column(name = "repo_url", nullable = false, length = MAX_TEXT_LENGTH)
    private String repoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;
}
