package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.Release;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.domain.repository.ApprovalRepository;
import com.example.backend.domain.repository.ReleaseRepository;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.dto.EvidenceScoreResponseDto;
import com.example.backend.dto.ReleaseEvidenceUpdateRequestDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReleaseMapper;
import com.example.backend.policy.PolicyService;
import com.example.backend.service.ReleaseService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação do serviço de Release.
 */
@Service
@Slf4j
public class ReleaseServiceImpl implements ReleaseService {

    private static final int SCORE_URL_VALID = 40;
    private static final int SCORE_REPORT_PATTERN = 20;
    private static final int SCORE_PASS_TOKEN = 20;
    private static final int SCORE_STATUS_HIGH = 10;
    private static final int SCORE_STATUS_MEDIUM = 5;
    private static final int SCORE_ENV_PROD = 10;
    private static final int SCORE_ENV_PREPROD = 7;
    private static final int SCORE_ENV_DEV = 5;
    private static final int SCORE_MIN = 0;
    private static final int SCORE_MAX = 100;

    private final ReleaseRepository repository;
    private final ApprovalRepository approvalRepository;
    private final PolicyService policyService;

    @Autowired
    public ReleaseServiceImpl(
            final ReleaseRepository repository,
            final ApprovalRepository approvalRepository,
            final PolicyService policyService) {
        this.repository = Objects.requireNonNull(repository, "ReleaseRepository não pode ser nulo");
        this.approvalRepository = Objects.requireNonNull(approvalRepository, "ApprovalRepository não pode ser nulo");
        this.policyService = Objects.requireNonNull(policyService, "PolicyService não pode ser nulo");
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
            final UUID applicationId, final String version, final EnvironmentEnum environment, final StatusEnum status)
            throws BusinessException {
        if (applicationId == null) {
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

        List<Release> entities = repository.findRelease(applicationId, version, environment, status);

        return entities.stream().map(ReleaseMapper::mapResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReleaseResponseDto findById(final UUID id) throws EntityNotFoundException {
        if (id == null) {
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
    public ReleaseResponseDto updateRelease(final UUID id, final ReleaseRequestDto dto)
            throws EntityNotFoundException, BusinessException {
        if (id == null) {
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
    public ReleaseResponseDto updateEvidenceUrl(final UUID id, final ReleaseEvidenceUpdateRequestDto dto)
            throws EntityNotFoundException, BusinessException {
        if (id == null) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        if (dto == null || dto.getEvidenceUrl() == null || dto.getEvidenceUrl().isBlank()) {
            throw new BusinessException("Evidence URL não pode ser vazia");
        }

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        entity.setEvidenceUrl(dto.getEvidenceUrl());

        Release saved = repository.saveAndFlush(entity);
        return ReleaseMapper.mapResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRelease(final UUID id) throws EntityNotFoundException, BusinessException {
        if (id == null) {
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
    public void approveRelease(final UUID id, final OutcomeEnum outcome)
            throws EntityNotFoundException, BusinessException {
        if (id == null) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("Outcome inválido para aprovação de release");
        }
        log.info("Processando aprovação para release ID: {} com outcome: {}", id, outcome);

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        policyService.validateFreezeWindow(entity.getEnv());

        if (outcome == OutcomeEnum.APPROVED) {
            List<Approval> approvals = approvalRepository.findByReleaseId(id);
            long approvedCount = approvals.stream().filter(a -> a.getOutcome() == OutcomeEnum.APPROVED).count();
            long totalCount = approvals.size();
            policyService.validateApprovalThresholds(approvedCount, totalCount);
        }

        applyApprovalTransition(id, outcome, entity);

        repository.saveAndFlush(entity);
    }

    private void applyApprovalTransition(final UUID id, final OutcomeEnum outcome, final Release entity)
            throws BusinessException {
        if (outcome == OutcomeEnum.APPROVED) {
            switch (entity.getStatus()) {
                case PENDING_PREPROD -> entity.setStatus(StatusEnum.APPROVED_PREPROD);
                case PENDING_PROD -> entity.setStatus(StatusEnum.APPROVED_PROD);
                default -> throw new BusinessException("Status da release não permite aprovação");
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
    public void promoteRelease(final UUID id) throws EntityNotFoundException, BusinessException {
        if (id == null) {
            throw new IllegalArgumentException("ID da release inválido");
        }
        log.info("Promovendo release ID: {}", id);

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        switch (entity.getEnv()) {
            case DEV -> promoteFromDevToPreprod(entity);
            case PREPROD -> promoteFromPreprodToProd(id, entity);
            default -> throw new BusinessException("Promoção permitida somente para DEV→PREPROD e PREPROD→PROD");
        }

        repository.saveAndFlush(entity);
        log.info(
                "Release ID: {} promovida para ambiente: {} com status: {}",
                id,
                entity.getEnv(),
                entity.getStatus());
    }

    private void promoteFromDevToPreprod(final Release entity) throws BusinessException {
        if (entity.getStatus() != StatusEnum.CREATED) {
            throw new BusinessException("Status da release não permite promoção DEV→PREPROD");
        }

        policyService.validateFreezeWindow(EnvironmentEnum.PREPROD);
        entity.setEnv(EnvironmentEnum.PREPROD);
        entity.setStatus(StatusEnum.PENDING_PREPROD);
    }

    private void promoteFromPreprodToProd(final UUID id, final Release entity) throws BusinessException {
        if (entity.getStatus() != StatusEnum.APPROVED_PREPROD) {
            throw new BusinessException("Status da release não permite promoção PREPROD→PROD");
        }

        validateEvidenceUrl(entity.getEvidenceUrl());

        List<Approval> approvals = approvalRepository.findByReleaseId(id);
        long approvedCount = approvals.stream().filter(a -> a.getOutcome() == OutcomeEnum.APPROVED).count();
        long totalCount = approvals.size();

        policyService.validateFreezeWindow(EnvironmentEnum.PROD);
        policyService.validateApprovalThresholds(approvedCount, totalCount);

        entity.setEnv(EnvironmentEnum.PROD);
        entity.setStatus(StatusEnum.APPROVED_PROD);
    }

    private void validateEvidenceUrl(final String evidenceUrl) throws BusinessException {
        if (evidenceUrl == null || evidenceUrl.isBlank()) {
            throw new BusinessException("Evidence URL é obrigatória para promoção PREPROD→PROD");
        }

        try {
            URI parsed = new URI(evidenceUrl);
            String scheme = parsed.getScheme();
            if (scheme == null) {
                throw new BusinessException("Evidence URL inválida para promoção PREPROD→PROD");
            }

            boolean allowedScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!allowedScheme || parsed.getHost() == null || parsed.getHost().isBlank()) {
                throw new BusinessException("Evidence URL inválida para promoção PREPROD→PROD");
            }
        } catch (URISyntaxException ex) {
            throw new BusinessException("Evidence URL inválida para promoção PREPROD→PROD");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceScoreResponseDto calculateEvidenceScore(final UUID id) throws EntityNotFoundException {
        if (id == null) {
            throw new IllegalArgumentException("ID da release inválido");
        }

        Release entity = repository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Release não encontrada com ID: " + id));

        EvidenceScoreBreakdown breakdown = buildEvidenceScore(entity);

        return EvidenceScoreResponseDto.builder()
                .releaseId(entity.getId())
                .score(breakdown.score)
                .evidenceUrl(entity.getEvidenceUrl())
                .rationale(breakdown.rationale)
                .build();
    }

    private EvidenceScoreBreakdown buildEvidenceScore(final Release entity) {
        String evidenceUrl = entity.getEvidenceUrl();

        boolean validUrl = isValidEvidenceUrl(evidenceUrl);
        boolean reportPattern =
                containsAnyIgnoreCase(evidenceUrl, "report", "evidence", "quality", "test", "coverage");
        boolean passToken = containsAnyIgnoreCase(evidenceUrl, "pass", "passed", "result=pass", "status=pass");
        int statusWeight = statusWeight(entity.getStatus());
        int envWeight = environmentWeight(entity.getEnv());

        int normalizedScore = calculateEvidenceScoreValue(validUrl, reportPattern, passToken, statusWeight, envWeight);
        String rationale = buildEvidenceRationale(validUrl, reportPattern, passToken, statusWeight, envWeight);

        return new EvidenceScoreBreakdown(normalizedScore, rationale);
    }

    private int calculateEvidenceScoreValue(
            final boolean validUrl,
            final boolean reportPattern,
            final boolean passToken,
            final int statusWeight,
            final int envWeight) {
        int score = SCORE_MIN;
        if (validUrl) {
            score += SCORE_URL_VALID;
        }
        if (reportPattern) {
            score += SCORE_REPORT_PATTERN;
        }
        if (passToken) {
            score += SCORE_PASS_TOKEN;
        }
        score += statusWeight + envWeight;
        return Math.min(SCORE_MAX, Math.max(SCORE_MIN, score));
    }

    private String buildEvidenceRationale(
            final boolean validUrl,
            final boolean reportPattern,
            final boolean passToken,
            final int statusWeight,
            final int envWeight) {
        return String.format(
                "validUrl=%s, reportPattern=%s, passToken=%s, statusWeight=%d, envWeight=%d",
                validUrl,
                reportPattern,
                passToken,
                statusWeight,
                envWeight);
    }

    private boolean isValidEvidenceUrl(final String evidenceUrl) {
        if (evidenceUrl == null || evidenceUrl.isBlank()) {
            return false;
        }

        try {
            URI parsed = new URI(evidenceUrl);
            String scheme = parsed.getScheme();
            boolean allowedScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            return allowedScheme && parsed.getHost() != null && !parsed.getHost().isBlank();
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    private boolean containsAnyIgnoreCase(final String value, final String... tokens) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.toLowerCase();
        for (String token : tokens) {
            if (normalized.contains(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private int statusWeight(final StatusEnum status) {
        if (status == null) {
            return 0;
        }

        return switch (status) {
            case DEPLOYED, APPROVED_PROD, APPROVED_PREPROD -> SCORE_STATUS_HIGH;
            case PENDING_PROD, PENDING_PREPROD -> SCORE_STATUS_MEDIUM;
            default -> 0;
        };
    }

    private int environmentWeight(final EnvironmentEnum env) {
        if (env == null) {
            return 0;
        }

        return switch (env) {
            case PROD -> SCORE_ENV_PROD;
            case PREPROD -> SCORE_ENV_PREPROD;
            case DEV -> SCORE_ENV_DEV;
        };
    }

    private static final class EvidenceScoreBreakdown {

        private final int score;
        private final String rationale;

        private EvidenceScoreBreakdown(final int score, final String rationale) {
            this.score = score;
            this.rationale = rationale;
        }
    }
}
