package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.Release;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.domain.repository.ReleaseRepository;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.EntityNotFoundException;
import com.example.backend.service.impl.ReleaseServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReleaseServiceImplTest {

    @Mock
    private ReleaseRepository releaseRepository;

    private ReleaseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReleaseServiceImpl(releaseRepository);
    }

    @Test
    void approveReleaseDeveAprovarQuandoStatusPendentePreprod() {
        Release release = releaseComStatus(StatusEnum.PENDING_PREPROD);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.approveRelease(1L, OutcomeEnum.APPROVED));

        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releaseRepository).saveAndFlush(captor.capture());
        assertEquals(StatusEnum.APPROVED_PREPROD, captor.getValue().getStatus());
    }

    @Test
    void approveReleaseDeveRejeitarQuandoOutcomeRejected() {
        Release release = releaseComStatus(StatusEnum.PENDING_PROD);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.approveRelease(1L, OutcomeEnum.REJECTED));

        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releaseRepository).saveAndFlush(captor.capture());
        assertEquals(StatusEnum.REJECTED, captor.getValue().getStatus());
    }

    @Test
    void approveReleaseDeveFalharQuandoStatusNaoPermiteAprovacao() {
        Release release = releaseComStatus(StatusEnum.CREATED);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertThrows(BusinessException.class, () -> service.approveRelease(1L, OutcomeEnum.APPROVED));

        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveMoverParaPendingPreprodQuandoStatusCreated() {
        Release release = releaseComStatus(StatusEnum.CREATED);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.promoteRelease(1L));

        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releaseRepository).saveAndFlush(captor.capture());
        assertEquals(StatusEnum.PENDING_PREPROD, captor.getValue().getStatus());
    }

    @Test
    void promoteReleaseDeveMoverParaDeployedQuandoStatusApprovedProd() {
        Release release = releaseComStatus(StatusEnum.APPROVED_PROD);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.promoteRelease(1L));

        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releaseRepository).saveAndFlush(captor.capture());
        assertEquals(StatusEnum.DEPLOYED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getDeployedAt());
    }

    @Test
    void promoteReleaseDeveFalharQuandoStatusNaoPermitePromocao() {
        Release release = releaseComStatus(StatusEnum.REJECTED);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertThrows(BusinessException.class, () -> service.promoteRelease(1L));

        verify(releaseRepository, never()).saveAndFlush(release);
    }

    @Test
    void promoteReleaseDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.promoteRelease(1L));
    }

    @Test
    void approveReleaseDeveAprovarQuandoStatusPendenteProd() {
        Release release = releaseComStatus(StatusEnum.PENDING_PROD);
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));

        assertDoesNotThrow(() -> service.approveRelease(1L, OutcomeEnum.APPROVED));

        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releaseRepository).saveAndFlush(captor.capture());
        assertEquals(StatusEnum.APPROVED_PROD, captor.getValue().getStatus());
    }

    @Test
    void approveReleaseDeveFalharQuandoNaoEncontrarRelease() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.approveRelease(1L, OutcomeEnum.APPROVED));
    }

    private static Release releaseComStatus(final StatusEnum status) {
        return Release.builder().id(1L).status(status).build();
    }
}
