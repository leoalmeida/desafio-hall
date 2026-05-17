package com.example.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.dto.ApprovalRequestDto;
import com.example.backend.dto.ApprovalResponseDto;

class ApprovalMapperTest {

    @Test
    void mapRequestDeveRetornarNullQuandoDtoForNull() {
        assertNull(ApprovalMapper.mapRequest(null));
    }

    @Test
    void mapRequestDeveMapearCamposCorretamente() {
        ApprovalRequestDto dto = ApprovalRequestDto.builder()
                .releaseId(10L)
                .approverEmail("approver@email.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("aprovado")
                .build();

        Approval result = ApprovalMapper.mapRequest(dto);

        assertNotNull(result);
        assertEquals(10L, result.getReleaseId());
        assertEquals("approver@email.com", result.getApproverEmail());
        assertEquals(OutcomeEnum.APPROVED, result.getOutcome());
        assertEquals("aprovado", result.getNotes());
    }

    @Test
    void mapResponseDeveRetornarNullQuandoEntidadeForNull() {
        assertNull(ApprovalMapper.mapResponse(null));
    }

    @Test
    void mapResponseDeveMapearCamposCorretamente() {
        Approval entity = Approval.builder()
                .id(1L)
                .releaseId(10L)
                .approverEmail("approver@email.com")
                .outcome(OutcomeEnum.REJECTED)
                .notes("reprovado")
                .timestamp(LocalDateTime.of(2025, 1, 2, 3, 4, 5))
                .build();

        ApprovalResponseDto result = ApprovalMapper.mapResponse(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10L, result.getReleaseId());
        assertEquals("approver@email.com", result.getApproverEmail());
        assertEquals("REJECTED", result.getOutcome());
        assertEquals("reprovado", result.getNotes());
        assertEquals("2025-01-02T03:04:05", result.getTimestamp());
    }
}
