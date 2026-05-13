package com.example.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa uma aprovação de release.
 */
@Entity
@Table(name = "APPROVAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_OUTCOME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "release_id", nullable = false)
    private Long releaseId;

    @Column(name = "approver_email", nullable = false, length = MAX_EMAIL_LENGTH)
    private String approverEmail;

    @Column(name = "outcome", nullable = false, length = MAX_OUTCOME_LENGTH)
    private OutcomeEnum outcome;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private java.time.LocalDateTime timestamp;
}
