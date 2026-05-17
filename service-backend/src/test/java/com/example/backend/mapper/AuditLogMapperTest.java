package com.example.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.backend.domain.entity.AuditLog;
import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;

class AuditLogMapperTest {

    private static final UUID AUDIT_LOG_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final String RELEASE_ENTITY_ID = "10000000-0000-0000-0000-000000000099";
    private static final String APPLICATION_ENTITY_ID = "00000000-0000-0000-0000-000000000007";

    @Test
    void mapRequestDeveRetornarNullQuandoDtoForNull() {
        assertNull(AuditLogMapper.mapRequest(null));
    }

    @Test
    void mapRequestDeveMapearCamposCorretamente() {
        AuditLogRequestDto dto = AuditLogRequestDto.builder()
                .actor("user@email.com")
                .action("CREATE")
                .entity("Release")
                .entityId(RELEASE_ENTITY_ID)
                .payload("{\"k\":\"v\"}")
                .build();

        AuditLog result = AuditLogMapper.mapRequest(dto);

        assertNotNull(result);
        assertEquals("user@email.com", result.getActor());
        assertEquals("CREATE", result.getAction());
        assertEquals("Release", result.getEntity());
        assertEquals(RELEASE_ENTITY_ID, result.getEntityId());
        assertEquals("{\"k\":\"v\"}", result.getPayload());
    }

    @Test
    void mapResponseDeveRetornarNullQuandoEntidadeForNull() {
        assertNull(AuditLogMapper.mapResponse(null));
    }

    @Test
    void mapResponseDeveMapearCamposCorretamente() {
        AuditLog entity = AuditLog.builder()
                .id(AUDIT_LOG_ID)
                .actor("admin@email.com")
                .action("UPDATE")
                .entity("Application")
                .entityId(APPLICATION_ENTITY_ID)
                .payload("{\"changed\":true}")
                .timestamp(LocalDateTime.of(2025, 2, 3, 4, 5, 6))
                .build();

        AuditLogResponseDto result = AuditLogMapper.mapResponse(entity);

        assertNotNull(result);
        assertEquals(AUDIT_LOG_ID, result.getId());
        assertEquals("admin@email.com", result.getActor());
        assertEquals("UPDATE", result.getAction());
        assertEquals("Application", result.getEntity());
        assertEquals(APPLICATION_ENTITY_ID, result.getEntityId());
        assertEquals("{\"changed\":true}", result.getPayload());
        assertEquals("2025-02-03T04:05:06", result.getTimestamp());
    }
}
