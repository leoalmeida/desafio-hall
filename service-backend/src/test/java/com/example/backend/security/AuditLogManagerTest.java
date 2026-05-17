package com.example.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.domain.repository.AuditLogRepository;
import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;
import com.example.backend.service.AuditLogService;
import com.example.backend.service.impl.AuditLogServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuditLogManagerTest {

    @Mock
    private AuditLogRepository repository;

    private AuditLogService auditLogService;

    private AuditLogManager manager;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogServiceImpl(repository);
        manager = new AuditLogManager(auditLogService, new ObjectMapper());
    }

    @Test
    void logActionDeveEncaminharDadosParaServico() {
        when(auditLogService.create(any(AuditLogRequestDto.class))).thenReturn(AuditLogResponseDto.builder().id(1L).build());

        manager.logAction("actor@email.com", "LOGIN", "AppUser", 42, "payload");

        ArgumentCaptor<AuditLogRequestDto> captor = ArgumentCaptor.forClass(AuditLogRequestDto.class);
        verify(auditLogService).create(captor.capture());

        AuditLogRequestDto dto = captor.getValue();
        assertEquals("actor@email.com", dto.getActor());
        assertEquals("LOGIN", dto.getAction());
        assertEquals("AppUser", dto.getEntity());
        assertEquals(42, dto.getEntityId());
        assertEquals("payload", dto.getPayload());
    }

    @Test
    void logActionDevePermitirCamposOpcionaisNulos() {
        when(auditLogService.create(any(AuditLogRequestDto.class))).thenReturn(AuditLogResponseDto.builder().id(2L).build());

        manager.logAction("actor@email.com", "READ", "Application", null, null);

        ArgumentCaptor<AuditLogRequestDto> captor = ArgumentCaptor.forClass(AuditLogRequestDto.class);
        verify(auditLogService).create(captor.capture());

        AuditLogRequestDto dto = captor.getValue();
        assertNull(dto.getEntityId());
        assertNull(dto.getPayload());
    }
}
