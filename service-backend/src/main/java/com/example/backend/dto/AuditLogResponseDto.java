package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para resposta de AuditLog.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "AuditLogResponseDto", description = "Dados de um registro de auditoria")
@ToString
public class AuditLogResponseDto {

    @Schema(description = "Identificador único do registro", example = "1")
    private Long id;

    @Schema(description = "Ator que realizou a ação", example = "usuario@example.com")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Ator deve ter no máximo 255 caracteres")
    private String actor;

    @Schema(description = "Ação realizada", example = "CREATE")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Ação deve ter no máximo 255 caracteres")
    private String action;

    @Schema(description = "Entidade afetada", example = "Release")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Entidade deve ter no máximo 255 caracteres")
    private String entity;

    @Schema(description = "ID da entidade", example = "1")
    private Integer entityId;

    @Schema(description = "Dados da ação em JSON", example = "{\"field\": \"value\"}")
    private String payload;

    @Schema(description = "Data e hora da ação", example = "2024-01-01T12:00:00")
    private String timestamp;
}
