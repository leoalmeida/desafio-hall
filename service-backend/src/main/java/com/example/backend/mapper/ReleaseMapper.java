package com.example.backend.mapper;

import com.example.backend.domain.entity.Release;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;

import java.time.format.DateTimeFormatter;

/**
 * Mapper para conversão entre Release e DTOs.
 */
public class ReleaseMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ReleaseMapper() {
        // Utility class
    }

    /**
     * Converte ReleaseRequestDto para entidade Release.
     *
     * @param dto DTO de request
     * @return Entidade Release
     */
    public static Release mapRequest(final ReleaseRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Release.builder()
                .applicationId(dto.getApplicationId())
                .version(dto.getVersion())
                .env(dto.getEnv())
                .status(dto.getStatus())
                .evidenceUrl(dto.getEvidenceUrl())
                .build();
    }

    /**
     * Converte entidade Release para ReleaseResponseDto.
     *
     * @param entity Entidade Release
     * @return DTO de response
     */
    public static ReleaseResponseDto mapResponse(final Release entity) {
        if (entity == null) {
            return null;
        }
        return ReleaseResponseDto.builder()
                .id(entity.getId())
                .applicationId(entity.getApplicationId())
                .version(entity.getVersion())
                .env(entity.getEnv().toString())
                .status(entity.getStatus().toString())
                .evidenceUrl(entity.getEvidenceUrl())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
                .deployedAt(
                        entity.getDeployedAt() != null ? entity.getDeployedAt().format(DATE_TIME_FORMATTER) : null)
                .build();
    }
}
