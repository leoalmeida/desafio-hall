package com.example.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registro de deduplicação por Idempotency-Key.
 */
@Entity
@Table(
        name = "IDEMPOTENCY_RECORD",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_idempotency_operation_key",
                    columnNames = {"operation_name", "idempotency_key"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    private static final int MAX_OPERATION_LENGTH = 100;
    private static final int MAX_KEY_LENGTH = 255;
    private static final int MAX_HASH_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "operation_name", nullable = false, length = MAX_OPERATION_LENGTH)
    private String operationName;

    @Column(name = "idempotency_key", nullable = false, length = MAX_KEY_LENGTH)
    private String idempotencyKey;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "request_hash", nullable = false, length = MAX_HASH_LENGTH)
    private String requestHash;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "completed", nullable = false)
    private Boolean completed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
