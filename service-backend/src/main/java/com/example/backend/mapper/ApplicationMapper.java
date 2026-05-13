package com.example.backend.mapper;

import com.example.backend.domain.entity.Application;
import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ApplicationResponseDto;

import jakarta.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;

/**
 * Classe Mapper responsável pela conversão entre Entity e DTO de aplicações.
 */
public class ApplicationMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Converte Um ApplicationRequestDto para Application Entity
     * @param dto DTO de requisição contendo os dados da aplicação a ser criada ou atualizada
     * @return Application Entity correspondente aos dados do DTO, ou null se o DTO for null
     */
    public static Application mapRequest(@NotNull final ApplicationRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Application application = Application.builder()
                .name(dto.getName())
                .ownerTeam(dto.getOwnerTeam())
                .repoUrl(dto.getRepoUrl())
                .build();
        return application;
    }

    /**
     * Converte Entity para ApplicationResponseDto
     * @param application Entity de aplicação a ser convertida para DTO de resposta
     * @return ApplicationResponseDto correspondente aos dados da Entity, ou null se a Entity for null
     */
    public static ApplicationResponseDto mapResponse(@NotNull final Application application) {
        if (application == null) {
            return null;
        }
        return ApplicationResponseDto.builder()
                .id(application.getId())
                .name(application.getName())
                .ownerTeam(application.getOwnerTeam())
                .repoUrl(application.getRepoUrl())
                .createdAt(
                        application.getCreatedAt() != null
                                ? application.getCreatedAt().format(DATE_TIME_FORMATTER)
                                : null)
                .updatedAt(
                        application.getUpdatedAt() != null
                                ? application.getUpdatedAt().format(DATE_TIME_FORMATTER)
                                : null)
                .build();
    }

    /**
     * Faz o merge de um dto de request para um Entity existente, atualizando
     * apenas os campos que foram modificados
     * @param objDestino Entity de aplicação existente a ser atualizado
     * @param objOrigem DTO de requisição contendo os novos dados da aplicação
     * @param isFullUpdate Indica se a atualização deve ser completa ou parcial
     * @return Entity de aplicação atualizado com os dados do DTO, ou null se o DTO for null
     * @throws IllegalArgumentException se o DTO de origem ou a Entity de destino forem nulos
     */
    public static Application map(
            @NotNull final Application objDestino,
            @NotNull final ApplicationRequestDto objOrigem,
            @NotNull final Boolean isFullUpdate)
            throws IllegalArgumentException {
        if (objOrigem == null) {
            throw new IllegalArgumentException("Objeto de origem não pode ser nulo");
        }
        if (objDestino == null) {
            throw new IllegalArgumentException("Objeto de destino não pode ser nulo");
        }
        if (isFullUpdate) {
            objDestino.setName(objOrigem.getName());
            objDestino.setOwnerTeam(objOrigem.getOwnerTeam());
            objDestino.setRepoUrl(objOrigem.getRepoUrl());
        } else {
            if (objOrigem.getName() != null && !objOrigem.getName().isBlank()) {
                objDestino.setName(objOrigem.getName());
            }
            if (objOrigem.getOwnerTeam() != null && !objOrigem.getOwnerTeam().isBlank()) {
                objDestino.setOwnerTeam(objOrigem.getOwnerTeam());
            }
            if (objOrigem.getRepoUrl() != null && !objOrigem.getRepoUrl().isBlank()) {
                objDestino.setRepoUrl(objOrigem.getRepoUrl());
            }
        }
        return objDestino;
    }
}
