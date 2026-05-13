package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.Release;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.domain.repository.ReleaseRepository;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReleaseMapper;
import com.example.backend.service.ReleaseService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação do serviço de Release.
 */
@Service
@Slf4j
public class ReleaseServiceImpl implements ReleaseService {

    private final ReleaseRepository repository;

    @Autowired
    public ReleaseServiceImpl(final ReleaseRepository repository) {
        this.repository = Objects.requireNonNull(repository, "ReleaseRepository não pode ser nulo");
    }

    @Override
    @Transactional
    public ReleaseResponseDto create(final ReleaseRequestDto dto) throws BusinessException {
        if (dto == null) {
            throw new BusinessException("Dados da release não podem ser nulos");
        }
        log.info("Criando nova release para aplicação ID: {}", dto.getApplicationId());

        Release entity = ReleaseMapper.mapRequest(dto);
        entity.setCreatedAt(LocalDateTime.now());

        Release saved = repository.saveAndFlush(entity);
        log.info("Release criada com sucesso. ID: {}", saved.getId());

        return ReleaseMapper.mapResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReleaseResponseDto> find(
            final Long applicationId, final String version, final EnvironmentEnum environment, final StatusEnum status)
            throws BusinessException {
        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException("ID da aplicação inválido");
        }
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Versão da release não pode ser vazia");
        }
        if (environment == null) {
            throw new IllegalArgumentException("Ambiente da release não pode ser vazio");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status da release não pode ser vazio");
        }
        log.info(
                "Buscando releases para aplicação ID: {}, versão: {}, ambiente: {}, status: {}",
                applicationId,
                version,
                environment,
                status);

        log.info("Buscando releases para aplicação ID: {}", applicationId);

        List<Release> entities = repository.findRelease(applicationId, version, environment.name(), status.name());

        return entities.stream().map(ReleaseMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReleaseResponseDto findById(final Long id) throws EntityNotFoundException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        log.info("Buscando release por ID: {}", id);

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        return ReleaseMapper.mapResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReleaseResponseDto> findAll() throws BusinessException {
        log.info("Buscando todas as releases");

        List<Release> entities = repository.findAll();

        return entities.stream().map(ReleaseMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReleaseResponseDto updateRelease(final Long id, final ReleaseRequestDto dto)
            throws EntityNotFoundException, BusinessException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        if (dto == null) {
            throw new BusinessException("Dados da release não podem ser nulos");
        }
        log.info("Alterando release ID: {}", id);

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        entity.setVersion(dto.getVersion());
        entity.setEnv(dto.getEnv());
        entity.setStatus(dto.getStatus());
        entity.setEvidenceUrl(dto.getEvidenceUrl());

        Release saved = repository.saveAndFlush(entity);
        log.info("Release alterada com sucesso. ID: {}", saved.getId());

        return ReleaseMapper.mapResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRelease(final Long id) throws EntityNotFoundException, BusinessException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        log.info("Removendo release ID: {}", id);

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Release não encontrada com ID: " + id);
        }

        repository.deleteById(id);
        log.info("Release removida com sucesso. ID: {}", id);
    }

    @Override
    @Transactional
    public void approveRelease(final Long id, final OutcomeEnum outcome)
            throws EntityNotFoundException, BusinessException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("Outcome inválido para aprovação de release");
        }
        log.info("Processando aprovação para release ID: {} com outcome: {}", id, outcome);

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        applyApprovalTransition(id, outcome, entity);

        repository.saveAndFlush(entity);
    }

    private void applyApprovalTransition(final Long id, final OutcomeEnum outcome, final Release entity)
            throws BusinessException {
        if (outcome == OutcomeEnum.APPROVED) {
            if (entity.getStatus() == StatusEnum.PENDING_PREPROD) {
                entity.setStatus(StatusEnum.APPROVED_PREPROD);
            } else if (entity.getStatus() == StatusEnum.PENDING_PROD) {
                entity.setStatus(StatusEnum.APPROVED_PROD);
            } else {
                throw new BusinessException("Status da release não permite aprovação");
            }
            log.info("Release ID: {} aprovada", id);
            return;
        }

        if (outcome == OutcomeEnum.REJECTED) {
            entity.setStatus(StatusEnum.REJECTED);
            log.info("Release ID: {} rejeitada", id);
            return;
        }

        throw new IllegalArgumentException("Outcome inválido para aprovação de release");
    }

    @Override
    @Transactional
    public void promoteRelease(final Long id) throws EntityNotFoundException, BusinessException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        log.info("Promovendo release ID: {}", id);

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        StatusEnum currentStatus = entity.getStatus();
        if (currentStatus == StatusEnum.CREATED) {
            entity.setStatus(StatusEnum.PENDING_PREPROD);
        } else if (currentStatus == StatusEnum.APPROVED_PREPROD) {
            entity.setStatus(StatusEnum.PENDING_PROD);
        } else if (currentStatus == StatusEnum.APPROVED_PROD) {
            entity.setStatus(StatusEnum.DEPLOYED);
            entity.setDeployedAt(LocalDateTime.now());
        } else {
            throw new BusinessException("Status da release não permite promoção");
        }

        repository.saveAndFlush(entity);
        log.info("Release ID: {} promovida para status: {}", id, entity.getStatus());
    }
}
