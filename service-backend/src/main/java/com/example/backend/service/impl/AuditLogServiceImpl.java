package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.AuditLog;
import com.example.backend.domain.repository.AuditLogRepository;
import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.AuditLogMapper;
import com.example.backend.service.AuditLogService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementação do serviço de AuditLog.
 */
@Service
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repository;

    @Autowired
    public AuditLogServiceImpl(final AuditLogRepository repository) {
        this.repository = Objects.requireNonNull(repository, "AuditLogRepository não pode ser nulo");
    }

    @Override
    @Transactional
    public AuditLogResponseDto create(final AuditLogRequestDto dto) throws BusinessException {
        if (dto == null) {
            throw new BusinessException("Dados de auditoria não podem ser nulos");
        }
        log.info("Criando novo registro de auditoria para entidade: {}", dto.getEntity());

        AuditLog entity = AuditLogMapper.mapRequest(dto);
        entity.setTimestamp(LocalDateTime.now());

        AuditLog saved = repository.saveAndFlush(entity);
        log.info("Registro de auditoria criado com sucesso. ID: {}", saved.getId());

        return AuditLogMapper.mapResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponseDto findById(final UUID id) throws EntityNotFoundException {
        if (id == null) {
            throw new IllegalArgumentException("ID do registro inválido");
        }
        log.info("Buscando registro de auditoria por ID: {}", id);

        AuditLog entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro não encontrado com ID: " + id));

        return AuditLogMapper.mapResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> findByActor(final String actor) throws BusinessException {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Nome do ator não pode ser vazio");
        }
        log.info("Buscando registros para ator: {}", actor);

        List<AuditLog> entities = repository.findByActorContainingIgnoreCase(actor);

        return entities.stream().map(AuditLogMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> findByAction(final String action) throws BusinessException {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Ação não pode ser vazia");
        }
        log.info("Buscando registros para ação: {}", action);

        List<AuditLog> entities = repository.findByActionContainingIgnoreCase(action);

        return entities.stream().map(AuditLogMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> findByEntity(final String entity) throws BusinessException {
        if (entity == null || entity.isBlank()) {
            throw new IllegalArgumentException("Entidade não pode ser vazia");
        }
        log.info("Buscando registros para entidade: {}", entity);

        List<AuditLog> entities = repository.findByEntity(entity);

        return entities.stream().map(AuditLogMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> findByDateRange(final LocalDateTime dataInicio, final LocalDateTime dataFim)
            throws BusinessException {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial não pode ser após data final");
        }
        log.info("Buscando registros entre {} e {}", dataInicio, dataFim);

        List<AuditLog> entities = repository.findByTimestampBetween(dataInicio, dataFim);

        return entities.stream().map(AuditLogMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> findAll() throws BusinessException {
        log.info("Buscando todos os registros de auditoria");

        List<AuditLog> entities = repository.findAll();

        return entities.stream().map(AuditLogMapper::mapResponse).collect(Collectors.toList());
    }
}
