package com.example.backend.dto;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.StatusEnum;
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
 * DTO para criação de Release.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ReleaseRequestDto", description = "Dados para criar uma release")
@ToString
public class ReleaseRequestDto {

    @Schema(description = "ID da aplicação", example = "1")
    @NotNull(message = "ID da aplicação é obrigatório")
    @Min(value = DtoConstants.MIN_ID_VALUE, message = "ID da application deve ser um número positivo")
    private Long applicationId;

    @Schema(description = "Versão da release", example = "1.0.0")
    @NotBlank(message = "Versão é obrigatória")
    @Size(max = DtoConstants.MAX_VERSION_LENGTH, message = "Versão deve ter no máximo 50 caracteres")
    private String version;

    @Schema(description = "Ambiente de deployment", example = "PROD")
    @NotNull(message = "Ambiente é obrigatório")
    private EnvironmentEnum env;

    @Schema(description = "Status da release", example = "PENDING")
    @NotNull(message = "Status é obrigatório")
    private StatusEnum status;

    @Schema(description = "URL da evidência de deployment", example = "https://example.com/evidence")
    @Size(max = DtoConstants.MAX_EMAIL_LENGTH, message = "URL deve ter no máximo 255 caracteres")
    private String evidenceUrl;
}
