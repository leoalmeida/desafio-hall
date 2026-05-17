package com.example.backend.domain.entity;

import java.io.Serial;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um registro de auditoria.
 */
@Entity
@Table(name = "AUDITLOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_TEXT_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor", nullable = false, length = MAX_TEXT_LENGTH)
    private String actor;

    @Column(name = "action", nullable = false, length = MAX_TEXT_LENGTH)
    private String action;

    @Column(name = "entity", nullable = false, length = MAX_TEXT_LENGTH)
    private String entity;

    @Column(name = "entity_id", length = MAX_TEXT_LENGTH)
    private String entityId;

    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
