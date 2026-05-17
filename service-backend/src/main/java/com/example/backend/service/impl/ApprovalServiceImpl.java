package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.repository.ApprovalRepository;
import com.example.backend.dto.ApprovalRequestDto;
import com.example.backend.dto.ApprovalResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ApprovalMapper;
import com.example.backend.service.ApprovalService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação do serviço de Approval.
 */
@Service
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository repository;

    @Autowired
    public ApprovalServiceImpl(final ApprovalRepository repository) {
        this.repository = Objects.requireNonNull(repository, "ApprovalRepository não pode ser nulo");
    }

    @Override
    @Transactional
    public ApprovalResponseDto create(final ApprovalRequestDto dto) throws BusinessException {
        if (dto == null) {
            throw new BusinessException("Dados de aprovação não podem ser nulos");
        }
        log.info("Criando nova aprovação para release ID: {}", dto.getReleaseId());

        Approval entity = ApprovalMapper.mapRequest(dto);
        entity.setTimestamp(LocalDateTime.now());

        Approval saved = repository.saveAndFlush(entity);
        log.info("Aprovação criada com sucesso. ID: {}", saved.getId());

        return ApprovalMapper.mapResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalResponseDto findById(final UUID id) throws EntityNotFoundException {
        if (id == null) {
            throw new IllegalArgumentException("ID da aprovação inválido");
        }
        log.info("Buscando aprovação por ID: {}", id);

        Approval entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aprovação não encontrada com ID: " + id));

        return ApprovalMapper.mapResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponseDto> findByReleaseId(final UUID releaseId) throws BusinessException {
        if (releaseId == null) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        log.info("Buscando aprovações para release ID: {}", releaseId);

        List<Approval> entities = repository.findByReleaseId(releaseId);

        return entities.stream().map(ApprovalMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponseDto> findByApprover(final String approverEmail) throws BusinessException {
        if (approverEmail == null || approverEmail.isBlank()) {
            throw new IllegalArgumentException("Email do aprovador não pode ser vazio");
        }
        log.info("Buscando aprovações para aprovador: {}", approverEmail);

        List<Approval> entities = repository.findByApproverEmail(approverEmail);

        return entities.stream().map(ApprovalMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponseDto> findByOutcome(final OutcomeEnum outcome) throws BusinessException {
        if (outcome == null) {
            throw new IllegalArgumentException("Outcome da aprovação não pode ser nulo");
        }
        log.info("Buscando aprovações para outcome: {}", outcome);

        List<Approval> entities = repository.findByOutcome(outcome);

        return entities.stream().map(ApprovalMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponseDto> findAll() throws BusinessException {
        log.info("Buscando todas as aprovações");

        List<Approval> entities = repository.findAll();

        return entities.stream().map(ApprovalMapper::mapResponse).collect(Collectors.toList());
    }
}
