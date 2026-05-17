package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.domain.entity.AuditLog;
import com.example.backend.domain.repository.AuditLogRepository;
import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.impl.AuditLogServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    private static final UUID AUDIT_LOG_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID AUDIT_LOG_ID_2 = UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID AUDIT_LOG_ID_5 = UUID.fromString("30000000-0000-0000-0000-000000000005");
    private static final String RELEASE_ENTITY_ID = "10000000-0000-0000-0000-000000000001";
    private static final String APPLICATION_ENTITY_ID = "00000000-0000-0000-0000-000000000003";
    private static final String RELEASE_ENTITY_ID_7 = "10000000-0000-0000-0000-000000000007";

    @Mock
    private AuditLogRepository repository;

    private AuditLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditLogServiceImpl(repository);
    }

    // ---- create ----

    @Test
    void createDeveThrowBusinessExceptionQuandoDtoNulo() {
        assertThrows(BusinessException.class, () -> service.create(null));
    }

    @Test
    void createDeveSalvarERetornarAuditLogResponseDto() throws BusinessException {
        AuditLogRequestDto dto = AuditLogRequestDto.builder()
                .actor("admin@example.com")
                .action("CREATE")
                .entity("Release")
            .entityId(RELEASE_ENTITY_ID)
                .payload("{\"version\":\"V1.0\"}")
                .build();

        AuditLog saved = AuditLog.builder()
            .id(AUDIT_LOG_ID_5)
                .actor("admin@example.com")
                .action("CREATE")
                .entity("Release")
            .entityId(RELEASE_ENTITY_ID)
                .payload("{\"version\":\"V1.0\"}")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.saveAndFlush(any(AuditLog.class))).thenReturn(saved);

        AuditLogResponseDto response = service.create(dto);

        assertNotNull(response);
        assertEquals(AUDIT_LOG_ID_5, response.getId());
        assertEquals("admin@example.com", response.getActor());
        assertEquals("CREATE", response.getAction());
        verify(repository).saveAndFlush(any(AuditLog.class));
    }

    // ---- findById ----

    @Test
    void findByIdDeveThrowIllegalArgumentExceptionQuandoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }

    @Test
    void findByIdDeveThrowIllegalArgumentExceptionQuandoIdAusente() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }

    @Test
    void findByIdDeveThrowEntityNotFoundExceptionQuandoNaoExistir() {
        UUID missingAuditLogId = UUID.fromString("30000000-0000-0000-0000-000000000099");
        when(repository.findById(missingAuditLogId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(missingAuditLogId));
    }

    @Test
    void findByIdDeveRetornarDtoQuandoExistir() throws EntityNotFoundException {
        AuditLog log = AuditLog.builder()
                .id(AUDIT_LOG_ID)
                .actor("user@example.com")
                .action("UPDATE")
                .entity("Application")
                .entityId(APPLICATION_ENTITY_ID)
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findById(AUDIT_LOG_ID)).thenReturn(Optional.of(log));

        AuditLogResponseDto response = service.findById(AUDIT_LOG_ID);

        assertNotNull(response);
        assertEquals(AUDIT_LOG_ID, response.getId());
        assertEquals("UPDATE", response.getAction());
    }

    // ---- findByActor ----

    @Test
    void findByActorDeveThrowIllegalArgumentExceptionQuandoAtorNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.findByActor(null));
    }

    @Test
    void findByActorDeveThrowIllegalArgumentExceptionQuandoAtorVazio() {
        assertThrows(IllegalArgumentException.class, () -> service.findByActor("  "));
    }

    @Test
    void findByActorDeveRetornarRegistrosDoAtor() throws BusinessException {
        AuditLog log = AuditLog.builder()
                .id(AUDIT_LOG_ID)
                .actor("admin@example.com")
                .action("DELETE")
                .entity("Release")
                .entityId(RELEASE_ENTITY_ID_7)
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findByActorContainingIgnoreCase("admin")).thenReturn(List.of(log));

        List<AuditLogResponseDto> result = service.findByActor("admin");

        assertEquals(1, result.size());
        assertEquals("admin@example.com", result.get(0).getActor());
    }

    // ---- findByAction ----

    @Test
    void findByActionDeveThrowIllegalArgumentExceptionQuandoAcaoVazia() {
        assertThrows(IllegalArgumentException.class, () -> service.findByAction(""));
    }

    @Test
    void findByActionDeveRetornarRegistrosDaAcao() throws BusinessException {
        AuditLog log = AuditLog.builder()
                .id(AUDIT_LOG_ID_2)
                .actor("u@x.com")
                .action("CREATE")
                .entity("Application")
                .entityId("00000000-0000-0000-0000-000000000001")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findByActionContainingIgnoreCase("CREATE")).thenReturn(List.of(log));

        List<AuditLogResponseDto> result = service.findByAction("CREATE");

        assertEquals(1, result.size());
        assertEquals("CREATE", result.get(0).getAction());
    }
}
