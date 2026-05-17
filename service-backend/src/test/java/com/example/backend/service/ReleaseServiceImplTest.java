package com.example.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.Release;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.domain.repository.ApprovalRepository;
import com.example.backend.domain.repository.ReleaseRepository;
import com.example.backend.dto.EvidenceScoreResponseDto;
import com.example.backend.dto.ReleaseEvidenceUpdateRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.policy.PolicyService;
import com.example.backend.service.impl.ReleaseServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReleaseServiceImplTest {

    private static final UUID RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private PolicyService policyService;

    private ReleaseServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new ReleaseServiceImpl(releaseRepository, approvalRepository, policyService);
    }

    @Test
    void approveReleaseDeveAprovarQuandoStatusPendentePreprod() {
        Release release = releaseComStatus(StatusEnum.PENDING_PREPROD, EnvironmentEnum.PREPROD);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(RELEASE_ID))
                .thenReturn(List.of(Approval.builder().outcome(OutcomeEnum.APPROVED).build()));

        assertDoesNotThrow(() -> service.approveRelease(RELEASE_ID, OutcomeEnum.APPROVED,
            "approver@example.com", "Liberado"));

        verify(approvalRepository).saveAndFlush(any(Approval.class));
        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PREPROD);
        verify(policyService).validateApprovalThresholds(2L, 2L);
        assertEquals(StatusEnum.APPROVED_PREPROD, release.getStatus());
    }

    @Test
    void approveReleaseDeveRejeitarQuandoOutcomeRejected() {
        Release release = releaseComStatus(StatusEnum.PENDING_PROD, EnvironmentEnum.PROD);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.approveRelease(RELEASE_ID, OutcomeEnum.REJECTED,
                "approver@example.com", "Reprovado"));

        verify(approvalRepository).findByReleaseId(RELEASE_ID);
        verify(approvalRepository).saveAndFlush(any(Approval.class));
        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PROD);
        assertEquals(StatusEnum.REJECTED, release.getStatus());
    }

    @Test
    void approveReleaseDeveFalharQuandoStatusNaoPermiteAprovacao() {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        BusinessException ex =
            assertThrows(BusinessException.class, () -> service.approveRelease(RELEASE_ID, OutcomeEnum.APPROVED,
                    "approver@example.com", "Liberado"));
        assertNotNull(ex);

        verify(policyService).validateFreezeWindow(EnvironmentEnum.DEV);
        verify(approvalRepository, never()).findByReleaseId(any(UUID.class));
        verify(approvalRepository, never()).saveAndFlush(any(Approval.class));
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveMoverParaPendingPreprodQuandoStatusCreated() {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.promoteRelease(RELEASE_ID));

        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PREPROD);
        assertEquals(EnvironmentEnum.PREPROD, release.getEnv());
        assertEquals(StatusEnum.PENDING_PREPROD, release.getStatus());
    }

    @Test
    void promoteReleaseDeveMoverParaProdQuandoStatusApprovedPreprodComRegrasAtendidas() {
        Release release = releaseComStatus(StatusEnum.APPROVED_PREPROD, EnvironmentEnum.PREPROD);
        release.setEvidenceUrl("https://example.com/evidence");
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(RELEASE_ID)).thenReturn(List.of(
            Approval.builder().outcome(OutcomeEnum.APPROVED).build(),
            Approval.builder().outcome(OutcomeEnum.APPROVED).build()));

        assertDoesNotThrow(() -> service.promoteRelease(RELEASE_ID));

        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PROD);
        verify(policyService).validateApprovalThresholds(2L, 2L);
        assertEquals(EnvironmentEnum.PROD, release.getEnv());
        assertEquals(StatusEnum.APPROVED_PROD, release.getStatus());
    }

    @Test
    void promoteReleaseDeveFalharQuandoStatusNaoPermitePromocao() {
        Release release = releaseComStatus(StatusEnum.REJECTED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.promoteRelease(RELEASE_ID));
        assertNotNull(ex);

        verify(policyService, never()).validateFreezeWindow(any(EnvironmentEnum.class));
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveFalharQuandoEvidenceUrlInvalidaNoFluxoPreprodProd() {
        Release release = releaseComStatus(StatusEnum.APPROVED_PREPROD, EnvironmentEnum.PREPROD);
        release.setEvidenceUrl("not-a-url");
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.promoteRelease(RELEASE_ID));
        assertNotNull(ex);
        verify(approvalRepository, never()).findByReleaseId(any(UUID.class));
        verify(policyService, never()).validateApprovalThresholds(anyLong(), anyLong());
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDevePropagarBloqueioPorFreezeWindow() throws BusinessException {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));
        org.mockito.Mockito.doThrow(new BusinessException("freeze ativo"))
                .when(policyService)
                .validateFreezeWindow(EnvironmentEnum.PREPROD);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.promoteRelease(RELEASE_ID));
        assertNotNull(ex);
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.promoteRelease(RELEASE_ID));
        assertNotNull(ex);
    }

    @Test
    void approveReleaseDeveAprovarQuandoStatusPendenteProd() {
        Release release = releaseComStatus(StatusEnum.PENDING_PROD, EnvironmentEnum.PROD);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(RELEASE_ID)).thenReturn(List.of(
            Approval.builder().outcome(OutcomeEnum.APPROVED).build(),
            Approval.builder().outcome(OutcomeEnum.REJECTED).build()));

        assertDoesNotThrow(() -> service.approveRelease(RELEASE_ID, OutcomeEnum.APPROVED,
                "approver@example.com", "Seguiu para PROD"));

        verify(approvalRepository).saveAndFlush(any(Approval.class));
        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PROD);
        verify(policyService).validateApprovalThresholds(2L, 3L);
        assertEquals(StatusEnum.APPROVED_PROD, release.getStatus());
    }

    @Test
    void approveReleaseDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException ex =
            assertThrows(EntityNotFoundException.class, () -> service.approveRelease(RELEASE_ID, OutcomeEnum.APPROVED,
                    "approver@example.com", "Liberado"));
        assertNotNull(ex);
    }

    @Test
    void approveReleaseDevePropagarErroDaPolicy() throws BusinessException {
        Release release = releaseComStatus(StatusEnum.PENDING_PREPROD, EnvironmentEnum.PREPROD);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(RELEASE_ID))
                .thenReturn(List.of(Approval.builder().outcome(OutcomeEnum.APPROVED).build()));
        org.mockito.Mockito.doThrow(new BusinessException("Policy bloqueou"))
                .when(policyService)
                .validateApprovalThresholds(2L, 2L);

        BusinessException ex =
            assertThrows(BusinessException.class, () -> service.approveRelease(RELEASE_ID, OutcomeEnum.APPROVED,
                    "approver@example.com", "Liberado"));
        assertNotNull(ex);
        verify(approvalRepository, never()).saveAndFlush(any(Approval.class));
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void calculateEvidenceScoreDeveRetornar100QuandoEvidenciaForte() {
        Release release = releaseComStatus(StatusEnum.DEPLOYED, EnvironmentEnum.PROD);
        release.setEvidenceUrl("https://ci.example.com/reports/rel-1?result=PASS");
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        EvidenceScoreResponseDto result = service.calculateEvidenceScore(RELEASE_ID);

        assertNotNull(result);
        assertEquals(100, result.getScore());
        assertEquals(RELEASE_ID, result.getReleaseId());
    }

    @Test
    void calculateEvidenceScoreDeveRetornar5QuandoSemEvidenceUrlNoDev() {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        release.setEvidenceUrl(null);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        EvidenceScoreResponseDto result = service.calculateEvidenceScore(RELEASE_ID);

        assertNotNull(result);
        assertEquals(5, result.getScore());
    }

    @Test
    void calculateEvidenceScoreDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.calculateEvidenceScore(RELEASE_ID));
        assertNotNull(ex);
    }

    @Test
    void calculateEvidenceScoreDeveAplicarPontuacaoDeterministicaIntermediaria() {
        Release release = releaseComStatus(StatusEnum.PENDING_PREPROD, EnvironmentEnum.PREPROD);
        release.setEvidenceUrl("https://example.com/evidence/run-42");
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));

        EvidenceScoreResponseDto result = service.calculateEvidenceScore(RELEASE_ID);

        assertNotNull(result);
        assertEquals(72, result.getScore());
    }

    @Test
    void updateEvidenceUrlDeveAtualizarReleaseQuandoValida() {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(RELEASE_ID)).thenReturn(Optional.of(release));
        when(releaseRepository.saveAndFlush(release)).thenReturn(release);

        ReleaseEvidenceUpdateRequestDto request = ReleaseEvidenceUpdateRequestDto.builder()
                .evidenceUrl("https://example.com/new-evidence")
                .build();

        ReleaseResponseDto result = service.updateEvidenceUrl(RELEASE_ID, request);

        assertNotNull(result);
        assertEquals("https://example.com/new-evidence", release.getEvidenceUrl());
        verify(releaseRepository).saveAndFlush(release);
    }

    @Test
    void updateEvidenceUrlDeveFalharQuandoEvidenceUrlVazia() {
        ReleaseEvidenceUpdateRequestDto request = ReleaseEvidenceUpdateRequestDto.builder()
                .evidenceUrl(" ")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateEvidenceUrl(RELEASE_ID, request));

        assertNotNull(ex);
        verify(releaseRepository, never()).saveAndFlush(any(Release.class));
    }

    private static Release releaseComStatus(final StatusEnum status, final EnvironmentEnum env) {
        return Release.builder().id(RELEASE_ID).status(status).env(env).build();
    }
}
