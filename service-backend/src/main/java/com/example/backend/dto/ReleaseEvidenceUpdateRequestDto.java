package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para atualização da evidence URL de uma release.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ReleaseEvidenceUpdateRequestDto", description = "Dados para atualizar evidenceUrl da release")
@ToString
public class ReleaseEvidenceUpdateRequestDto {

    @Schema(description = "URL da evidência de deployment", example = "https://example.com/evidence")
    @NotBlank(message = "Evidence URL é obrigatória")
    @Size(max = DtoConstants.MAX_EMAIL_LENGTH, message = "URL deve ter no máximo 255 caracteres")
    private String evidenceUrl;
}
