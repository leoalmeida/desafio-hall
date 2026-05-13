package com.example.backend.mapper;

import com.example.backend.domain.entity.AuditLog;
import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;

import java.time.format.DateTimeFormatter;

/**
 * Mapper para conversão entre AuditLog e DTOs.
 */
public class AuditLogMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private AuditLogMapper() {
        // Utility class
    }

    /**
     * Converte AuditLogRequestDto para entidade AuditLog.
     *
     * @param dto DTO de request
     * @return Entidade AuditLog
     */
    public static AuditLog mapRequest(final AuditLogRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return AuditLog.builder()
                .actor(dto.getActor())
                .action(dto.getAction())
                .entity(dto.getEntity())
                .entityId(dto.getEntityId())
                .payload(dto.getPayload())
                .build();
    }

    /**
     * Converte entidade AuditLog para AuditLogResponseDto.
     *
     * @param entity Entidade AuditLog
     * @return DTO de response
     */
    public static AuditLogResponseDto mapResponse(final AuditLog entity) {
        if (entity == null) {
            return null;
        }
        return AuditLogResponseDto.builder()
                .id(entity.getId())
                .actor(entity.getActor())
                .action(entity.getAction())
                .entity(entity.getEntity())
                .entityId(entity.getEntityId())
                .payload(entity.getPayload())
                .timestamp(entity.getTimestamp() != null ? entity.getTimestamp().format(DATE_TIME_FORMATTER) : null)
                .build();
    }
}
