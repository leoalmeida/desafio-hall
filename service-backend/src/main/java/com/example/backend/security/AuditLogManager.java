package com.example.backend.security;

import com.example.backend.service.AuditLogService;
import com.example.backend.dto.AuditLogRequestDto;

import org.springframework.stereotype.Component;

/**
 * Componente centralizado para gerenciar logs de auditoria.
 * Encapsula a lógica de obtenção do usuário autenticado e criação de registros de auditoria.
 */
@Component
public class AuditLogManager {

    private final AuditLogService auditLogService;

    public AuditLogManager(final AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Registra uma ação de auditoria com o usuário autenticado como actor.
     * 
     * @param action a ação realizada
     * @param entity a entidade afetada
     * @param entityId o ID da entidade (opcional)
     * @param payload dados adicionais sobre a ação (opcional)
     */
    public void logAction(
            final String actor,
            final String action,
            final String entity,
            final Integer entityId,
            final String payload) {
        auditLogService.create(AuditLogRequestDto.builder()
                .actor(actor)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .payload(payload)
                .build());
    }

}
