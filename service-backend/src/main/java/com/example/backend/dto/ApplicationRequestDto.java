package com.example.backend.dto;

import com.example.backend.validator.ApplicationValidator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para a entidade Application.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ApplicationRequestDto", description = "Dados de um Request para criação ou alteração de uma aplicação")
public class ApplicationRequestDto {

    @Schema(
            description = "Nome da aplicação",
            example = "App teste",
            maxLength = ApplicationValidator.NOME_MAX_LENGTH)
    @Size(
            max = ApplicationValidator.NOME_MAX_LENGTH,
            message = "Nome deve ter entre "
                    + ApplicationValidator.NOME_MIN_LENGTH + " e "
                    + ApplicationValidator.NOME_MAX_LENGTH + " caracteres")
    private String name;

    @Schema(description = "Equipe responsável pela aplicação", example = "Equipe de Desenvolvimento")
    @Size(
            max = ApplicationValidator.OWNERTEAM_MAX_LENGTH,
            message = "OwnerTeam deve ter entre "
                    + ApplicationValidator.OWNERTEAM_MIN_LENGTH + " e "
                    + ApplicationValidator.OWNERTEAM_MAX_LENGTH + " caracteres")
    private String ownerTeam;

    @Schema(description = "URL do repositório da aplicação", example = "https://github.com/example/repo")
    @Size(
            max = ApplicationValidator.REPOURL_MAX_LENGTH,
            message = "Repo URL deve ter entre "
                    + ApplicationValidator.REPOURL_MIN_LENGTH + " e "
                    + ApplicationValidator.REPOURL_MAX_LENGTH + " caracteres")
    private String repoUrl;
}
