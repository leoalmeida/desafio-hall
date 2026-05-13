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
 * DTO para resposta de Release.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ReleaseResponseDto", description = "Dados de uma release")
@ToString
public class ReleaseResponseDto {

    @Schema(description = "Identificador único da release", example = "1")
    private Long id;

    @Schema(description = "ID da aplicação", example = "1")
    private Long applicationId;

    @Schema(description = "Versão da release", example = "1.0.0")
    @Size(max = DtoConstants.MAX_VERSION_LENGTH, message = "Versão deve ter no máximo 50 caracteres")
    private String version;

    @Schema(description = "Ambiente de deployment", example = "PROD")
    @Size(max = DtoConstants.MAX_VERSION_LENGTH, message = "Ambiente deve ter no máximo 50 caracteres")
    private String env;

    @Schema(description = "Status da release", example = "PENDING")
    @Size(max = DtoConstants.MAX_VERSION_LENGTH, message = "Status deve ter no máximo 50 caracteres")
    private String status;

    @Schema(description = "URL da evidência de deployment", example = "https://example.com/evidence")
    @Size(max = DtoConstants.MAX_EMAIL_LENGTH, message = "URL deve ter no máximo 255 caracteres")
    private String evidenceUrl;

    @Schema(description = "Data de criação", example = "2024-01-01T12:00:00")
    private String createdAt;

    @Schema(description = "Data de deployment", example = "2024-01-02T12:00:00")
    private String deployedAt;
}
