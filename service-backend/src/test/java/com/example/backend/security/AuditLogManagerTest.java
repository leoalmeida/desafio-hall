package com.example.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;
import com.example.backend.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuditLogManagerTest {

    private static final UUID AUDIT_LOG_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Mock
    private AuditLogService auditLogService;


    private AuditLogManager manager;

    @BeforeEach
    void setUp() {
        manager = new AuditLogManager(auditLogService, new ObjectMapper());
    }

    @Test
    void logActionDeveEncaminharDadosParaServico() {
        when(auditLogService.create(any(AuditLogRequestDto.class)))
            .thenReturn(AuditLogResponseDto.builder().id(AUDIT_LOG_ID).build());

        manager.logAction("actor@email.com", "LOGIN", "AppUser", "42", "payload");

        ArgumentCaptor<AuditLogRequestDto> captor = ArgumentCaptor.forClass(AuditLogRequestDto.class);
        verify(auditLogService).create(captor.capture());

        AuditLogRequestDto dto = captor.getValue();
        assertEquals("actor@email.com", dto.getActor());
        assertEquals("LOGIN", dto.getAction());
        assertEquals("AppUser", dto.getEntity());
        assertEquals("42", dto.getEntityId());
        assertEquals("payload", dto.getPayload());
    }

    @Test
    void logActionDevePermitirCamposOpcionaisNulos() {
        when(auditLogService.create(any(AuditLogRequestDto.class)))
            .thenReturn(AuditLogResponseDto.builder()
                .id(UUID.fromString("30000000-0000-0000-0000-000000000002"))
                .build());

        manager.logAction("actor@email.com", "READ", "Application", null, null);

        ArgumentCaptor<AuditLogRequestDto> captor = ArgumentCaptor.forClass(AuditLogRequestDto.class);
        verify(auditLogService).create(captor.capture());

        AuditLogRequestDto dto = captor.getValue();
        assertNull(dto.getEntityId());
        assertNull(dto.getPayload());
    }
}
