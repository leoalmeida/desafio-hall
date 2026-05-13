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
import com.example.backend.exception.EntityNotFoundException;
import com.example.backend.service.impl.ApprovalServiceImpl;


@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

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
                .releaseId(1L)
                .approverEmail("approver@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("Aprovado")
                .build();

        Approval saved = Approval.builder()
                .id(10L)
                .releaseId(1L)
                .approverEmail("approver@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("Aprovado")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.saveAndFlush(any(Approval.class))).thenReturn(saved);

        ApprovalResponseDto response = service.create(dto);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getReleaseId());
        assertEquals("approver@example.com", response.getApproverEmail());
        verify(repository).saveAndFlush(any(Approval.class));
    }

    // ---- findById ----

    @Test
    void findByIdDeveThrowIllegalArgumentExceptionQuandoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }

    @Test
    void findByIdDeveThrowIllegalArgumentExceptionQuandoIdZero() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(0L));
    }

    @Test
    void findByIdDeveThrowEntityNotFoundExceptionQuandoNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findByIdDeveRetornarDtoQuandoExistir() throws EntityNotFoundException {
        Approval approval = Approval.builder()
                .id(1L)
                .releaseId(5L)
                .approverEmail("user@example.com")
                .outcome(OutcomeEnum.REJECTED)
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(approval));

        ApprovalResponseDto response = service.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getApproverEmail());
    }

    // ---- findByReleaseId ----

    @Test
    void findByReleaseIdDeveThrowIllegalArgumentExceptionQuandoIdNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.findByReleaseId(null));
    }

    @Test
    void findByReleaseIdDeveThrowIllegalArgumentExceptionQuandoIdNegativo() {
        assertThrows(IllegalArgumentException.class, () -> service.findByReleaseId(-1L));
    }

    @Test
    void findByReleaseIdDeveRetornarListaVaziaQuandoSemRegistros() throws BusinessException {
        when(repository.findByReleaseId(1L)).thenReturn(List.of());

        List<ApprovalResponseDto> result = service.findByReleaseId(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void findByReleaseIdDeveRetornarAprovacoesDaRelease() throws BusinessException {
        Approval a1 = Approval.builder()
                .id(1L)
                .releaseId(10L)
                .approverEmail("a@x.com")
                .outcome(OutcomeEnum.APPROVED)
                .timestamp(LocalDateTime.now())
                .build();
        Approval a2 = Approval.builder()
                .id(2L)
                .releaseId(10L)
                .approverEmail("b@x.com")
                .outcome(OutcomeEnum.REJECTED)
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findByReleaseId(10L)).thenReturn(List.of(a1, a2));

        List<ApprovalResponseDto> result = service.findByReleaseId(10L);

        assertEquals(2, result.size());
    }
}
