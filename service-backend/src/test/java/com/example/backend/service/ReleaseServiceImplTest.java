package com.example.backend.service;

import java.util.List;
import java.util.Optional;

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
import com.example.backend.exception.BusinessException;
import com.example.backend.policy.PolicyService;
import com.example.backend.service.impl.ReleaseServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReleaseServiceImplTest {

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
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(1L))
                .thenReturn(List.of(Approval.builder().outcome(OutcomeEnum.APPROVED).build()));

        assertDoesNotThrow(() -> service.approveRelease(1L, OutcomeEnum.APPROVED));

        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PREPROD);
        verify(policyService).validateApprovalThresholds(1L, 1L);
        assertEquals(StatusEnum.APPROVED_PREPROD, release.getStatus());
    }

    @Test
    void approveReleaseDeveRejeitarQuandoOutcomeRejected() {
        Release release = releaseComStatus(StatusEnum.PENDING_PROD, EnvironmentEnum.PROD);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.approveRelease(1L, OutcomeEnum.REJECTED));

        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PROD);
        verify(approvalRepository, never()).findByReleaseId(anyLong());
        assertEquals(StatusEnum.REJECTED, release.getStatus());
    }

    @Test
    void approveReleaseDeveFalharQuandoStatusNaoPermiteAprovacao() {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(1L))
                .thenReturn(List.of(Approval.builder().outcome(OutcomeEnum.APPROVED).build()));

        BusinessException ex =
            assertThrows(BusinessException.class, () -> service.approveRelease(1L, OutcomeEnum.APPROVED));
        assertNotNull(ex);

        verify(policyService).validateFreezeWindow(EnvironmentEnum.DEV);
        verify(policyService).validateApprovalThresholds(1L, 1L);
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveMoverParaPendingPreprodQuandoStatusCreated() {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.promoteRelease(1L));

        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PREPROD);
        assertEquals(EnvironmentEnum.PREPROD, release.getEnv());
        assertEquals(StatusEnum.PENDING_PREPROD, release.getStatus());
    }

    @Test
    void promoteReleaseDeveMoverParaProdQuandoStatusApprovedPreprodComRegrasAtendidas() {
        Release release = releaseComStatus(StatusEnum.APPROVED_PREPROD, EnvironmentEnum.PREPROD);
        release.setEvidenceUrl("https://example.com/evidence");
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(1L)).thenReturn(List.of(
            Approval.builder().outcome(OutcomeEnum.APPROVED).build(),
            Approval.builder().outcome(OutcomeEnum.APPROVED).build()));

        assertDoesNotThrow(() -> service.promoteRelease(1L));

        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PROD);
        verify(policyService).validateApprovalThresholds(2L, 2L);
        assertEquals(EnvironmentEnum.PROD, release.getEnv());
        assertEquals(StatusEnum.APPROVED_PROD, release.getStatus());
    }

    @Test
    void promoteReleaseDeveFalharQuandoStatusNaoPermitePromocao() {
        Release release = releaseComStatus(StatusEnum.REJECTED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.promoteRelease(1L));
        assertNotNull(ex);

        verify(policyService, never()).validateFreezeWindow(any(EnvironmentEnum.class));
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveFalharQuandoEvidenceUrlInvalidaNoFluxoPreprodProd() {
        Release release = releaseComStatus(StatusEnum.APPROVED_PREPROD, EnvironmentEnum.PREPROD);
        release.setEvidenceUrl("not-a-url");
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.promoteRelease(1L));
        assertNotNull(ex);
        verify(approvalRepository, never()).findByReleaseId(anyLong());
        verify(policyService, never()).validateApprovalThresholds(anyLong(), anyLong());
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDevePropagarBloqueioPorFreezeWindow() throws BusinessException {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        org.mockito.Mockito.doThrow(new BusinessException("freeze ativo"))
                .when(policyService)
                .validateFreezeWindow(EnvironmentEnum.PREPROD);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.promoteRelease(1L));
        assertNotNull(ex);
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.promoteRelease(1L));
        assertNotNull(ex);
    }

    @Test
    void approveReleaseDeveAprovarQuandoStatusPendenteProd() {
        Release release = releaseComStatus(StatusEnum.PENDING_PROD, EnvironmentEnum.PROD);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(1L)).thenReturn(List.of(
            Approval.builder().outcome(OutcomeEnum.APPROVED).build(),
            Approval.builder().outcome(OutcomeEnum.REJECTED).build()));

        assertDoesNotThrow(() -> service.approveRelease(1L, OutcomeEnum.APPROVED));

        verify(releaseRepository).saveAndFlush(any(Release.class));
        verify(policyService).validateFreezeWindow(EnvironmentEnum.PROD);
        verify(policyService).validateApprovalThresholds(1L, 2L);
        assertEquals(StatusEnum.APPROVED_PROD, release.getStatus());
    }

    @Test
    void approveReleaseDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex =
            assertThrows(EntityNotFoundException.class, () -> service.approveRelease(1L, OutcomeEnum.APPROVED));
        assertNotNull(ex);
    }

    @Test
    void approveReleaseDevePropagarErroDaPolicy() throws BusinessException {
        Release release = releaseComStatus(StatusEnum.PENDING_PREPROD, EnvironmentEnum.PREPROD);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(approvalRepository.findByReleaseId(1L))
                .thenReturn(List.of(Approval.builder().outcome(OutcomeEnum.APPROVED).build()));
        org.mockito.Mockito.doThrow(new BusinessException("Policy bloqueou"))
                .when(policyService)
                .validateApprovalThresholds(1L, 1L);

        BusinessException ex =
            assertThrows(BusinessException.class, () -> service.approveRelease(1L, OutcomeEnum.APPROVED));
        assertNotNull(ex);
        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void calculateEvidenceScoreDeveRetornar100QuandoEvidenciaForte() {
        Release release = releaseComStatus(StatusEnum.DEPLOYED, EnvironmentEnum.PROD);
        release.setEvidenceUrl("https://ci.example.com/reports/rel-1?result=PASS");
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        EvidenceScoreResponseDto result = service.calculateEvidenceScore(1L);

        assertNotNull(result);
        assertEquals(100, result.getScore());
        assertEquals(1L, result.getReleaseId());
    }

    @Test
    void calculateEvidenceScoreDeveRetornar5QuandoSemEvidenceUrlNoDev() {
        Release release = releaseComStatus(StatusEnum.CREATED, EnvironmentEnum.DEV);
        release.setEvidenceUrl(null);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        EvidenceScoreResponseDto result = service.calculateEvidenceScore(1L);

        assertNotNull(result);
        assertEquals(5, result.getScore());
    }

    @Test
    void calculateEvidenceScoreDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.calculateEvidenceScore(1L));
        assertNotNull(ex);
    }

    @Test
    void calculateEvidenceScoreDeveAplicarPontuacaoDeterministicaIntermediaria() {
        Release release = releaseComStatus(StatusEnum.PENDING_PREPROD, EnvironmentEnum.PREPROD);
        release.setEvidenceUrl("https://example.com/evidence/run-42");
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        EvidenceScoreResponseDto result = service.calculateEvidenceScore(1L);

        assertNotNull(result);
        assertEquals(72, result.getScore());
    }

    private static Release releaseComStatus(final StatusEnum status, final EnvironmentEnum env) {
        return Release.builder().id(1L).status(status).env(env).build();
    }
}
