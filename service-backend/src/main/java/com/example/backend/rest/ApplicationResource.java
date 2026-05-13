package com.example.backend.rest;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ApplicationResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.SecurityContextUtils;
import com.example.backend.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = { "http://localhost:80", "http://localhost:4200" }, maxAge = ApplicationResource.MAX_AGE)
@RequestMapping("/api/applications")
@Tag(name = "Aplicações", description = "Endpoint de gestão de aplicações")
@Slf4j
@Validated
public class ApplicationResource {
        public static final long MAX_AGE = 3600L;

        private final ApplicationService applicationService;
        private final AuditLogManager auditLogManager;

        public ApplicationResource(final ApplicationService service, final AuditLogManager auditLogManager) {
                this.applicationService = Objects.requireNonNull(service, "applicationService não pode ser nulo");
                this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager não pode ser nulo");
        }

        @Operation(
                summary = "Listar todas as aplicações",
                description = "Retorna uma lista com todas as aplicações cadastradas no sistema")
        @ApiResponses(value = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "Lista de aplicações retornada com sucesso",
                                content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = List.class))),
                        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        })
        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<ApplicationResponseDto>> findAllApplications() {
                auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "FIND", "Application", null,
                                null);
                List<ApplicationResponseDto> applications = applicationService.findAll();
                return ResponseEntity.ok(applications);
        }

        @Operation(
                summary = "Criar nova aplicação",
                description = "Cria uma nova aplicação no sistema com os dados fornecidos")
        @ApiResponses(value = {
                        @ApiResponse(
                                responseCode = "201",
                                description = "Aplicação criada com sucesso",
                                content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApplicationResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos ou incompletos"),
                        @ApiResponse(responseCode = "422", description = "Erro de negócio ao criar aplicação"),
                        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        })
        @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApplicationResponseDto> createApplication(
                        @Parameter(description = "Dados da aplicação a ser criada", required = true)
                        @RequestBody final ApplicationRequestDto applicationDto) {

                auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "CREATE", "Application", null,
                                applicationDto.toString());
                ApplicationResponseDto savedApplication = applicationService.create(applicationDto);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(savedApplication);
        }

        @Operation(summary = "Substituir aplicação", description = "Substitui os dados de uma aplicação existente")
        @ApiResponses(value = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "Aplicação atualizada com sucesso",
                                content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApplicationResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos ou incompletos"),
                        @ApiResponse(responseCode = "404", description = "Aplicação não encontrada"),
                        @ApiResponse(responseCode = "422", description = "Erro de negócio ao atualizar aplicação"),
                        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        })
        @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApplicationResponseDto> putApplication(
                        @Parameter(description = "ID único da aplicação a atualizar", required = true, example = "1")
                        @PathVariable final Long id,
                        @Parameter(description = "Novos dados da aplicação", required = true)
                        @RequestBody final ApplicationRequestDto application) {

                auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "PUT", "Application",
                                id.intValue(), application.toString());
                ApplicationResponseDto updApplication = applicationService.update(id, application, true);
                log.info("Aplicação atualizada: {}", updApplication);
                return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(updApplication);
        }

        @Operation(summary = "Atualizar aplicação", description = "Atualiza os dados de uma aplicação existente")
        @ApiResponses(value = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "Aplicação atualizada com sucesso",
                                content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApplicationResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos ou incompletos"),
                        @ApiResponse(responseCode = "404", description = "Aplicação não encontrada"),
                        @ApiResponse(responseCode = "422", description = "Erro de negócio ao atualizar aplicação"),
                        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        })
        @PatchMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApplicationResponseDto> patchApplication(
                        @Parameter(description = "ID único da aplicação a atualizar", required = true, example = "1")
                        @PathVariable final Long id,
                        @Parameter(description = "Novos dados da aplicação", required = true)
                        @RequestBody final ApplicationRequestDto application) {
                auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "PATCH", "Application",
                                id.intValue(), application.toString());
                ApplicationResponseDto updApplication = applicationService.update(id, application, false);
                log.info("Aplicação atualizada: {}", updApplication);
                return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(updApplication);
        }

}
