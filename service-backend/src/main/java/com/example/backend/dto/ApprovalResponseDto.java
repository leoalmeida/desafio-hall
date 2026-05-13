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
 * DTO para resposta de Approval.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ApprovalResponseDto", description = "Dados de uma aprovação")
@ToString
public class ApprovalResponseDto {

    @Schema(description = "Identificador único da aprovação", example = "1")
    private Long id;

    @Schema(description = "ID da release", example = "1")
    private Long releaseId;

    @Schema(description = "Email do aprovador", example = "approver@example.com")
    @Size(max = DtoConstants.MAX_EMAIL_LENGTH, message = "Email deve ter no máximo 255 caracteres")
    private String approverEmail;

    @Schema(description = "Resultado da aprovação", example = "APPROVED")
    @Size(max = DtoConstants.MAX_OUTCOME_LENGTH, message = "Resultado deve ter no máximo 50 caracteres")
    private String outcome;

    @Schema(description = "Notas sobre a aprovação", example = "Aprovado após revisão")
    private String notes;

    @Schema(description = "Data e hora da aprovação", example = "2024-01-01T12:00:00")
    private String timestamp;
}
