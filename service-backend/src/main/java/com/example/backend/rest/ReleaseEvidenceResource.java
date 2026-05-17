package com.example.backend.rest;

import com.example.backend.dto.EvidenceScoreResponseDto;
import com.example.backend.dto.ReleaseEvidenceUpdateRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.service.ReleaseAuditService;
import com.example.backend.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para endpoints de evidência de release.
 */
@RestController
@CrossOrigin(
        origins = {"http://localhost:80", "http://localhost:4200"},
        maxAge = RestConstants.CORS_MAX_AGE)
@RequestMapping("/api/releases")
@Tag(name = "Release Evidence", description = "Endpoint de evidência e scoring de releases")
@Validated
public class ReleaseEvidenceResource {

    private final ReleaseService releaseService;
    private final ReleaseAuditService releaseAuditService;

    public ReleaseEvidenceResource(
            final ReleaseService releaseService,
            final ReleaseAuditService releaseAuditService) {
        this.releaseService = Objects.requireNonNull(releaseService, "releaseService não pode ser nulo");
        this.releaseAuditService = Objects.requireNonNull(
                releaseAuditService,
                "releaseAuditService não pode ser nulo");
    }

    @Operation(summary = "Atualizar evidenceUrl da release")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evidence URL atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Release não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping(value = "/{id}/evidence-url", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReleaseResponseDto> updateEvidenceUrl(
            @Parameter(description = "ID da release", required = true,
                example = "10000000-0000-0000-0000-000000000001") @PathVariable final UUID id,
            @RequestBody final ReleaseEvidenceUpdateRequestDto dto) {
        ReleaseResponseDto result = releaseService.updateEvidenceUrl(id, dto);
        releaseAuditService.logChangeEvidenceUrl(id, dto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Calcular score de evidência da release")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Score calculado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Release não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/{id}/evidence-score", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER','VIEWER')")
    public ResponseEntity<EvidenceScoreResponseDto> evidenceScore(
            @Parameter(description = "ID da release", required = true,
                example = "10000000-0000-0000-0000-000000000001") @PathVariable final UUID id) {
        releaseAuditService.logEvidenceScore(id);
        EvidenceScoreResponseDto result = releaseService.calculateEvidenceScore(id);
        return ResponseEntity.ok(result);
    }
}
