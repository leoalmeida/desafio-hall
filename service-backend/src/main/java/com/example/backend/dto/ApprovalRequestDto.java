package com.example.backend.dto;

import com.example.backend.domain.entity.OutcomeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para criação de Approval.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ApprovalRequestDto", description = "Dados para criar uma aprovação")
@ToString
public class ApprovalRequestDto {

    @Schema(description = "ID da release", example = "10000000-0000-0000-0000-000000000001")
    @NotNull(message = "ID da release é obrigatório")
    private UUID releaseId;

    @Schema(description = "Email do aprovador", example = "approver@example.com")
    @NotBlank(message = "Email do aprovador é obrigatório")
    @Size(max = DtoConstants.MAX_EMAIL_LENGTH, message = "Email deve ter no máximo 255 caracteres")
    private String approverEmail;

    @Schema(description = "Resultado da aprovação", example = "APPROVED")
    @NotNull(message = "Resultado é obrigatório")
    private OutcomeEnum outcome;

    @Schema(description = "Notas sobre a aprovação", example = "Aprovado após revisão")
    @Size(max = DtoConstants.MAX_NOTES_LENGTH, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;
}
