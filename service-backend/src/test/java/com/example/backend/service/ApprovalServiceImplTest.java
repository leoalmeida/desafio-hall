package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.repository.ApprovalRepository;
import com.example.backend.dto.ApprovalRequestDto;
import com.example.backend.dto.ApprovalResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.impl.ApprovalServiceImpl;

import jakarta.persistence.EntityNotFoundException;


@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    private static final UUID APPROVAL_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID APPROVAL_ID_2 = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID APPROVAL_ID_10 = UUID.fromString("20000000-0000-0000-0000-000000000010");
    private static final UUID RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID RELEASE_ID_5 = UUID.fromString("10000000-0000-0000-0000-000000000005");
    private static final UUID RELEASE_ID_10 = UUID.fromString("10000000-0000-0000-0000-000000000010");
    private static final UUID MISSING_APPROVAL_ID = UUID.fromString("20000000-0000-0000-0000-000000000099");

    @Mock
    private ApprovalRepository repository;

    private ApprovalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApprovalServiceImpl(repository);
    }

    // ---- create ----

    @Test
    void createDeveThrowBusinessExceptionQuandoDtoNulo() {
        assertThrows(BusinessException.class, () -> service.create(null));
    }

    @Test
    void createDeveSalvarERetornarApprovalResponseDto() throws BusinessException {
        ApprovalRequestDto dto = ApprovalRequestDto.builder()
            .releaseId(RELEASE_ID)
                .approverEmail("approver@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("Aprovado")
                .build();

        Approval saved = Approval.builder()
            .id(APPROVAL_ID_10)
            .releaseId(RELEASE_ID)
                .approverEmail("approver@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("Aprovado")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.saveAndFlush(any(Approval.class))).thenReturn(saved);

        ApprovalResponseDto response = service.create(dto);

        assertNotNull(response);
        assertEquals(APPROVAL_ID_10, response.getId());
        assertEquals(RELEASE_ID, response.getReleaseId());
        assertEquals("approver@example.com", response.getApproverEmail());
        verify(repository).saveAndFlush(any(Approval.class));
    }

    // ---- findById ----

    @Test
    void findByIdDeveThrowIllegalArgumentExceptionQuandoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }

    @Test
    void findByIdDeveThrowIllegalArgumentExceptionQuandoIdAusente() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }

    @Test
    void findByIdDeveThrowEntityNotFoundExceptionQuandoNaoExistir() {
        when(repository.findById(MISSING_APPROVAL_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(MISSING_APPROVAL_ID));
    }

    @Test
    void findByIdDeveRetornarDtoQuandoExistir() throws EntityNotFoundException {
        Approval approval = Approval.builder()
                .id(APPROVAL_ID)
                .releaseId(RELEASE_ID_5)
                .approverEmail("user@example.com")
                .outcome(OutcomeEnum.REJECTED)
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findById(APPROVAL_ID)).thenReturn(Optional.of(approval));

        ApprovalResponseDto response = service.findById(APPROVAL_ID);

        assertNotNull(response);
        assertEquals(APPROVAL_ID, response.getId());
        assertEquals("user@example.com", response.getApproverEmail());
    }

    // ---- findByReleaseId ----

    @Test
    void findByReleaseIdDeveThrowIllegalArgumentExceptionQuandoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.findByReleaseId(null));
    }

    @Test
    void findByReleaseIdDeveThrowIllegalArgumentExceptionQuandoIdAusente() {
        assertThrows(IllegalArgumentException.class, () -> service.findByReleaseId(null));
    }

    @Test
    void findByReleaseIdDeveRetornarListaVaziaQuandoSemRegistros() throws BusinessException {
        when(repository.findByReleaseId(RELEASE_ID)).thenReturn(List.of());

        List<ApprovalResponseDto> result = service.findByReleaseId(RELEASE_ID);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void findByReleaseIdDeveRetornarAprovacoesDaRelease() throws BusinessException {
        Approval a1 = Approval.builder()
            .id(APPROVAL_ID)
            .releaseId(RELEASE_ID_10)
                .approverEmail("a@x.com")
                .outcome(OutcomeEnum.APPROVED)
                .timestamp(LocalDateTime.now())
                .build();
        Approval a2 = Approval.builder()
            .id(APPROVAL_ID_2)
            .releaseId(RELEASE_ID_10)
                .approverEmail("b@x.com")
                .outcome(OutcomeEnum.REJECTED)
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findByReleaseId(RELEASE_ID_10)).thenReturn(List.of(a1, a2));

        List<ApprovalResponseDto> result = service.findByReleaseId(RELEASE_ID_10);

        assertEquals(2, result.size());
    }
}
