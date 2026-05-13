package com.example.backend.dto;

import com.example.backend.validator.ApplicationValidator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para a entidade Application.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ApplicationResponseDto", description = "Dados de uma aplicação")
@ToString
public class ApplicationResponseDto {

    @Schema(description = "Identificador único da aplicação", example = "1")
    private Long id;

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
    private String ownerTeam;

    @Schema(description = "URL do repositório da aplicação", example = "https://github.com/example/repo")
    private String repoUrl;

    @Schema(description = "Data de criação da aplicação", example = "2024-01-01T12:00:00")
    private String createdAt;

    @Schema(description = "Data da última atualização da aplicação", example = "2024-01-02T12:00:00")
    private String updatedAt;
}
