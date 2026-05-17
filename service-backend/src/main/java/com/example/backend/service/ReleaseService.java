package com.example.backend.service;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.dto.EvidenceScoreResponseDto;
import com.example.backend.dto.ReleaseEvidenceUpdateRequestDto;
import com.example.backend.exception.BusinessException;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.UUID;

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
            @NonNull UUID applicationId,
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
        ReleaseResponseDto findById(@NonNull UUID id) throws EntityNotFoundException;

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
        ReleaseResponseDto updateRelease(@NonNull UUID id, @NonNull ReleaseRequestDto dto)
            throws EntityNotFoundException, BusinessException;

    /**
     * Atualiza apenas a evidenceUrl de uma release.
     *
     * @param id ID da release
     * @param dto dados de evidência
     * @return Release atualizada
     */
        ReleaseResponseDto updateEvidenceUrl(@NonNull UUID id, @NonNull ReleaseEvidenceUpdateRequestDto dto)
            throws EntityNotFoundException, BusinessException;

    /**
     * Remove uma release.
     *
     * @param id ID da release
     */
        void deleteRelease(@NonNull UUID id) throws EntityNotFoundException, BusinessException;

    /**
     * Aprova ou rejeita uma release.
     *
     * @param id ID da release
     * @param outcome Resultado da aprovação (APPROVED ou REJECTED)
     */
        void approveRelease(@NonNull UUID id, @NonNull OutcomeEnum outcome)
            throws EntityNotFoundException, BusinessException;

    /**
     * Promove uma release para o próximo ambiente.
     *
     * @param id ID da release
     */
        void promoteRelease(@NonNull UUID id) throws EntityNotFoundException, BusinessException;

        /**
         * Calcula score determinístico (0..100) de evidência para uma release.
         *
         * @param id ID da release
         * @return Resultado do score de evidência
         */
        EvidenceScoreResponseDto calculateEvidenceScore(@NonNull UUID id) throws EntityNotFoundException;
}
