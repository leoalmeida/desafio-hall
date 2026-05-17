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
 * DTO para aprovação ou reprovação de release com notas opcionais.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Schema(name = "ReleaseApprovalDecisionRequestDto", description = "Dados complementares da decisão de aprovação")
public class ReleaseApprovalDecisionRequestDto {

    @Schema(description = "Notas da decisão", example = "Aprovado após revisão final do changelog")
    @Size(max = DtoConstants.MAX_NOTES_LENGTH, message = "Notas devem ter no máximo 1000 caracteres")
    private String notes;
}
