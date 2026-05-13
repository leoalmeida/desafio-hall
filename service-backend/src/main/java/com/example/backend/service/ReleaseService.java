package com.example.backend.service;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.exception.BusinessException;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import lombok.NonNull;

/**
 * Interface de serviço para a entidade Release.
 */
public interface ReleaseService {

    /**
     * Cria uma nova release.
     *
     * @param dto Dados da release
     * @return Release criada
     */
    ReleaseResponseDto create(@NonNull ReleaseRequestDto dto) throws BusinessException;

    /**
     * Busca releases a partir dos filtros fornecidos.
     *
     * @param applicationId ID da aplicação
     * @param version Versão da release
     * @param environment Ambiente da release
     * @param status Status da release
     * @return Lista de releases
     */
    List<ReleaseResponseDto> find(
            @NonNull Long applicationId,
            @NonNull String version,
            @NonNull EnvironmentEnum environment,
            @NonNull StatusEnum status)
            throws BusinessException;

    /**
     * Busca uma release por ID.
     *
     * @param id ID da release
     * @return Release encontrada
     */
    ReleaseResponseDto findById(@NonNull Long id) throws EntityNotFoundException;

    /**
     * Busca todas as releases.
     *
     * @return Lista de todas as releases
     */
    List<ReleaseResponseDto> findAll() throws BusinessException;

    /**
     * Altera uma release.
     *
     * @param id  ID da release
     * @param dto Novos dados
     * @return Release alterada
     */
    ReleaseResponseDto updateRelease(@NonNull Long id, @NonNull ReleaseRequestDto dto)
            throws EntityNotFoundException, BusinessException;

    /**
     * Remove uma release.
     *
     * @param id ID da release
     */
    void deleteRelease(@NonNull Long id) throws EntityNotFoundException, BusinessException;

    /**
     * Aprova ou rejeita uma release.
     *
     * @param id ID da release
     * @param outcome Resultado da aprovação (APPROVED ou REJECTED)
     */
    void approveRelease(@NonNull Long id, @NonNull OutcomeEnum outcome)
            throws EntityNotFoundException, BusinessException;

    /**
     * Promove uma release para o próximo ambiente.
     *
     * @param id ID da release
     */
    void promoteRelease(@NonNull Long id) throws EntityNotFoundException, BusinessException;
}
