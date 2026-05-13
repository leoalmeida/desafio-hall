package com.example.backend.mapper;

import com.example.backend.domain.entity.Approval;
import com.example.backend.dto.ApprovalRequestDto;
import com.example.backend.dto.ApprovalResponseDto;

import java.time.format.DateTimeFormatter;

/**
 * Mapper para conversão entre Approval e DTOs.
 */
public class ApprovalMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ApprovalMapper() {
        // Utility class
    }

    /**
     * Converte ApprovalRequestDto para entidade Approval.
     *
     * @param dto DTO de request
     * @return Entidade Approval
     */
    public static Approval mapRequest(final ApprovalRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Approval.builder()
                .releaseId(dto.getReleaseId())
                .approverEmail(dto.getApproverEmail())
                .outcome(dto.getOutcome())
                .notes(dto.getNotes())
                .build();
    }

    /**
     * Converte entidade Approval para ApprovalResponseDto.
     *
     * @param entity Entidade Approval
     * @return DTO de response
     */
    public static ApprovalResponseDto mapResponse(final Approval entity) {
        if (entity == null) {
            return null;
        }
        return ApprovalResponseDto.builder()
                .id(entity.getId())
                .releaseId(entity.getReleaseId())
                .approverEmail(entity.getApproverEmail())
                .outcome(entity.getOutcome().toString())
                .notes(entity.getNotes())
                .timestamp(entity.getTimestamp() != null ? entity.getTimestamp().format(DATE_TIME_FORMATTER) : null)
                .build();
    }
}
