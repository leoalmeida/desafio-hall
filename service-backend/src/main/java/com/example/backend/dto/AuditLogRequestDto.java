package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para criação de AuditLog.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "AuditLogRequestDto", description = "Dados para criar um registro de auditoria")
@ToString
public class AuditLogRequestDto {

    @Schema(description = "Ator que realizou a ação", example = "usuario@example.com")
    @NotBlank(message = "Ator é obrigatório")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Ator deve ter no máximo 255 caracteres")
    private String actor;

    @Schema(description = "Ação realizada", example = "CREATE")
    @NotBlank(message = "Ação é obrigatória")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Ação deve ter no máximo 255 caracteres")
    private String action;

    @Schema(description = "Entidade afetada", example = "Release")
    @NotBlank(message = "Entidade é obrigatória")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Entidade deve ter no máximo 255 caracteres")
    private String entity;

    @Schema(description = "ID da entidade", example = "1")
    private Integer entityId;

    @Schema(description = "Dados da ação em JSON", example = "{\"field\": \"value\"}")
    private String payload;
}
