package com.example.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.backend.domain.entity.AuditLog;
import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;

class AuditLogMapperTest {

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
                .entityId(99)
                .payload("{\"k\":\"v\"}")
                .build();

        AuditLog result = AuditLogMapper.mapRequest(dto);

        assertNotNull(result);
        assertEquals("user@email.com", result.getActor());
        assertEquals("CREATE", result.getAction());
        assertEquals("Release", result.getEntity());
        assertEquals(99, result.getEntityId());
        assertEquals("{\"k\":\"v\"}", result.getPayload());
    }

    @Test
    void mapResponseDeveRetornarNullQuandoEntidadeForNull() {
        assertNull(AuditLogMapper.mapResponse(null));
    }

    @Test
    void mapResponseDeveMapearCamposCorretamente() {
        AuditLog entity = AuditLog.builder()
                .id(10L)
                .actor("admin@email.com")
                .action("UPDATE")
                .entity("Application")
                .entityId(7)
                .payload("{\"changed\":true}")
                .timestamp(LocalDateTime.of(2025, 2, 3, 4, 5, 6))
                .build();

        AuditLogResponseDto result = AuditLogMapper.mapResponse(entity);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("admin@email.com", result.getActor());
        assertEquals("UPDATE", result.getAction());
        assertEquals("Application", result.getEntity());
        assertEquals(7, result.getEntityId());
        assertEquals("{\"changed\":true}", result.getPayload());
        assertEquals("2025-02-03T04:05:06", result.getTimestamp());
    }
}
