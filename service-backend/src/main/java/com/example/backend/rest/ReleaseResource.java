package com.example.backend.rest;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.dto.EvidenceScoreResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.SecurityContextUtils;
import com.example.backend.service.ReleaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller para endpoints de Release.
 */
@RestController
@CrossOrigin(
        origins = {"http://localhost:80", "http://localhost:4200"},
        maxAge = RestConstants.CORS_MAX_AGE)
@RequestMapping("/api/releases")
@Tag(name = "Releases", description = "Endpoint de gestão de releases")
@Slf4j
@Validated
public class ReleaseResource {

    private final ReleaseService releaseService;
    private final AuditLogManager auditLogManager;

    @Autowired
    public ReleaseResource(final ReleaseService service, final AuditLogManager auditLogManager) {
        this.releaseService = Objects.requireNonNull(service, "releaseService não pode ser nulo");
        this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager não pode ser nulo");
    }

    @Operation(summary = "Listar releases")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de releases retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER','VIEWER')")
    public ResponseEntity<List<ReleaseResponseDto>> find(
            @Parameter(description = "ID da aplicação", required = true) @RequestParam final Long applicationId,
            @Parameter(description = "Versão da release", required = true) @RequestParam final String version,
            @Parameter(description = "Ambiente da release", required = true) @RequestParam
                    final EnvironmentEnum environment,
            @Parameter(description = "Status da release", required = true) @RequestParam final StatusEnum status) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND", "Release", null,
                String.format("{applicationId:%d, version:%s, environment:%s, status:%s}",
                        applicationId, version, environment, status));
        List<ReleaseResponseDto> result = releaseService.find(applicationId, version, environment, status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Criar nova release")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Release criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReleaseResponseDto> create(@RequestBody final ReleaseRequestDto dto) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "CREATE", "Release", null,
                auditLogManager.toJsonNode(dto));
        ReleaseResponseDto result = releaseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Aprovar release")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Release aprovada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Release não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(value = "/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<Void> approve(
            @Parameter(description = "ID da release", required = true) @PathVariable final Long id) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "APPROVE", "Release", id.intValue(),
            auditLogManager.toJsonNode("releaseId",id.toString()));
        releaseService.approveRelease(id, OutcomeEnum.APPROVED);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reprovar release")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Release reprovada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Release não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(value = "/{id}/disapprove")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<Void> disapprove(
            @Parameter(description = "ID da release", required = true) @PathVariable final Long id) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "DISAPPROVE", "Release",
            id.intValue(), auditLogManager.toJsonNode("ReleaseID", id.toString()));
        releaseService.approveRelease(id, OutcomeEnum.REJECTED);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Promover release para próximo ambiente")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Release promovida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Release não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(value = "/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> promote(
            @Parameter(description = "ID da release", required = true) @PathVariable final Long id) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "PROMOTE", "Release", id.intValue(),
                auditLogManager.toJsonNode("ReleaseId",id.toString()));
        releaseService.promoteRelease(id);
        return ResponseEntity.noContent().build();
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
            @Parameter(description = "ID da release", required = true) @PathVariable final Long id) {
        auditLogManager.logAction(
            SecurityContextUtils.getCurrentUserEmail(), "EVIDENCE_SCORE", "Release", id.intValue(),
                auditLogManager.toJsonNode("ReleaseId", id.toString()));
        EvidenceScoreResponseDto result = releaseService.calculateEvidenceScore(id);
        return ResponseEntity.ok(result);
    }

}
