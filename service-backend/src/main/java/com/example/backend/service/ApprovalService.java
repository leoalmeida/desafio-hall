package com.example.backend.service;

import java.util.List;

import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.dto.ApprovalRequestDto;
import com.example.backend.dto.ApprovalResponseDto;
import com.example.backend.exception.BusinessException;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;

/**
 * Interface de serviço para a entidade Approval.
 */
public interface ApprovalService {

    /**
     * Cria uma nova aprovação.
     *
     * @param dto Dados da aprovação
     * @return Approval criada
     */
    ApprovalResponseDto create(@NonNull ApprovalRequestDto dto) throws BusinessException;

    /**
     * Busca uma aprovação por ID.
     *
     * @param id ID da aprovação
     * @return Aprovação encontrada
     */
    ApprovalResponseDto findById(@NonNull Long id) throws EntityNotFoundException;

    /**
     * Busca aprovações por ID da release.
     *
     * @param releaseId ID da release
     * @return Lista de aprovações
     */
    List<ApprovalResponseDto> findByReleaseId(@NonNull Long releaseId) throws BusinessException;

    /**
     * Busca aprovações por email do aprovador.
     *
     * @param approverEmail Email do aprovador
     * @return Lista de aprovações
     */
    List<ApprovalResponseDto> findByApprover(@NonNull String approverEmail) throws BusinessException;

    /**
     * Busca aprovações por outcome.
     *
     * @param outcome Outcome da aprovação
     * @return Lista de aprovações
     */
    List<ApprovalResponseDto> findByOutcome(@NonNull OutcomeEnum outcome) throws BusinessException;

    /**
     * Busca todas as aprovações.
     *
     * @return Lista de aprovações
     */
    List<ApprovalResponseDto> findAll() throws BusinessException;

}
