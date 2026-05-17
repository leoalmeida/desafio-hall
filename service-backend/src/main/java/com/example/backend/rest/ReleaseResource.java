package com.example.backend.rest;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseApprovalDecisionRequestDto;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.service.ReleaseAuditService;
import com.example.backend.service.ReleasePromotionCoordinator;
import com.example.backend.service.ReleaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Controller para endpoints de Release.
 */
@RestController
@CrossOrigin(
        origins = {"http://localhost:80", "http://localhost:4200"},
        maxAge = RestConstants.CORS_MAX_AGE)
@RequestMapping("/api/releases")
@Tag(name = "Releases", description = "Endpoint de gestão de releases")
public class ReleaseResource {

    private final ReleaseService releaseService;
        private final ReleaseAuditService releaseAuditService;
    private final ReleasePromotionCoordinator releasePromotionCoordinator;

    public ReleaseResource(
            final ReleaseService service,
            final ReleaseAuditService releaseAuditService,
            final ReleasePromotionCoordinator releasePromotionCoordinator) {
        this.releaseService = Objects.requireNonNull(service, "releaseService não pode ser nulo");
        this.releaseAuditService = Objects.requireNonNull(
            releaseAuditService,
            "releaseAuditService não pode ser nulo");
        this.releasePromotionCoordinator = Objects.requireNonNull(
                releasePromotionCoordinator,
                "releasePromotionCoordinator não pode ser nulo");
    }

    @Operation(summary = "Listar releases")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de releases retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER','VIEWER')")
    public ResponseEntity<List<ReleaseResponseDto>> find(
            @Parameter(description = "ID da aplicação", required = true,
                example = "00000000-0000-0000-0000-000000000001") @RequestParam final UUID applicationId,
            @Parameter(description = "Versão da release", required = true) @RequestParam final String version,
            @Parameter(description = "Ambiente da release", required = true) @RequestParam
                    final EnvironmentEnum environment,
            @Parameter(description = "Status da release", required = true) @RequestParam final StatusEnum status) {
        releaseAuditService.logFind(applicationId, version, environment, status);
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
        releaseAuditService.logCreate(dto);
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
            @Parameter(description = "ID da release", required = true,
                example = "10000000-0000-0000-0000-000000000001") @PathVariable final UUID id,
            @RequestBody(required = false) final ReleaseApprovalDecisionRequestDto dto) {
        releaseAuditService.logApprove(id, dto);
        releaseService.approveRelease(id, OutcomeEnum.APPROVED, null,
                dto == null ? null : dto.getNotes());
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
            @Parameter(description = "ID da release", required = true,
                example = "10000000-0000-0000-0000-000000000001") @PathVariable final UUID id,
            @RequestBody(required = false) final ReleaseApprovalDecisionRequestDto dto) {
        releaseAuditService.logDisapprove(id, dto);
        releaseService.approveRelease(id, OutcomeEnum.REJECTED, null,
                dto == null ? null : dto.getNotes());
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
            @Parameter(description = "ID da release", required = true,
                example = "10000000-0000-0000-0000-000000000001") @PathVariable final UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) final String idempotencyKey) {
        releasePromotionCoordinator.promote(id, idempotencyKey);

        return ResponseEntity.noContent().build();
    }

}
