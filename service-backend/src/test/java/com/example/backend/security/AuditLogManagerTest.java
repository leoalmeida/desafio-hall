package com.example.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.domain.entity.AuditLog;
import com.example.backend.domain.repository.AuditLogRepository;
import com.example.backend.mapper.AuditLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

/**
 * Testes para AuditLogManager.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogManagerTest")
class AuditLogManagerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogManager auditLogManager;

    private String actor;
    private String action;
    private String entity;
    private String details;
    private Integer entityId;

    @BeforeEach
    void setUp() {
        actor = "user@example.com";
        action = "CREATE_APPLICATION";
        entity = "Application";
        entityId = 1;
        details = "Created application TestApp";
    }

    @Test
    @DisplayName("logAction deve criar e salvar um log de auditoria")
    void testLogActionSuccess() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            return log;
        });

        auditLogManager.logAction(actor, action, entity, entityId, details);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog capturedLog = captor.getValue();
        assertEquals(actor, capturedLog.getActor());
        assertEquals(action, capturedLog.getAction());
        assertEquals(entity, capturedLog.getEntity());
        assertEquals(entityId.longValue(), capturedLog.getEntityId());
        assertEquals(details, capturedLog.getDetails());
    }

    @Test
    @DisplayName("logAction com detalhes null deve funcionar")
    void testLogActionWithNullDetails() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogManager.logAction(actor, action, entity, entityId, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog capturedLog = captor.getValue();
        assertNull(capturedLog.getDetails());
    }

    @Test
    @DisplayName("logAction com entityId zero deve funcionar")
    void testLogActionWithZeroEntityId() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogManager.logAction(actor, action, entity, 0, details);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog capturedLog = captor.getValue();
        assertEquals(0, capturedLog.getEntityId());
    }

    @Test
    @DisplayName("logAction deve incluir timestamp")
    void testLogActionIncludesTimestamp() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogManager.logAction(actor, action, entity, entityId, details);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog capturedLog = captor.getValue();
        assertNotNull(capturedLog.getTimestamp());
    }

    @Test
    @DisplayName("logAction com actor vazio deve funcionar")
    void testLogActionWithEmptyActor() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogManager.logAction("", action, entity, entityId, details);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("logAction com action vazio deve funcionar")
    void testLogActionWithEmptyAction() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogManager.logAction(actor, "", entity, entityId, details);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("logAction deve chamar repository exatamente uma vez")
    void testLogActionCallsRepositoryOnce() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogManager.logAction(actor, action, entity, entityId, details);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        verifyNoMoreInteractions(auditLogRepository);
    }

    @Test
    @DisplayName("logAction com múltiplos chamados deve salvar cada um")
    void testLogActionMultipleCalls() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogManager.logAction(actor, action, entity, entityId, details);
        auditLogManager.logAction(actor, "DELETE_APPLICATION", entity, 2, "Deleted application");

        verify(auditLogRepository, times(2)).save(any(AuditLog.class));
    }
}
