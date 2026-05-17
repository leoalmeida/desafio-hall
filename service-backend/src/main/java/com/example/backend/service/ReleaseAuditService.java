package com.example.backend.service;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseApprovalDecisionRequestDto;
import com.example.backend.dto.ReleaseEvidenceUpdateRequestDto;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.SecurityContextUtils;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Fachada de auditoria para ações de release.
 */
@Service
public class ReleaseAuditService {

    private final AuditLogManager auditLogManager;

    public ReleaseAuditService(final AuditLogManager auditLogManager) {
        this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager não pode ser nulo");
    }

    /**
     * Registra auditoria de busca de releases.
     */
    public void logFind(
            final UUID applicationId,
            final String version,
            final EnvironmentEnum environment,
            final StatusEnum status) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "FIND",
                "Release",
                null,
                String.format(
                    "{applicationId:%s, version:%s, environment:%s, status:%s}",
                        applicationId,
                        version,
                        environment,
                        status));
    }

    /**
     * Registra auditoria de criação de release.
     */
    public void logCreate(final ReleaseRequestDto dto) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "CREATE",
                "Release",
                null,
                auditLogManager.toJsonNode(dto));
    }

    /**
     * Registra auditoria de aprovação.
     */
    public void logApprove(final UUID id, final ReleaseApprovalDecisionRequestDto dto) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "APPROVE",
                "Release",
                id.toString(),
                auditLogManager.toJsonNode(
                    String.format("{releaseId:%s, notes:%s}", id, dto == null ? null : dto.getNotes())));
    }

    /**
     * Registra auditoria de reprovação.
     */
    public void logDisapprove(final UUID id, final ReleaseApprovalDecisionRequestDto dto) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "DISAPPROVE",
                "Release",
                id.toString(),
                auditLogManager.toJsonNode(
                    String.format("{releaseId:%s, notes:%s}", id, dto == null ? null : dto.getNotes())));
    }

    /**
     * Registra auditoria de alteração da evidence URL.
     */
    public void logChangeEvidenceUrl(final UUID id, final ReleaseEvidenceUpdateRequestDto dto) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "CHANGE_EVIDENCE_URL",
                "Release",
                id.toString(),
                auditLogManager.toJsonNode(dto));
    }

    /**
     * Registra auditoria de cálculo de score de evidência.
     */
    public void logEvidenceScore(final UUID id) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "EVIDENCE_SCORE",
                "Release",
                id.toString(),
                auditLogManager.toJsonNode("ReleaseId", id.toString()));
    }
}
