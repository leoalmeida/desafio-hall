package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.exception.BusinessException;
import com.example.backend.security.AuditLogManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReleasePromotionCoordinatorTest {

    private static final UUID RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Mock
    private ReleaseService releaseService;

    @Mock
    private AuditLogManager auditLogManager;

    @Mock
    private IdempotencyService idempotencyService;

    private ReleasePromotionCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new ReleasePromotionCoordinator(releaseService, auditLogManager, idempotencyService);
    }

    @Test
    void promoteDeveExecutarFluxoCompletoQuandoIdempotencyKeyForNova() {
        when(idempotencyService.begin("PROMOTE_RELEASE", "key-1", RELEASE_ID, "releaseId=" + RELEASE_ID))
                .thenReturn(new IdempotencyResult(IdempotencyState.NEW, null));

        assertDoesNotThrow(() -> coordinator.promote(RELEASE_ID, "key-1"));

        verify(releaseService).promoteRelease(RELEASE_ID);
        verify(auditLogManager).logAction(any(), eq("PROMOTE"), eq("Release"), eq(RELEASE_ID.toString()), any());
        verify(idempotencyService).complete("PROMOTE_RELEASE", "key-1", 204);
    }

    @Test
    void promoteDeveIgnorarReplayConcluido() {
        when(idempotencyService.begin("PROMOTE_RELEASE", "key-2", RELEASE_ID, "releaseId=" + RELEASE_ID))
                .thenReturn(new IdempotencyResult(IdempotencyState.COMPLETED, 204));

        assertDoesNotThrow(() -> coordinator.promote(RELEASE_ID, "key-2"));

        verify(releaseService, never()).promoteRelease(RELEASE_ID);
        verify(auditLogManager, never()).logAction(any(), any(), any(), any(), any());
    }

    @Test
    void promoteDeveRetornarConflitoQuandoChaveEstiverEmProcessamento() {
        when(idempotencyService.begin("PROMOTE_RELEASE", "key-3", RELEASE_ID, "releaseId=" + RELEASE_ID))
                .thenReturn(new IdempotencyResult(IdempotencyState.IN_PROGRESS, null));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> coordinator.promote(RELEASE_ID, "key-3"));

        assertNotNull(ex);
        verify(releaseService, never()).promoteRelease(RELEASE_ID);
    }

    @Test
    void promoteDeveAbortarRegistroQuandoFalhar() {
        when(idempotencyService.begin("PROMOTE_RELEASE", "key-4", RELEASE_ID, "releaseId=" + RELEASE_ID))
                .thenReturn(new IdempotencyResult(IdempotencyState.NEW, null));
        doThrow(new BusinessException("falhou"))
                .when(releaseService)
            .promoteRelease(RELEASE_ID);

        BusinessException ex = assertThrows(BusinessException.class, () -> coordinator.promote(RELEASE_ID, "key-4"));

        assertNotNull(ex);
        verify(idempotencyService).abort("PROMOTE_RELEASE", "key-4");
        verify(idempotencyService, never()).complete("PROMOTE_RELEASE", "key-4", 204);
    }
}
