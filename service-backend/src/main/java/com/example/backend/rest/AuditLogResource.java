package com.example.backend.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.SecurityContextUtils;
import com.example.backend.service.AuditLogService;

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
@CrossOrigin(
        origins = {"http://localhost:80", "http://localhost:4200"},
        maxAge = RestConstants.CORS_MAX_AGE)
@RequestMapping("/api/audit")
@Tag(name = "Auditoria", description = "Endpoint de gestão de logs de auditoria")
@Slf4j
@Validated
public class AuditLogResource {

    private final AuditLogService auditLogService;
    private final AuditLogManager auditLogManager;

    @Autowired
    public AuditLogResource(final AuditLogService service, final AuditLogManager auditLogManager) {
        this.auditLogService = Objects.requireNonNull(service, "auditLogService não pode ser nulo");
        this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager não pode ser nulo");
    }

    @Operation(summary = "Listar todos os registros de auditoria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de registros retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDto>> findAll(
            @RequestParam(required = false) final String ator,
            @RequestParam(required = false) final String acao,
            @RequestParam(required = false) final String entidade,
            @RequestParam(required = false) final String dataInicio,
            @RequestParam(required = false) final String dataFim) {
        auditLogManager.logAction(
                SecurityContextUtils.getCurrentUserEmail(),
                "LIST_AUDITLOG",
                "AuditLog",
                null,
                buildFilterPayload(ator, acao, entidade, dataInicio, dataFim));
        List<AuditLogResponseDto> result = resolveAuditQuery(ator, acao, entidade, dataInicio, dataFim);
        return ResponseEntity.ok(result);
    }

    private List<AuditLogResponseDto> resolveAuditQuery(
            final String ator,
            final String acao,
            final String entidade,
            final String dataInicio,
            final String dataFim) {
        if (hasText(ator)) {
            return auditLogService.findByActor(ator);
        }
        if (hasText(acao)) {
            return auditLogService.findByAction(acao);
        }
        if (hasText(entidade)) {
            return auditLogService.findByEntity(entidade);
        }
        if (hasText(dataInicio) || hasText(dataFim)) {
            return findAuditByDateRange(dataInicio, dataFim);
        }
        return auditLogService.findAll();
    }

    private List<AuditLogResponseDto> findAuditByDateRange(final String dataInicio, final String dataFim) {
        if (!hasText(dataInicio) || !hasText(dataFim)) {
            throw new IllegalArgumentException("dataInicio e dataFim devem ser informadas juntas");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime inicio = LocalDateTime.parse(dataInicio, formatter);
        LocalDateTime fim = LocalDateTime.parse(dataFim, formatter);
        return auditLogService.findByDateRange(inicio, fim);
    }

    private String buildFilterPayload(
            final String ator,
            final String acao,
            final String entidade,
            final String dataInicio,
            final String dataFim) {
        return String.format(
                "{ator:%s, acao:%s, entidade:%s, dataInicio:%s, dataFim:%s}",
                ator,
                acao,
                entidade,
                dataInicio,
                dataFim);
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    @Operation(summary = "Buscar registros por ator")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registros encontrados"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/actor", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDto>> findByActor(
            @Parameter(description = "Nome do ator", required = true) @RequestParam final String ator) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND_BY_ACTOR", "AuditLog", null,
            ator);
        List<AuditLogResponseDto> result = auditLogService.findByActor(ator);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar registros por ação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registros encontrados"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/action", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDto>> findByAction(
            @Parameter(description = "Ação realizada", required = true) @RequestParam final String acao) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND_BY_ACTION", "AuditLog", null,
            acao);
        List<AuditLogResponseDto> result = auditLogService.findByAction(acao);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar registros por entidade")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registros encontrados"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/entity", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDto>> findByEntity(
            @Parameter(description = "Nome da entidade", required = true) @RequestParam final String entidade) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND_BY_ENTITY", "AuditLog", null,
            entidade);
        List<AuditLogResponseDto> result = auditLogService.findByEntity(entidade);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Buscar registros por intervalo de data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registros encontrados"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(value = "/interval", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDto>> findByDateRange(
            @Parameter(description = "Data inicial (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam
                    final String dataInicio,
            @Parameter(description = "Data final (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam
                    final String dataFim) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND_BY_DATE_RANGE", "AuditLog", null,
            dataInicio + " - " + dataFim);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime inicio = LocalDateTime.parse(dataInicio, formatter);
        LocalDateTime fim = LocalDateTime.parse(dataFim, formatter);
        List<AuditLogResponseDto> result = auditLogService.findByDateRange(inicio, fim);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Criar novo registro de auditoria")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Registro criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogResponseDto> create(@RequestBody final AuditLogRequestDto dto) {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "CREATE", "AuditLog", null,
                dto.toString());
        AuditLogResponseDto result = auditLogService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
