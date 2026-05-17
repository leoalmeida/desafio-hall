package com.example.backend.rest;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.dto.ApprovalResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.SecurityContextUtils;
import com.example.backend.service.ApprovalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para endpoints de AuditLog.
 */
@RestController
@CrossOrigin(origins = { "http://localhost:80", "http://localhost:4200" }, maxAge = RestConstants.CORS_MAX_AGE)
@RequestMapping("/api/approvals")
@Tag(name = "Aprovações", description = "Endpoint de gestão de aprovações")
@Slf4j
@Validated
public class ApprovalResource {

    private final ApprovalService approvalService;
    private final AuditLogManager auditLogManager;

    @Autowired
    public ApprovalResource(final ApprovalService approvalService, final AuditLogManager auditLogManager) {
        this.approvalService = Objects.requireNonNull(approvalService, "approvalService não pode ser nulo");
        this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager não pode ser nulo");
    }

    @Operation(summary = "Listar todos os registros de aprovações")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de registros retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<List<ApprovalResponseDto>> findAll() {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "LIST_ALL_APPROVALS", "Approval", null,
                null);
        List<ApprovalResponseDto> result = approvalService.findAll();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar registros por aprovador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registros encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/approver", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<List<ApprovalResponseDto>> findByApprover(
            @Parameter(description = "Login do aprovador", required = true) @RequestParam final String aprovador) {
        
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND_BY_APPROVER", "Approval", null,
            auditLogManager.toJsonNode("aprovador", aprovador));
        List<ApprovalResponseDto> result = approvalService.findByApprover(aprovador);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar registros por release")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registros encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/release", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<List<ApprovalResponseDto>> buscarPorRelease(
            @Parameter(description = "ID da release", required = true) @RequestParam final Long releaseId) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND_BY_RELEASE_ID", "ApprovalResource",
            releaseId.intValue(), auditLogManager.toJsonNode("releaseId", releaseId.toString()));
        List<ApprovalResponseDto> result = approvalService.findByReleaseId(releaseId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar registros por outcome")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registros encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/outcome", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<List<ApprovalResponseDto>> findByOutcome(
            @Parameter(description = "Nome do outcome", required = true) @RequestParam final String outcome) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND_BY_OUTCOME", "Approval", null,
                auditLogManager.toJsonNode("outcome", outcome));
        List<ApprovalResponseDto> result = approvalService.findByOutcome(OutcomeEnum.valueOf(outcome.toUpperCase()));
        return ResponseEntity.ok(result);
    }

}
