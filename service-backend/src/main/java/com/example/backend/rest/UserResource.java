package com.example.backend.rest;

import java.util.List;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.UserRequestDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.SecurityContextUtils;
import com.example.backend.service.UserService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

/**
 * Endpoint de autenticacao simples via token.
 */
@RestController
@CrossOrigin(origins = { "http://localhost:80", "http://localhost:4200" }, maxAge = RestConstants.CORS_MAX_AGE)
@RequestMapping("/api/users")
public class UserResource {

    private final UserService service;
    private final AuditLogManager auditLogManager;

    public UserResource(
            final UserService service,
            final AuditLogManager auditLogManager) {
        this.service = Objects.requireNonNull(service, "service nao pode ser nulo");
        this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager nao pode ser nulo");
    }

    @GetMapping(
            value = "/",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        auditLogManager.logAction(SecurityContextUtils.getCurrentUserEmail(), "LIST_ALL_USERS", "USER", null, null);
        List<UserResponseDto> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody final UserRequestDto request) {
        auditLogManager.logAction(
            SecurityContextUtils.getCurrentUserEmail(),
            "CREATE_USER", "USER", null, request.toString());
        UserResponseDto response = service.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(
            value = "/{userMail}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(
                description = "Email do usuário a ser atualizado",
                required = true)
            @PathVariable final String userMail,
            @Valid @RequestBody final UserRequestDto request) {
        auditLogManager.logAction(
            SecurityContextUtils.getCurrentUserEmail(),
            "UPDATE_USER", "USER", null, request.toString());
        UserResponseDto response = service.update(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(
            value = "/{userMail}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteUser(
            @Parameter(
                description = "Email do usuário removido",
                required = true)
            @PathVariable final String userMail) {
        auditLogManager.logAction(
            SecurityContextUtils.getCurrentUserEmail(),
            "DELETE_USER", "USER", null, null);
        service.delete(userMail);
        return ResponseEntity.noContent().build();
    }

}
