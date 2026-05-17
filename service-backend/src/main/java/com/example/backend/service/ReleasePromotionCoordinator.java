package com.example.backend.service;

import com.example.backend.security.AuditLogManager;
import com.example.backend.security.SecurityContextUtils;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Coordena promoção de release com idempotência e auditoria.
 */
@Service
public class ReleasePromotionCoordinator {

    private static final String PROMOTE_OPERATION = "PROMOTE_RELEASE";

    private final ReleaseService releaseService;
    private final AuditLogManager auditLogManager;
    private final IdempotencyService idempotencyService;

    public ReleasePromotionCoordinator(
            final ReleaseService releaseService,
            final AuditLogManager auditLogManager,
            final IdempotencyService idempotencyService) {
        this.releaseService = Objects.requireNonNull(releaseService, "releaseService não pode ser nulo");
        this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager não pode ser nulo");
        this.idempotencyService = Objects.requireNonNull(idempotencyService, "idempotencyService não pode ser nulo");
    }

    /**
     * Executa a promoção com suporte a Idempotency-Key.
     *
     * @param id identificador da release
     * @param idempotencyKey chave de idempotência opcional
     */
    public void promote(final UUID id, final String idempotencyKey) {
        if (hasIdempotencyKey(idempotencyKey)) {
            IdempotencyResult result = idempotencyService.begin(
                    PROMOTE_OPERATION,
                    idempotencyKey,
                    id,
                    buildPromoteRequestHash(id));
            if (!result.shouldProcess()) {
                handleDuplicate(result);
                return;
            }
        }

        try {
            releaseService.promoteRelease(id);
            auditPromote(id);
            completeIdempotencyIfNeeded(idempotencyKey);
        } catch (Exception ex) {
            abortIdempotencyIfNeeded(idempotencyKey);
            throw ex;
        }
    }

    private void handleDuplicate(final IdempotencyResult result) {
        if (result.state() == IdempotencyState.COMPLETED) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key já está em processamento");
    }

    private String buildPromoteRequestHash(final UUID id) {
        return "releaseId=" + id;
    }

    private void auditPromote(final UUID id) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "PROMOTE",
                "Release",
                id.toString(),
                auditLogManager.toJsonNode("ReleaseId", id.toString()));
    }

    private void completeIdempotencyIfNeeded(final String idempotencyKey) {
        if (hasIdempotencyKey(idempotencyKey)) {
            idempotencyService.complete(PROMOTE_OPERATION, idempotencyKey, HttpStatus.NO_CONTENT.value());
        }
    }

    private void abortIdempotencyIfNeeded(final String idempotencyKey) {
        if (hasIdempotencyKey(idempotencyKey)) {
            idempotencyService.abort(PROMOTE_OPERATION, idempotencyKey);
        }
    }

    private boolean hasIdempotencyKey(final String idempotencyKey) {
        return idempotencyKey != null && !idempotencyKey.isBlank();
    }
}
