package com.example.backend.service;

import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;
import com.example.backend.exception.BusinessException;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.NonNull;

/**
 * Interface de serviço para a entidade AuditLog.
 */
public interface AuditLogService {

    /**
     * Cria um novo registro de auditoria.
     *
     * @param dto Dados do registro
     * @return AuditLog criado
     */
    AuditLogResponseDto create(@NonNull AuditLogRequestDto dto) throws BusinessException;

    /**
     * Busca um registro de auditoria por ID.
     *
     * @param id ID do registro
     * @return AuditLog encontrado
     */
    AuditLogResponseDto findById(@NonNull Long id) throws EntityNotFoundException;

    /**
     * Busca registros por ator.
     *
     * @param actor Nome do ator
     * @return Lista de registros
     */
    List<AuditLogResponseDto> findByActor(@NonNull String actor) throws BusinessException;

    /**
     * Busca registros por ação.
     *
     * @param action Ação realizada
     * @return Lista de registros
     */
    List<AuditLogResponseDto> findByAction(@NonNull String action) throws BusinessException;

    /**
     * Busca registros por entidade.
     *
     * @param entity Nome da entidade
     * @return Lista de registros
     */
    List<AuditLogResponseDto> findByEntity(@NonNull String entity) throws BusinessException;

    /**
     * Busca registros por intervalo de datas.
     *
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de registros
     */
    List<AuditLogResponseDto> findByDateRange(@NonNull LocalDateTime dataInicio, @NonNull LocalDateTime dataFim)
            throws BusinessException;

    /**
     * Busca todos os registros.
     *
     * @return Lista de todos os registros
     */
    List<AuditLogResponseDto> findAll() throws BusinessException;
}
