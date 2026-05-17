package com.example.backend.security;

import com.example.backend.service.AuditLogService;
import com.example.backend.dto.AuditLogRequestDto;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Componente centralizado para gerenciar logs de auditoria.
 * Encapsula a lógica de obtenção do usuário autenticado e criação de registros
 * de auditoria.
 */
@Component
@Slf4j
public class AuditLogManager {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditLogManager(final AuditLogService auditLogService, final ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra uma ação de auditoria com o usuário autenticado como actor.
     * 
     * @param action   a ação realizada
     * @param entity   a entidade afetada
    * @param entityId o ID textual da entidade (opcional)
     * @param payload  dados adicionais sobre a ação (opcional)
     */
    public void logAction(
            final String actor,
            final String action,
            final String entity,
            final String entityId,
            final String payload) {

        auditLogService.create(AuditLogRequestDto.builder()
                .actor(actor)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .payload(payload)
                .build());
    }

    public String toJsonNode(final String entityName, final String simpleText) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put(entityName, simpleText);
        return json.toString();
    }

    public String toJsonNode(final Object simpleObject) {
        try {
            // Convert object to JSON string
            return objectMapper.writeValueAsString(simpleObject);
        } catch (JsonProcessingException e) {
            log.info("Error converting object to JSON: " + e.getMessage());
        }
        return null;
    }
}
